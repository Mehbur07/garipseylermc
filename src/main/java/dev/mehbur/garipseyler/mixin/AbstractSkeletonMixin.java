package dev.mehbur.garipseyler.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.mehbur.garipseyler.config.ConfigManager;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Skeletons look like Demogorgons, so they fight like them: unless {@code demogorgon.skeletonsShootArrows} is on, a held
 * bow is ignored when the weapon goal is picked and every skeleton type uses its melee goal. The weapon goal is
 * re-picked on spawn, load and every equipment change, so this also covers old worlds and bows picked up later.
 */
@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin {
	@ModifyExpressionValue(method = "reassessWeaponGoal", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
	private boolean garipseylermc$meleeOnly(boolean holdsBow) {
		return holdsBow && ConfigManager.get().demogorgon.skeletonsShootArrows;
	}
}
