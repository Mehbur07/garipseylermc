package dev.mehbur.garipseyler.mixin;

import dev.mehbur.garipseyler.effect.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vecna's curse stops natural (hunger) regeneration. Potions, golden apples and other healing still work.
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V"))
	private void garipseylermc$noRegenWhileCursed(ServerPlayer player, float amount) {
		if (!player.hasEffect(ModEffects.VECNA_CURSE)) {
			player.heal(amount);
		}
	}
}
