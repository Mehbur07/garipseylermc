package dev.mehbur.garipseyler.entity.mindflayer;

import dev.mehbur.garipseyler.block.ModBlocks;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.effect.FireAspectBonus;
import dev.mehbur.garipseyler.effect.ModSounds;
import dev.mehbur.garipseyler.entity.ModEntities;
import dev.mehbur.garipseyler.item.ModItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A chunk of Upside Down stone thrown by the Mind Flayer in a ballistic arc. Full damage on a direct hit,
 * half to everything else within {@link #SPLASH_RADIUS}; breaks no blocks. Rendered as its item (a stone cube).
 */
public class MindFlayerRock extends ThrowableItemProjectile {
	public static final double GRAVITY = 0.03;
	public static final double AIR_DRAG = 0.99;
	private static final double SPLASH_RADIUS = 3.5;
	private static final int MAX_AGE_TICKS = 200;

	public MindFlayerRock(EntityType<? extends MindFlayerRock> type, Level level) {
		super(type, level);
	}

	public MindFlayerRock(Level level, LivingEntity owner) {
		super(ModEntities.MIND_FLAYER_ROCK, owner, level, ModItems.UPSIDE_DOWN_STONE.getDefaultInstance());
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.UPSIDE_DOWN_STONE;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide() && this.tickCount > MAX_AGE_TICKS) {
			this.discard();
		}
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return entity != this.getOwner() && !(entity instanceof MindFlayerRock) && !entity.is(FireAspectBonus.UPSIDE_DOWN_CREATURES) && super.canHitEntity(entity);
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (this.level() instanceof ServerLevel level) {
			hitResult.getEntity().hurtServer(level, this.damageSources().thrown(this, this.getOwner()),
					(float) ConfigManager.get().mindFlayer.rockDamage);
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}
		Entity direct = hitResult instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
		float splash = (float) ConfigManager.get().mindFlayer.rockDamage * 0.5F;
		for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(SPLASH_RADIUS),
				living -> living != direct && living != this.getOwner() && living.isAlive() && !living.is(FireAspectBonus.UPSIDE_DOWN_CREATURES) && living.distanceToSqr(this) <= SPLASH_RADIUS * SPLASH_RADIUS
						&& !(living instanceof Player player && (player.isCreative() || player.isSpectator())))) {
			living.hurtServer(level, this.damageSources().thrown(this, this.getOwner()), splash);
		}
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.UPSIDE_DOWN_STONE.defaultBlockState()),
				this.getX(), this.getY(), this.getZ(), 80, 1.4, 0.8, 1.4, 0.2);
		level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
		level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.MIND_FLAYER_ROCK_IMPACT, SoundSource.HOSTILE, 3.0F, 1.0F);
		this.discard();
	}
}
