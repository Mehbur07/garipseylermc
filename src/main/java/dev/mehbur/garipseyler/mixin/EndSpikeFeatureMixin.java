package dev.mehbur.garipseyler.mixin;

import dev.mehbur.garipseyler.block.ModBlocks;
import dev.mehbur.garipseyler.config.ConfigManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Builds the End spikes from Upside Down stone instead of obsidian. Only affects spikes generated from now
 * on (new End chunks and dragon respawns); spikes already saved in a world stay obsidian.
 */
@Mixin(EndSpikeFeature.class)
public abstract class EndSpikeFeatureMixin {
	@ModifyArg(method = "placeSpike", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/levelgen/feature/EndSpikeFeature;setBlock(Lnet/minecraft/world/level/LevelWriter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"),
			index = 2)
	private BlockState garipseylermc$upsideDownStone(BlockState state) {
		if (state.is(Blocks.OBSIDIAN) && ConfigManager.get().end.upsideDownStoneSpikes) {
			return ModBlocks.UPSIDE_DOWN_STONE.defaultBlockState();
		}
		return state;
	}
}
