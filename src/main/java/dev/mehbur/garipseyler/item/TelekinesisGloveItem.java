package dev.mehbur.garipseyler.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Telekinesis Glove (any {@link GloveTier}): works only in the offhand. The power itself lives in {@link Telekinesis}.
 */
public class TelekinesisGloveItem extends Item {
	private final GloveTier tier;

	public TelekinesisGloveItem(Item.Properties properties, GloveTier tier) {
		super(properties);
		this.tier = tier;
	}

	public GloveTier tier() {
		return this.tier;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.garipseylermc.telekinesis_glove.tooltip.offhand").withStyle(ChatFormatting.GRAY));
		builder.accept(Component.translatable("item.garipseylermc.telekinesis_glove.tooltip.use").withStyle(ChatFormatting.DARK_PURPLE));
		switch (this.tier) {
			case BASIC -> builder.accept(Component.translatable("item.garipseylermc.telekinesis_glove.tooltip.combine").withStyle(ChatFormatting.DARK_GRAY));
			case TIER_001 -> {
				builder.accept(Component.translatable("item.garipseylermc.telekinesis_glove_001.tooltip.power").withStyle(ChatFormatting.RED));
				builder.accept(Component.translatable("item.garipseylermc.telekinesis_glove_001.tooltip.bone_break").withStyle(ChatFormatting.DARK_RED));
				builder.accept(Component.translatable("item.garipseylermc.telekinesis_glove_001.tooltip.combine").withStyle(ChatFormatting.DARK_GRAY));
			}
			case TIER_011 -> {
				builder.accept(Component.translatable("item.garipseylermc.telekinesis_glove_011.tooltip.power").withStyle(ChatFormatting.RED));
				builder.accept(Component.translatable("item.garipseylermc.telekinesis_glove_011.tooltip.bone_break").withStyle(ChatFormatting.DARK_RED));
			}
		}
	}
}
