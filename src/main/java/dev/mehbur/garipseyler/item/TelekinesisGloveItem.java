package dev.mehbur.garipseyler.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Telekinesis Glove: works only in the offhand. The power itself lives in {@link Telekinesis}.
 */
public class TelekinesisGloveItem extends Item {
	public TelekinesisGloveItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.garipseylermc.telekinesis_glove.tooltip.offhand").withStyle(ChatFormatting.GRAY));
		builder.accept(Component.translatable("item.garipseylermc.telekinesis_glove.tooltip.use").withStyle(ChatFormatting.DARK_PURPLE));
	}
}
