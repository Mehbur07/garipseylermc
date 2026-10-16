package dev.mehbur.garipseyler.dimension;

import dev.mehbur.garipseyler.config.ConfigManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Shows "Ters Dünya" / "En Derin Dünya" title cards when a player enters (or joins in) the Nether or End.
 */
public final class DimensionTitles {
	private static final int FADE_IN_TICKS = 20;
	private static final int STAY_TICKS = 70;
	private static final int FADE_OUT_TICKS = 30;

	private DimensionTitles() {
	}

	public static void register() {
		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> show(player, destination));
		ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> show(listener.player, listener.player.level()));
	}

	private static void show(ServerPlayer player, ServerLevel level) {
		if (level.dimension() == Level.NETHER && ConfigManager.get().nether.titleOnEnter) {
			send(player, "upside_down", ChatFormatting.RED);
		} else if (level.dimension() == Level.END && ConfigManager.get().end.titleOnEnter) {
			send(player, "deepest_world", ChatFormatting.DARK_PURPLE);
		}
	}

	private static void send(ServerPlayer player, String key, ChatFormatting color) {
		player.connection.send(new ClientboundSetTitlesAnimationPacket(FADE_IN_TICKS, STAY_TICKS, FADE_OUT_TICKS));
		player.connection.send(new ClientboundSetSubtitleTextPacket(
				Component.translatable("subtitle.garipseylermc." + key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
		player.connection.send(new ClientboundSetTitleTextPacket(
				Component.translatable("title.garipseylermc." + key).withStyle(color, ChatFormatting.BOLD)));
	}
}
