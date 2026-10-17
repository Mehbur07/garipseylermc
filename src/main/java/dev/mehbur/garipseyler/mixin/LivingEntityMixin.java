package dev.mehbur.garipseyler.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.mehbur.garipseyler.effect.FireAspectBonus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Multiplies melee damage from Fire Aspect swords against Upside Down creatures (see {@link FireAspectBonus}).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
	private float garipseylermc$fireAspectBonus(float damage, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) DamageSource source) {
		return FireAspectBonus.apply((LivingEntity) (Object) this, level, source, damage);
	}
}
