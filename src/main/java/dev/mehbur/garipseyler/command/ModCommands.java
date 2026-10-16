package dev.mehbur.garipseyler.command;

import dev.mehbur.garipseyler.config.ConfigManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

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
		));
	}
}
