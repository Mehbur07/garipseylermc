package dev.mehbur.garipseyler.mixin;

import dev.mehbur.garipseyler.item.Telekinesis;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A block held by a Telekinesis Glove skips its vanilla server tick: the glove moves it, and it must not land
 * (and turn back into a block) or time out while it rests on the ground under the player's gaze. Clients still
 * tick it, so it glides along with the velocity the glove sends.
 */
@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void garipseylermc$heldByGlove(CallbackInfo ci) {
		FallingBlockEntity self = (FallingBlockEntity) (Object) this;
		if (!self.level().isClientSide() && Telekinesis.isHeld(self)) {
			ci.cancel();
		}
	}
}
