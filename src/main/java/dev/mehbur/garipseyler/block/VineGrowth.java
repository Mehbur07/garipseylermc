package dev.mehbur.garipseyler.block;

import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.config.ModConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Grows Upside Down vines on the obsidian frame of a freshly created Nether portal, styled after the
 * Warden emerging from the ground: the emerge sound plays, then vines sprout from the bottom of the frame
 * to the top, each one bursting out with obsidian debris particles and digging sounds.
 */
public final class VineGrowth {
	private static final int DEBRIS_PARTICLES = 14;
	private static final int DIG_SOUND_EVERY = 3;
	private static final int START_DELAY_TICKS = 10;
	private static final int TIMING_JITTER_TICKS = 6;

	private record PendingVine(BlockPos pos, Direction face, long dueTick) {
	}

	private record Job(ServerLevel level, VineSites.Site site, List<PendingVine> vines) {
	}

	private static final List<Job> JOBS = new ArrayList<>();

	private VineGrowth() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(VineGrowth::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> JOBS.clear());
	}

	/** Called when a player lights a portal. Arguments describe the portal interior (the purple blocks). */
	public static void onPortalLit(ServerLevel level, BlockPos bottomLeft, Direction rightDir, int width, int height) {
		if (ConfigManager.get().vine.growOnLitPortals) {
			start(level, bottomLeft, rightDir, width, height);
		}
	}

	/** Called when the game builds a portal on the other side of a trip. */
	public static void onPortalGenerated(ServerLevel level, BlockPos bottomLeft, Direction rightDir, int width, int height) {
		if (ConfigManager.get().vine.growOnGeneratedPortals) {
			start(level, bottomLeft, rightDir, width, height);
		}
	}

	private static void start(ServerLevel level, BlockPos bottomLeft, Direction rightDir, int width, int height) {
		ModConfig.Vine config = ConfigManager.get().vine;
		RandomSource random = level.getRandom();

		// Collect every air block touching a frame obsidian block, together with the face to cling to.
		Map<BlockPos, List<Direction>> candidates = new HashMap<>();
		for (int i = -1; i <= width; i++) {
			for (int j = -1; j <= height; j++) {
				boolean onFrame = i == -1 || i == width || j == -1 || j == height;
				if (!onFrame) {
					continue;
				}
				BlockPos frame = bottomLeft.relative(rightDir, i).relative(Direction.UP, j);
				if (!level.getBlockState(frame).is(Blocks.OBSIDIAN)) {
					continue;
				}
				for (Direction direction : Direction.values()) {
					BlockPos neighbour = frame.relative(direction);
					if (level.getBlockState(neighbour).isAir() && random.nextDouble() < config.coverage) {
						candidates.computeIfAbsent(neighbour.immutable(), pos -> new ArrayList<>()).add(direction.getOpposite());
					}
				}
			}
		}
		if (candidates.isEmpty()) {
			return;
		}

		int minY = bottomLeft.getY() - 2;
		int span = height + 3;
		long now = level.getGameTime();
		List<PendingVine> vines = new ArrayList<>();
		candidates.forEach((pos, faces) -> {
			long due = now + START_DELAY_TICKS
					+ (long) (config.growthDurationTicks * (pos.getY() - minY) / (double) span)
					+ random.nextInt(TIMING_JITTER_TICKS);
			faces.forEach(face -> vines.add(new PendingVine(pos, face, due)));
		});
		vines.sort(Comparator.comparingLong(PendingVine::dueTick));

		BlockPos min = bottomLeft.relative(rightDir, -2).relative(Direction.UP, -2);
		BlockPos max = bottomLeft.relative(rightDir, width + 1).relative(Direction.UP, height + 1);
		BlockPos boxMin = new BlockPos(Math.min(min.getX(), max.getX()) - 1, min.getY(), Math.min(min.getZ(), max.getZ()) - 1);
		BlockPos boxMax = new BlockPos(Math.max(min.getX(), max.getX()) + 1, max.getY(), Math.max(min.getZ(), max.getZ()) + 1);
		// The site is registered once its first vine exists, otherwise the drain check would see an empty
		// frame and forget it before anything has grown.
		JOBS.add(new Job(level, new VineSites.Site(boxMin, boxMax), vines));

		Vec3 center = Vec3.atCenterOf(bottomLeft.relative(rightDir, width / 2).relative(Direction.UP, height / 2));
		level.playSound(null, center.x, center.y, center.z, SoundEvents.WARDEN_EMERGE, SoundSource.BLOCKS, 1.5F, 1.0F);
	}

	private static void tick(MinecraftServer server) {
		Iterator<Job> jobs = JOBS.iterator();
		while (jobs.hasNext()) {
			Job job = jobs.next();
			long now = job.level().getGameTime();
			Iterator<PendingVine> pending = job.vines().iterator();
			int placed = 0;
			while (pending.hasNext()) {
				PendingVine vine = pending.next();
				if (vine.dueTick() > now) {
					break;
				}
				pending.remove();
				if (grow(job.level(), vine)) {
					VineSites.get(job.level()).add(job.site());
					if (placed++ % DIG_SOUND_EVERY == 0) {
						job.level().playSound(null, vine.pos(), SoundEvents.WARDEN_DIG, SoundSource.BLOCKS, 0.5F, 1.1F);
					}
				}
			}
			if (job.vines().isEmpty()) {
				jobs.remove();
			}
		}
	}

	private static boolean grow(ServerLevel level, PendingVine vine) {
		BlockPos pos = vine.pos();
		if (!level.isLoaded(pos) || !level.getBlockState(pos.relative(vine.face())).is(Blocks.OBSIDIAN)) {
			return false;
		}
		BlockState current = level.getBlockState(pos);
		BlockState vineState;
		if (current.isAir()) {
			vineState = ModBlocks.UPSIDE_DOWN_VINE.defaultBlockState();
		} else if (current.is(ModBlocks.UPSIDE_DOWN_VINE)) {
			vineState = current.setValue(UpsideDownVineBlock.AGE, 0);
		} else {
			return false;
		}
		vineState = vineState.setValue(MultifaceBlock.getFaceProperty(vine.face()), true);
		level.setBlock(pos, vineState, Block.UPDATE_ALL);
		level.scheduleTick(pos, ModBlocks.UPSIDE_DOWN_VINE, UpsideDownVineBlock.GROWTH_STEP_TICKS);

		// Debris bursts out of the obsidian the vine emerges from, like the ground around an emerging Warden.
		Vec3 surface = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(vine.face().getUnitVec3i()).scale(0.45));
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OBSIDIAN.defaultBlockState()),
				surface.x, surface.y, surface.z, DEBRIS_PARTICLES, 0.25, 0.25, 0.25, 0.1);
		level.playSound(null, pos, SoundEvents.SCULK_VEIN_PLACE, SoundSource.BLOCKS, 0.8F, 0.7F);
		return true;
	}
}
