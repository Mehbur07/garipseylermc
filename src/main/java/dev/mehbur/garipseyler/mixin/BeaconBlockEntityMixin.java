package dev.mehbur.garipseyler.mixin;

import dev.mehbur.garipseyler.entity.vecna.VecnaAltar;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every ticking beacon checks whether it is the heart of Vecna's Nether altar (see {@link VecnaAltar}).
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private static void garipseylermc$vecnaAltar(Level level, BlockPos pos, BlockState state, BeaconBlockEntity beacon, CallbackInfo ci) {
		VecnaAltar.beaconTick(level, pos);
	}
}
