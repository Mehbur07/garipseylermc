package dev.mehbur.garipseyler.client.render.endtint;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Wraps a baked block model: outside the End it is a plain pass-through to the original model; in the End
 * it emits the original parts re-pointed at the dripstone sprites.
 */
public record EndTintModel(BlockStateModel delegate) implements BlockStateModel {
	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
		int start = output.size();
		this.delegate.collectParts(random, output);
		if (EndTint.isActive()) {
			for (int i = start; i < output.size(); i++) {
				output.set(i, EndTint.tintedPart(output.get(i)));
			}
		}
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
			Predicate<@Nullable Direction> cullTest) {
		if (EndTint.isActive()) {
			// Default implementation: collects parts through collectParts above and emits them.
			BlockStateModel.super.emitQuads(emitter, level, pos, state, random, cullTest);
		} else {
			this.delegate.emitQuads(emitter, level, pos, state, random, cullTest);
		}
	}

	@Override
	public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		return EndTint.isActive() ? null : this.delegate.createGeometryKey(level, pos, state, random);
	}

	@Override
	public Material.Baked particleMaterial() {
		Material.Baked material = this.delegate.particleMaterial();
		return EndTint.isActive() ? EndTint.tintedMaterial(material) : material;
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		Material.Baked material = this.delegate.particleMaterial(level, pos, state);
		return EndTint.isActive() ? EndTint.tintedMaterial(material) : material;
	}

	@Override
	public int materialFlags() {
		return this.delegate.materialFlags();
	}

	@Override
	public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		return this.delegate.materialFlags(level, pos, state, random);
	}
}
