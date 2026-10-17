package dev.mehbur.garipseyler.item;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.block.ModBlocks;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

/**
 * Items of the mod.
 */
public final class ModItems {
	public static final Item UPSIDE_DOWN_STONE = registerBlock(ModBlocks.UPSIDE_DOWN_STONE);
	/** Vecna's only drop (0.1 %). Its telekinesis power is added in phase 7; for now it is a trophy. */
	public static final Item TELEKINESIS_GLOVE = register("telekinesis_glove",
			new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());

	private ModItems() {
	}

	private static Item register(String name, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, GaripSeylerMod.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(properties.setId(key)));
	}

	private static Item registerBlock(Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, BuiltInRegistries.BLOCK.getKey(block));
		BlockItem item = new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());
		item.registerBlocks(Item.BY_BLOCK, item);
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS)
				.register(output -> output.insertAfter(Items.CRYING_OBSIDIAN, UPSIDE_DOWN_STONE));
	}
}
