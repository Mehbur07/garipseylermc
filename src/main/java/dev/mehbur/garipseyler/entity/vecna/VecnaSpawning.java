package dev.mehbur.garipseyler.entity.vecna;

import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.effect.ModAttachments;
import dev.mehbur.garipseyler.effect.ModSounds;
import dev.mehbur.garipseyler.entity.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Vecna's one-time arrival: every player's time in the Nether is counted (saved with the player); the
 * first one to reach {@code spawnDelayMinutes} gets Vecna spawned close by. After that he never spawns
 * again in this world. There is no spawn egg and {@code /summon} is refused.
 */
public final class VecnaSpawning {
	private static final int CHECK_INTERVAL_TICKS = 20;
	private static final int TICKS_PER_MINUTE = 1200;
	private static int tickCounter;

	private VecnaSpawning() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(VecnaSpawning::tick);
	}

	private static void tick(MinecraftServer server) {
		if (++tickCounter < CHECK_INTERVAL_TICKS) {
			return;
		}
		tickCounter = 0;
		ServerLevel nether = server.getLevel(Level.NETHER);
		if (nether == null || nether.players().isEmpty()) {
			return;
		}
		VecnaWorldData data = VecnaWorldData.get(server);
		int needed = ConfigManager.get().vecna.spawnDelayMinutes * TICKS_PER_MINUTE;
		for (ServerPlayer player : nether.players()) {
			if (player.isSpectator() || !player.isAlive()) {
				continue;
			}
			int ticks = player.getAttachedOrCreate(ModAttachments.NETHER_TICKS) + CHECK_INTERVAL_TICKS;
			player.setAttached(ModAttachments.NETHER_TICKS, ticks);
			if (!data.spawned() && !player.isCreative() && ticks >= needed) {
				if (spawnNear(nether, player) != null) {
					data.markSpawned();
				}
			}
		}
	}

	/** Spawns Vecna 10–18 blocks from the player on solid ground (falls back to the player's spot). */
	public static @Nullable Vecna spawnNear(ServerLevel level, ServerPlayer player) {
		BlockPos pos = findSpot(level, player.blockPosition(), level.getRandom(), 10.0, 8.0);
		Vecna vecna = ModEntities.VECNA.spawn(level, pos, EntitySpawnReason.EVENT);
		if (vecna == null) {
			return null;
		}
		vecna.setTarget(player);
		level.sendParticles(ParticleTypes.SCULK_SOUL, vecna.getX(), vecna.getY(1.0), vecna.getZ(), 60, 0.6, 1.2, 0.6, 0.05);
		level.playSound(null, vecna.getX(), vecna.getY(), vecna.getZ(), ModSounds.VECNA_SPAWN, SoundSource.HOSTILE, 6.0F, 1.0F);
		for (ServerPlayer nearby : level.players()) {
			if (nearby.distanceToSqr(vecna) < 64 * 64) {
				nearby.sendSystemMessage(Component.translatable("message.garipseylermc.vecna.arrived").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
				nearby.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
			}
		}
		return vecna;
	}

	/** A free 3-block-tall spot on solid ground {@code minDistance} to {@code minDistance + spread} blocks away (else the origin). */
	static BlockPos findSpot(ServerLevel level, BlockPos origin, RandomSource random, double minDistance, double spread) {
		for (int attempt = 0; attempt < 48; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double distance = minDistance + random.nextDouble() * spread;
			BlockPos column = origin.offset((int) Math.round(Math.cos(angle) * distance), 0, (int) Math.round(Math.sin(angle) * distance));
			for (int dy = 6; dy >= -6; dy--) {
				BlockPos feet = column.above(dy);
				if (level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP)
						&& level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
						&& level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
						&& level.getBlockState(feet.above(2)).getCollisionShape(level, feet.above(2)).isEmpty()
						&& level.getFluidState(feet).isEmpty()) {
					return feet;
				}
			}
		}
		return origin;
	}
}
