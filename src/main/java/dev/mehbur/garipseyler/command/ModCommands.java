package dev.mehbur.garipseyler.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.mehbur.garipseyler.block.VineSites;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.effect.ModAttachments;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * {@code /garipseyler ...} admin commands.
 */
public final class ModCommands {
	private ModCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal("garipseyler")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.then(Commands.literal("reload").executes(context -> {
							ConfigManager.load();
							context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.reload"), true);
							return 1;
						}))
						.then(Commands.literal("vine")
								.then(Commands.argument("player", EntityArgument.player()).executes(ModCommands::vineStatus)))
		));
	}

	/** Shows a player's vine drain timer and the distance to the nearest vined portal in the Nether. */
	private static int vineStatus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		int ticks = player.getAttachedOrCreate(ModAttachments.VINE_DRAIN_TICKS);
		int intervalTicks = ConfigManager.get().vine.drainIntervalMinutes * 1200;
		ServerLevel nether = context.getSource().getServer().getLevel(Level.NETHER);
		int sites = nether == null ? 0 : VineSites.get(nether).sites().size();
		double nearest = nether == null || player.level() != nether ? -1
				: VineSites.get(nether).sites().stream().mapToDouble(site -> Math.sqrt(site.center().distanceToSqr(player.position()))).min().orElse(-1);
		context.getSource().sendSuccess(() -> Component.translatable("commands.garipseylermc.vine.status",
				player.getDisplayName(), ticks / 20, intervalTicks / 20, sites, nearest < 0 ? "-" : String.format("%.1f", nearest)), false);
		return ticks;
	}
}
