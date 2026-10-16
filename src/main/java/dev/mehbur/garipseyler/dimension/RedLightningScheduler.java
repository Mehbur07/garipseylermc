package dev.mehbur.garipseyler.dimension;

import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.config.ModConfig;
import dev.mehbur.garipseyler.network.RedLightningPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Every {@code redLightningIntervalSeconds} picks one strike column near each group of players in the
 * Nether (between the cave floor and ceiling) and tells nearby clients to render a red bolt there.
 * Nothing is spawned on the server, so the strike is visual only.
 */
public final class RedLightningScheduler {
	private static final int TICKS_PER_SECOND = 20;
	/** Players closer than this to an already chosen strike share it instead of getting their own. */
	private static final double SHARE_RADIUS = 96.0;
	/** Clients within this distance of a strike receive it. */
	private static final double BROADCAST_RADIUS = 160.0;
	private static final int PLACEMENT_ATTEMPTS = 8;
	private static final int MAX_UPWARD_SEARCH = 12;
	private static final int MIN_BOLT_HEIGHT = 12;

	private static int ticksSinceStrike;

	private RedLightningScheduler() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(RedLightningScheduler::onServerTick);
	}

	private static void onServerTick(MinecraftServer server) {
		ModConfig.Nether config = ConfigManager.get().nether;
		if (!config.redLightningEnabled) {
			ticksSinceStrike = 0;
			return;
		}
		if (++ticksSinceStrike < config.redLightningIntervalSeconds * TICKS_PER_SECOND) {
			return;
		}
		ticksSinceStrike = 0;

		ServerLevel nether = server.getLevel(Level.NETHER);
		if (nether == null || nether.players().isEmpty()) {
			return;
		}

		List<Vec3> strikes = new ArrayList<>();
		for (ServerPlayer player : nether.players()) {
			boolean covered = strikes.stream().anyMatch(strike -> strike.distanceToSqr(player.position()) < SHARE_RADIUS * SHARE_RADIUS);
			if (covered) {
				continue;
			}
			findStrike(nether, player, config).ifPresent(payload -> {
				strikes.add(new Vec3(payload.x(), payload.floorY(), payload.z()));
				broadcast(nether, payload);
			});
		}
	}

	private static Optional<RedLightningPayload> findStrike(ServerLevel level, ServerPlayer player, ModConfig.Nether config) {
		RandomSource random = level.getRandom();
		int minY = level.getMinY();
		int topY = minY + level.dimensionType().logicalHeight();

		for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
			double angle = random.nextDouble() * Mth.TWO_PI;
			double distance = Mth.nextDouble(random, config.redLightningMinDistance, config.redLightningMaxDistance);
			int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
			int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
			int y = Mth.clamp(player.getBlockY(), minY + 1, topY - 2);

			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
			if (!level.isLoaded(pos)) {
				continue;
			}

			// Start inside an open cave space at roughly the player's height.
			int climbed = 0;
			while (!isOpen(level, pos) && climbed < MAX_UPWARD_SEARCH && pos.getY() < topY - 1) {
				pos.move(0, 1, 0);
				climbed++;
			}
			if (!isOpen(level, pos)) {
				continue;
			}
			int startY = pos.getY();

			while (pos.getY() > minY && isOpen(level, pos.below())) {
				pos.move(0, -1, 0);
			}
			int floorY = pos.getY();

			pos.setY(startY);
			while (pos.getY() < topY - 1 && isOpen(level, pos.above())) {
				pos.move(0, 1, 0);
			}
			int ceilingY = pos.getY() + 1;

			if (ceilingY - floorY >= MIN_BOLT_HEIGHT) {
				return Optional.of(new RedLightningPayload(x + 0.5, floorY, z + 0.5, ceilingY - floorY));
			}
		}
		return Optional.empty();
	}

	/** Air and non-solid, non-liquid blocks (fire, plants) let the bolt through; lava and solids stop it. */
	private static boolean isOpen(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() || (state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty());
	}

	private static void broadcast(ServerLevel level, RedLightningPayload payload) {
		Vec3 strike = new Vec3(payload.x(), payload.floorY(), payload.z());
		for (ServerPlayer player : level.players()) {
			if (player.position().distanceToSqr(strike) < BROADCAST_RADIUS * BROADCAST_RADIUS) {
				ServerPlayNetworking.send(player, payload);
			}
		}
	}
}
