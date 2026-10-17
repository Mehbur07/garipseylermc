package dev.mehbur.garipseyler.mixin;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.entity.ModEntities;
import dev.mehbur.garipseyler.entity.mindflayer.MindFlayer;
import dev.mehbur.garipseyler.entity.mindflayer.MindFlayerFight;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

/**
 * Runs the vanilla End fight with the Mind Flayer instead of the Ender Dragon (config {@code replaceEnderDragon}):
 * the first fight and every crystal respawn create a Mind Flayer, the boss bar follows it, and its death opens the
 * exit portal, adds a gateway and places the egg exactly like the dragon's. A living fight dragon in an existing
 * world is removed and replaced. Dragons summoned by commands are left alone.
 */
@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin implements MindFlayerFight {
	@Shadow private ServerBossEvent dragonEvent;
	@Shadow private ServerLevel level;
	@Shadow private BlockPos origin;
	@Shadow private int ticksSinceDragonSeen;
	@Shadow private boolean dragonKilled;
	@Shadow private boolean hasPreviouslyKilledDragon;
	@Shadow private @Nullable UUID dragonUUID;
	@Shadow private @Nullable BlockPos exitPortalLocation;

	@Shadow
	protected abstract void spawnExitPortal(boolean activated);

	@Shadow
	protected abstract void spawnNewGateway();

	@Shadow
	protected abstract @Nullable EnderDragon createNewDragon();

	private static boolean garipseylermc$enabled() {
		return ConfigManager.get().mindFlayer.replaceEnderDragon;
	}

	private void garipseylermc$setDirty() {
		((SavedData) (Object) this).setDirty();
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void garipseylermc$mindFlayerBossBar(ServerLevel level, long seed, BlockPos origin, CallbackInfo ci) {
		if (garipseylermc$enabled()) {
			this.dragonEvent.setName(Component.translatable("entity.garipseylermc.mind_flayer"));
			this.dragonEvent.setColor(BossEvent.BossBarColor.RED);
			this.dragonEvent.setOverlay(BossEvent.BossBarOverlay.NOTCHED_10);
		}
	}

	@Inject(method = "findOrCreateDragon", at = @At("HEAD"), cancellable = true)
	private void garipseylermc$findOrCreateMindFlayer(CallbackInfo ci) {
		if (!garipseylermc$enabled()) {
			return;
		}
		ci.cancel();
		List<? extends MindFlayer> found = this.level.getEntities(ModEntities.MIND_FLAYER, LivingEntity::isAlive);
		if (found.isEmpty()) {
			this.createNewDragon();
		} else {
			this.dragonUUID = found.getFirst().getUUID();
			this.garipseylermc$setDirty();
		}
	}

	@Inject(method = "createNewDragon", at = @At("HEAD"), cancellable = true)
	private void garipseylermc$createMindFlayer(CallbackInfoReturnable<@Nullable EnderDragon> cir) {
		if (!garipseylermc$enabled()) {
			return;
		}
		cir.setReturnValue(null);
		MindFlayer flayer = MindFlayer.spawnForFight(this.level, this.origin);
		if (flayer == null) {
			return;
		}
		this.dragonUUID = flayer.getUUID();
		this.garipseylermc$setDirty();
		GaripSeylerMod.LOGGER.info("Mind Flayer spawned for the End fight at {}", flayer.blockPosition());
		if (this.hasPreviouslyKilledDragon) {
			// Crystal respawn: counts for "The End... Again".
			for (ServerPlayer player : this.dragonEvent.getPlayers()) {
				CriteriaTriggers.SUMMONED_ENTITY.trigger(player, flayer);
			}
		}
	}

	/** Every fight dragon ticks this; a living one is swapped for the Mind Flayer (existing worlds). */
	@Inject(method = "updateDragon", at = @At("HEAD"), cancellable = true)
	private void garipseylermc$replaceDragon(EnderDragon dragon, CallbackInfo ci) {
		if (garipseylermc$enabled() && dragon.isAlive() && dragon.getUUID().equals(this.dragonUUID)) {
			ci.cancel();
			GaripSeylerMod.LOGGER.info("Replacing the End fight's Ender Dragon with the Mind Flayer");
			dragon.discard();
			this.dragonUUID = null;
			this.garipseylermc$setDirty();
		}
	}

	@Override
	public void garipseylermc$updateMindFlayer(MindFlayer flayer) {
		if (flayer.getUUID().equals(this.dragonUUID)) {
			this.dragonEvent.setProgress(flayer.getHealth() / flayer.getMaxHealth());
			this.dragonEvent.setName(flayer.getDisplayName());
			this.dragonEvent.setColor(BossEvent.BossBarColor.RED);
			this.ticksSinceDragonSeen = 0;
		}
	}

	@Override
	public void garipseylermc$setMindFlayerKilled(MindFlayer flayer) {
		if (!flayer.getUUID().equals(this.dragonUUID)) {
			return;
		}
		this.dragonEvent.setProgress(0.0F);
		this.dragonEvent.setVisible(false);
		this.spawnExitPortal(true);
		this.spawnNewGateway();
		if (!this.hasPreviouslyKilledDragon) {
			this.level.setBlockAndUpdate(this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.getLocation(this.origin)),
					Blocks.DRAGON_EGG.defaultBlockState());
		}
		this.hasPreviouslyKilledDragon = true;
		this.dragonKilled = true;
		this.garipseylermc$setDirty();
	}

	@Override
	public BlockPos garipseylermc$origin() {
		return this.origin;
	}

	@Override
	public boolean garipseylermc$isBossKilled() {
		return this.dragonKilled;
	}

	@Override
	public @Nullable BlockPos garipseylermc$exitPortal() {
		return this.exitPortalLocation;
	}
}
