package dev.mehbur.garipseyler.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.mehbur.garipseyler.config.ConfigManager;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Arrows treat non-teleporting Demogorgons (Endermen) like any other mob: flaming arrows set them on fire and the
 * arrow stops in them instead of flying on as if the Enderman had teleported away.
 */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
	@ModifyExpressionValue(method = "onHitEntity", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;is(Ljava/lang/Object;)Z"))
	private boolean garipseylermc$demogorgonIsNormalTarget(boolean isEnderman) {
		return isEnderman && ConfigManager.get().demogorgon.canTeleport;
	}
}
