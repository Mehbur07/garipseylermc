package dev.mehbur.garipseyler.block;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;

/**
 * Dispensers place torches (normal, soul and copper) instead of spitting them out: standing on the block below
 * the spot in front, or on a wall next to it. This lets redstone relight Vecna's altar for a farm. If no torch
 * fits there (or one is already there), the dispenser keeps it and clicks. Redstone torches stay vanilla so
 * existing contraptions are not changed.
 */
public final class TorchDispensing {
	private static final Logger LOGGER = LogUtils.getLogger();

	private TorchDispensing() {
	}

	public static void register() {
		OptionalDispenseItemBehavior placeTorch = new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource source, ItemStack dispensed) {
				this.setSuccess(false);
				if (dispensed.getItem() instanceof BlockItem torch) {
					Direction facing = source.state().getValue(DispenserBlock.FACING);
					BlockPos target = source.pos().relative(facing);
					try {
						this.setSuccess(torch.place(new DirectionalPlaceContext(source.level(), target, facing, dispensed, Direction.UP)).consumesAction());
					} catch (Exception e) {
						LOGGER.error("Error trying to place a torch at {}", target, e);
					}
				}
				return dispensed;
			}
		};
		DispenserBlock.registerBehavior(Items.TORCH, placeTorch);
		DispenserBlock.registerBehavior(Items.SOUL_TORCH, placeTorch);
		DispenserBlock.registerBehavior(Items.COPPER_TORCH, placeTorch);
	}
}
