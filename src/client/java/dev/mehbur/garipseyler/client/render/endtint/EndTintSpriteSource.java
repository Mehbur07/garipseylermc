package dev.mehbur.garipseyler.client.render.endtint;

import com.mojang.serialization.MapCodec;
import dev.mehbur.garipseyler.client.mixin.SpriteContentsAccessor;
import dev.mehbur.garipseyler.config.ConfigManager;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Block atlas source ({@code garipseylermc:end_dripstone}) that adds a dripstone-coloured copy of every
 * {@code textures/block/*.png} of every namespace (vanilla, mods, resource packs). Copies keep the original
 * animation and are named {@code garipseylermc:end_tint/<namespace>/<path>}. Generated at resource load, so
 * no recoloured vanilla texture ships with the mod.
 */
public final class EndTintSpriteSource implements SpriteSource {
	public static final EndTintSpriteSource INSTANCE = new EndTintSpriteSource();
	public static final MapCodec<EndTintSpriteSource> MAP_CODEC = MapCodec.unit(INSTANCE);

	private static final FileToIdConverter BLOCK_TEXTURES = new FileToIdConverter("textures/block", ".png");

	private EndTintSpriteSource() {
	}

	@Override
	public void run(ResourceManager resourceManager, SpriteSource.Output output) {
		if (!ConfigManager.get().end.dripstoneBlocks) {
			return;
		}
		BLOCK_TEXTURES.listMatchingResources(resourceManager).forEach((file, resource) -> {
			Identifier original = BLOCK_TEXTURES.fileToId(file).withPrefix("block/");
			Identifier tinted = EndTint.tintedSpriteId(original);
			output.add(tinted, loader -> {
				SpriteContents contents = loader.loadSprite(tinted, resource);
				if (contents != null) {
					DripstoneRecolor.apply(((SpriteContentsAccessor) contents).garipseylermc$getOriginalImage());
				}
				return contents;
			});
		});
	}

	@Override
	public MapCodec<EndTintSpriteSource> codec() {
		return MAP_CODEC;
	}
}
