package dev.mehbur.garipseyler.effect;

import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.entity.vecna.MusicProtection;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Vecna's curse (replaces the show's bone breaking): 1 damage every {@code curseDamageIntervalSeconds}
 * seconds, twice as fast at amplifier 1 (while levitated). Natural regeneration is blocked by a mixin into
 * the food data. Music cancels it: a player under {@link MusicProtection} loses the curse on its next tick.
 */
public class VecnaCurseEffect extends MobEffect {
	public VecnaCurseEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		if (entity instanceof ServerPlayer player && MusicProtection.isProtected(player)) {
			MusicProtection.notifyShielded(player);
			return false;
		}
		entity.hurtServer(level, entity.damageSources().magic(), 1.0F);
		level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY(1.0), entity.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
		int interval = Math.max(1, (ConfigManager.get().vecna.curseDamageIntervalSeconds * 20) >> Math.min(amplifier, 4));
		return tickCount % interval == 0;
	}
}
