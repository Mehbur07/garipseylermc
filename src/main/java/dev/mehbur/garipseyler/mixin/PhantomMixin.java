package dev.mehbur.garipseyler.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Phantoms pick their circling anchor 20–40 blocks above the target, or 10–30 blocks above the top block
 * of the column. In a dimension with a ceiling (the Nether) that is inside or above the bedrock roof,
 * so Demobats would get stuck or fly over the roof. Here the anchor is kept under the cave ceiling.
 * Dimensions without a ceiling are untouched.
 */
@Mixin(Phantom.class)
public abstract class PhantomMixin extends Mob {
	@Unique
	private static final int CHECK_INTERVAL_TICKS = 10;
	@Unique
	private static final int MAX_CEILING_SEARCH = 40;
	@Unique
	private static final int CEILING_MARGIN = 3;

	@Shadow
	private @Nullable BlockPos anchorPoint;

	protected PhantomMixin(EntityType<? extends Mob> type, Level level) {
		super(type, level);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void garipseylermc$keepAnchorUnderCeiling(CallbackInfo ci) {
		Level level = this.level();
		if (level.isClientSide() || this.anchorPoint == null || !level.dimensionType().hasCeiling()
				|| this.tickCount % CHECK_INTERVAL_TICKS != 0) {
			return;
		}

		LivingEntity target = this.getTarget();
		BlockPos base = target != null ? target.blockPosition() : this.blockPosition();

		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(this.anchorPoint.getX(), base.getY(), this.anchorPoint.getZ());
		if (!level.isLoaded(cursor)) {
			return;
		}
		int searched = 0;
		while (searched < MAX_CEILING_SEARCH && level.getBlockState(cursor.above()).getCollisionShape(level, cursor.above()).isEmpty()) {
			cursor.move(0, 1, 0);
			searched++;
		}

		int maxY = Math.max(base.getY() + 1, cursor.getY() - CEILING_MARGIN);
		int minY = Math.min(base.getY() + 2, maxY);
		int clampedY = Math.max(minY, Math.min(maxY, this.anchorPoint.getY()));
		if (clampedY != this.anchorPoint.getY()) {
			this.anchorPoint = new BlockPos(this.anchorPoint.getX(), clampedY, this.anchorPoint.getZ());
		}
	}
}
