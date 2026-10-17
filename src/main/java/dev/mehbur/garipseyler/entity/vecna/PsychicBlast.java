package dev.mehbur.garipseyler.entity.vecna;

import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.entity.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Vecna's ranged attack: a slow-homing-free ball of psychic energy. Magic damage plus a moment of
 * darkness on hit; breaks no blocks and starts no fires. Drawn with particles only.
 */
public class PsychicBlast extends AbstractHurtingProjectile {
	private static final DustParticleOptions TRAIL = new DustParticleOptions(0xC0103A, 1.6F);
	private static final int MAX_AGE_TICKS = 100;
	private static final int DARKNESS_TICKS = 50;

	public PsychicBlast(EntityType<? extends PsychicBlast> type, Level level) {
		super(type, level);
	}

	public PsychicBlast(Level level, LivingEntity owner, Vec3 direction) {
		super(ModEntities.PSYCHIC_BLAST, owner, direction, level);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			for (int i = 0; i < 3; i++) {
				this.level().addParticle(ParticleTypes.WITCH, this.getRandomX(0.4), this.getRandomY(), this.getRandomZ(0.4), 0.0, 0.0, 0.0);
			}
		} else if (this.tickCount > MAX_AGE_TICKS) {
			this.discard();
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}
		Entity target = hitResult.getEntity();
		if (target == this.getOwner()) {
			return;
		}
		float damage = (float) ConfigManager.get().vecna.psychicBlastDamage;
		if (target.hurtServer(level, this.damageSources().indirectMagic(this, this.getOwner()), damage) && target instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_TICKS, 0), this.getEffectSource());
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (this.level() instanceof ServerLevel level) {
			level.sendParticles(TRAIL, this.getX(), this.getY(), this.getZ(), 30, 0.5, 0.5, 0.5, 0.0);
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 0.6F, 1.6F);
			this.discard();
		}
	}

	@Override
	protected @Nullable ParticleOptions getTrailParticle() {
		return TRAIL;
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}

	@Override
	public boolean isOnFire() {
		return false;
	}
}
