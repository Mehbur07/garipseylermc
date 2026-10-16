package dev.mehbur.garipseyler.mixin;

import dev.mehbur.garipseyler.block.VineGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A player lit a Nether portal: grow Upside Down vines on its frame.
 */
@Mixin(PortalShape.class)
public abstract class PortalShapeMixin {
	@Shadow
	@Final
	private Direction rightDir;
	@Shadow
	@Final
	private BlockPos bottomLeft;
	@Shadow
	@Final
	private int width;
	@Shadow
	@Final
	private int height;

	@Inject(method = "createPortalBlocks", at = @At("TAIL"))
	private void garipseylermc$growVines(LevelAccessor level, CallbackInfo ci) {
		if (level instanceof ServerLevel serverLevel) {
			VineGrowth.onPortalLit(serverLevel, this.bottomLeft, this.rightDir, this.width, this.height);
		}
	}
}
