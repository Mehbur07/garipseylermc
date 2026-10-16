package dev.mehbur.garipseyler.client.render.endtint;

import com.google.common.collect.MapMaker;
import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.SpriteSourceRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Deepest World look: while the client is in the End, every block model except End stone is drawn with
 * dripstone-coloured copies of its textures ({@link EndTintSpriteSource}). Blocks with a block entity
 * (chests, barrels, furnaces, shulker boxes, beds, signs…) and the workstations in {@link #KEEP_ORIGINAL}
 * keep their look. Purely visual and client-side: the world itself is not changed, and the Overworld and
 * the Nether look vanilla.
 */
public final class EndTint {
	public static final Identifier SPRITE_SOURCE_ID = GaripSeylerMod.id("end_dripstone");
	private static final String SPRITE_PREFIX = "end_tint/";

	/** Blocks without a block entity that still count as "chests etc.": workstations and utility blocks. */
	private static final Set<Block> KEEP_ORIGINAL = Set.of(
			Blocks.END_STONE,
			Blocks.CRAFTING_TABLE, Blocks.SMITHING_TABLE, Blocks.FLETCHING_TABLE, Blocks.CARTOGRAPHY_TABLE,
			Blocks.LOOM, Blocks.STONECUTTER, Blocks.GRINDSTONE,
			Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL,
			Blocks.COMPOSTER, Blocks.CAULDRON, Blocks.WATER_CAULDRON, Blocks.LAVA_CAULDRON, Blocks.POWDER_SNOW_CAULDRON,
			Blocks.RESPAWN_ANCHOR,
			// Flames and portals are effects rather than block surfaces.
			Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.NETHER_PORTAL);

	/** Recoloured copy of each model part, keyed by identity; parts of a previous resource reload are dropped. */
	private static final ConcurrentMap<BlockStateModelPart, BlockStateModelPart> PARTS = new MapMaker().weakKeys().makeMap();

	private static volatile boolean active;

	private EndTint() {
	}

	public static void register() {
		SpriteSourceRegistry.register(SPRITE_SOURCE_ID, EndTintSpriteSource.MAP_CODEC);
		ModelLoadingPlugin.register(context -> {
			Set<Block> keep = configuredExclusions();
			context.modifyBlockModelAfterBake().register((model, bakeContext) ->
					shouldRecolor(bakeContext.state(), keep) ? new EndTintModel(model) : model);
		});
		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> update(level));
		ClientTickEvents.END_CLIENT_TICK.register(client -> update(client.level));
	}

	/** True while the client is in the End and the feature is enabled; read from chunk-building threads. */
	public static boolean isActive() {
		return active;
	}

	private static void update(@Nullable ClientLevel level) {
		active = level != null && level.dimension() == Level.END && ConfigManager.get().end.dripstoneBlocks;
	}

	private static boolean shouldRecolor(BlockState state, Set<Block> configuredKeep) {
		return ConfigManager.get().end.dripstoneBlocks && !state.hasBlockEntity()
				&& !KEEP_ORIGINAL.contains(state.getBlock()) && !configuredKeep.contains(state.getBlock());
	}

	private static Set<Block> configuredExclusions() {
		Set<Block> blocks = new HashSet<>();
		for (String id : ConfigManager.get().end.keepOriginalTexture) {
			Identifier parsed = Identifier.tryParse(id);
			if (parsed != null) {
				BuiltInRegistries.BLOCK.getOptional(parsed).ifPresent(blocks::add);
			}
		}
		return blocks;
	}

	/** Sprite id of the dripstone copy of a block atlas sprite, e.g. {@code garipseylermc:end_tint/minecraft/block/stone}. */
	public static Identifier tintedSpriteId(Identifier original) {
		return GaripSeylerMod.id(SPRITE_PREFIX + original.getNamespace() + "/" + original.getPath());
	}

	static BlockStateModelPart tintedPart(BlockStateModelPart part) {
		return PARTS.computeIfAbsent(part, EndTintPart::new);
	}

	static Material.Baked tintedMaterial(Material.Baked material) {
		TextureAtlasSprite tinted = tintedSprite(material.sprite());
		return tinted == material.sprite() ? material : new Material.Baked(tinted, material.forceTranslucent());
	}

	static List<BakedQuad> tintedQuads(List<BakedQuad> quads) {
		return quads.stream().map(EndTint::tintedQuad).toList();
	}

	/** Moves the quad's UVs onto the dripstone copy of its sprite and drops biome tinting (grass, leaves). */
	private static BakedQuad tintedQuad(BakedQuad quad) {
		BakedQuad.MaterialInfo info = quad.materialInfo();
		TextureAtlasSprite from = info.sprite();
		TextureAtlasSprite to = tintedSprite(from);
		if (to == from) {
			return quad;
		}
		BakedQuad.MaterialInfo tintedInfo = new BakedQuad.MaterialInfo(to, info.layer(), info.itemRenderType(), -1, info.shade(), info.lightEmission());
		return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
				remap(quad.packedUV0(), from, to), remap(quad.packedUV1(), from, to),
				remap(quad.packedUV2(), from, to), remap(quad.packedUV3(), from, to),
				quad.direction(), tintedInfo);
	}

	private static long remap(long packedUv, TextureAtlasSprite from, TextureAtlasSprite to) {
		float u = (UVPair.unpackU(packedUv) - from.getU0()) / (from.getU1() - from.getU0());
		float v = (UVPair.unpackV(packedUv) - from.getV0()) / (from.getV1() - from.getV0());
		return UVPair.pack(to.getU0() + u * (to.getU1() - to.getU0()), to.getV0() + v * (to.getV1() - to.getV0()));
	}

	/** The dripstone copy of {@code sprite}, or {@code sprite} itself when there is none (non-block textures). */
	private static TextureAtlasSprite tintedSprite(TextureAtlasSprite sprite) {
		if (!sprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
			return sprite;
		}
		TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
		TextureAtlasSprite tinted = atlas.getSprite(tintedSpriteId(sprite.contents().name()));
		return tinted == atlas.missingSprite() ? sprite : tinted;
	}
}
