package dev.mehbur.garipseyler.block;

import dev.mehbur.garipseyler.GaripSeylerMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Function;

/**
 * Blocks of the mod.
 */
public final class ModBlocks {
	/** Grows on Nether portal frames; drops nothing. */
	public static final Block UPSIDE_DOWN_VINE = register("upside_down_vine", UpsideDownVineBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.CRIMSON_STEM)
					.replaceable()
					.noCollision()
					.strength(0.3F)
					.sound(SoundType.SCULK_VEIN)
					.pushReaction(PushReaction.DESTROY));

	/** Dripstone-toned stone as tough as obsidian; the End spikes are built from it. */
	public static final Block UPSIDE_DOWN_STONE = register("upside_down_stone", Block::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BROWN)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops()
					.strength(50.0F, 1200.0F)
					.sound(SoundType.DRIPSTONE_BLOCK)
					.pushReaction(PushReaction.BLOCK));

	private ModBlocks() {
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, GaripSeylerMod.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	/** Forces class loading so the static fields are registered. */
	public static void init() {
	}
}
