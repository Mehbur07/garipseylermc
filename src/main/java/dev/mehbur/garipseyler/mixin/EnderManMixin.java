package dev.mehbur.garipseyler.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.mehbur.garipseyler.config.ConfigManager;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Demogorgons (Endermen) do not teleport unless {@code demogorgon.canTeleport} is on. Every Enderman teleport (random,
 * fleeing water/sunlight/damage, towards a staring player, dodging projectiles) goes through {@code teleport(x, y, z)}.
 * Because they can no longer dodge, projectiles hurt them like any mob and water/rain no longer does.
 */
@Mixin(EnderMan.class)
public abstract class EnderManMixin {
	@Inject(method = "teleport(DDD)Z", at = @At("HEAD"), cancellable = true)
	private void garipseylermc$noTeleport(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		if (!ConfigManager.get().demogorgon.canTeleport) {
			cir.setReturnValue(false);
		}
	}

	/** Takes projectile hits through the normal damage path instead of the "dodge by teleporting" one. */
	@ModifyExpressionValue(method = "hurtServer", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"))
	private boolean garipseylermc$hitByProjectiles(boolean isProjectile) {
		return isProjectile && ConfigManager.get().demogorgon.canTeleport;
	}

	@Inject(method = "isSensitiveToWater", at = @At("HEAD"), cancellable = true)
	private void garipseylermc$waterproof(CallbackInfoReturnable<Boolean> cir) {
		if (!ConfigManager.get().demogorgon.canTeleport) {
			cir.setReturnValue(false);
		}
	}
}
