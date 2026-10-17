package dev.mehbur.garipseyler.client.mixin;

import dev.mehbur.garipseyler.client.VecnaClient;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vecna's mind control reverses the movement keys: forward walks backward, left walks right.
 */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
	@Inject(method = "tick", at = @At("TAIL"))
	private void garipseylermc$reverseUnderMindControl(CallbackInfo ci) {
		if (VecnaClient.isMindControlled()) {
			this.moveVector = this.moveVector.negated();
		}
	}
}
