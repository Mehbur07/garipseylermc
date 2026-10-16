package dev.mehbur.garipseyler.client.render.endtint;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Dripstone copy of a baked model part: same geometry, quads moved onto the dripstone sprites. Built once
 * per part the first time it is drawn in the End.
 */
final class EndTintPart implements BlockStateModelPart {
	private final BlockStateModelPart original;
	private final Map<Direction, List<BakedQuad>> sideQuads = new EnumMap<>(Direction.class);
	private final List<BakedQuad> unculledQuads;
	private final Material.Baked particleMaterial;

	EndTintPart(BlockStateModelPart original) {
		this.original = original;
		for (Direction direction : Direction.values()) {
			this.sideQuads.put(direction, EndTint.tintedQuads(original.getQuads(direction)));
		}
		this.unculledQuads = EndTint.tintedQuads(original.getQuads(null));
		this.particleMaterial = EndTint.tintedMaterial(original.particleMaterial());
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable Direction direction) {
		return direction == null ? this.unculledQuads : this.sideQuads.get(direction);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.original.useAmbientOcclusion();
	}

	@Override
	public Material.Baked particleMaterial() {
		return this.particleMaterial;
	}

	@Override
	public int materialFlags() {
		return this.original.materialFlags();
	}
}
