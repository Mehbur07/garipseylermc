package dev.mehbur.garipseyler.mixin;

import dev.mehbur.garipseyler.block.VineGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockUtil;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * The game built an exit portal for a traveller: grow Upside Down vines on that frame too, so the
 * Nether side of every portal gets vines.
 */
@Mixin(PortalForcer.class)
public abstract class PortalForcerMixin {
	@Shadow
	@Final
	private ServerLevel level;

	@Inject(method = "createPortal", at = @At("RETURN"))
	private void garipseylermc$growVines(BlockPos origin, Direction.Axis portalAxis,
			CallbackInfoReturnable<Optional<BlockUtil.FoundRectangle>> cir) {
		cir.getReturnValue().ifPresent(rectangle -> {
			// Generated portals always run along the positive direction of their axis.
			Direction rightDir = Direction.get(Direction.AxisDirection.POSITIVE, portalAxis);
			VineGrowth.onPortalGenerated(this.level, rectangle.minCorner, rightDir, rectangle.axis1Size, rectangle.axis2Size);
		});
	}
}
