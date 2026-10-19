package dev.mehbur.garipseyler.entity.vecna;

import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.entity.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Vecna's summoning altar, checked by every beacon in the Nether once a second:
 * <pre>
 *   y+1   soul torches on (at least) 4 of the ring blocks
 *   y     8 netherrack around the beacon
 *   y-1   3x3 netherite blocks
 * </pre>
 * When it is complete the soul torches burn out and Vecna emerges from the ground a few blocks away, like the
 * Warden. This works even after his one natural arrival; only one Vecna may exist at a time.
 */
public final class VecnaAltar {
	private static final int CHECK_INTERVAL_TICKS = 20;
	private static final int TORCHES_NEEDED = 4;
	private static final double NOTIFY_RADIUS = 16.0;

	private VecnaAltar() {
	}

	/** Called from every beacon's tick. */
	public static void beaconTick(Level level, BlockPos beacon) {
		if (!(level instanceof ServerLevel server) || level.dimension() != Level.NETHER
				|| level.getGameTime() % CHECK_INTERVAL_TICKS != 0 || !ConfigManager.get().vecna.altarEnabled) {
			return;
		}
		List<BlockPos> torches = completeAltarTorches(server, beacon);
		if (torches == null) {
			return;
		}
		if (vecnaExists(server)) {
			notifyNearby(server, beacon, Component.translatable("message.garipseylermc.vecna.altar_busy").withStyle(ChatFormatting.DARK_RED), true);
			return;
		}
		summon(server, beacon, torches);
	}

	/** The soul torches of a complete altar around {@code beacon}, or null when it is not complete. */
	private static List<BlockPos> completeAltarTorches(ServerLevel level, BlockPos beacon) {
		List<BlockPos> torches = new ArrayList<>();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (!level.getBlockState(beacon.offset(dx, -1, dz)).is(Blocks.NETHERITE_BLOCK)) {
					return null;
				}
				if (dx == 0 && dz == 0) {
					continue;
				}
				BlockPos ring = beacon.offset(dx, 0, dz);
				if (!level.getBlockState(ring).is(Blocks.NETHERRACK)) {
					return null;
				}
				if (level.getBlockState(ring.above()).is(Blocks.SOUL_TORCH)) {
					torches.add(ring.above());
				}
			}
		}
		return torches.size() >= TORCHES_NEEDED ? torches : null;
	}

	private static boolean vecnaExists(ServerLevel level) {
		for (ServerLevel dimension : level.getServer().getAllLevels()) {
			if (!dimension.getEntities(ModEntities.VECNA, Vecna::isAlive).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private static void summon(ServerLevel level, BlockPos beacon, List<BlockPos> torches) {
		for (BlockPos torch : torches) {
			level.setBlock(torch, Blocks.AIR.defaultBlockState(), 3);
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, torch.getX() + 0.5, torch.getY() + 0.6, torch.getZ() + 0.5, 12, 0.1, 0.2, 0.1, 0.03);
			level.sendParticles(ParticleTypes.LARGE_SMOKE, torch.getX() + 0.5, torch.getY() + 0.6, torch.getZ() + 0.5, 6, 0.1, 0.2, 0.1, 0.02);
		}
		level.playSound(null, beacon, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 2.0F, 0.5F);
		level.playSound(null, beacon, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 2.0F, 0.6F);

		BlockPos spot = VecnaSpawning.findSpot(level, beacon, level.getRandom(), 4.0, 3.0);
		Vecna vecna = ModEntities.VECNA.spawn(level, spot, EntitySpawnReason.EVENT);
		if (vecna == null) {
			return;
		}
		vecna.startEmerging();
		ServerPlayer nearest = level.getNearestPlayer(vecna, 48.0) instanceof ServerPlayer player && !player.isCreative() && !player.isSpectator() ? player : null;
		if (nearest != null) {
			vecna.setTarget(nearest);
			vecna.getLookControl().setLookAt(nearest);
		}
		VecnaWorldData.get(level.getServer()).markSpawned();
		notifyNearby(level, beacon, Component.translatable("message.garipseylermc.vecna.summoned").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(vecna) < 64 * 64) {
				player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, Vecna.EMERGE_TICKS, 0, false, false));
			}
		}
	}

	private static void notifyNearby(ServerLevel level, BlockPos beacon, Component message, boolean actionBar) {
		for (ServerPlayer player : level.players()) {
			if (player.blockPosition().closerThan(beacon, NOTIFY_RADIUS)) {
				player.sendSystemMessage(message, actionBar);
			}
		}
	}
}
