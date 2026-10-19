package dev.mehbur.garipseyler.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.item.GloveTier;
import dev.mehbur.garipseyler.network.BoneBreakPayload;
import dev.mehbur.garipseyler.network.TelekinesisPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.lwjgl.glfw.GLFW;

/**
 * Glove keys, rebindable under Controls → Garip Şeyler MC: telekinesis (default V) asks the server to grab or
 * throw, bone break (default B) to crack the looked-at mob's bones. The server checks glove, range and cooldown.
 */
public final class TelekinesisKey {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(GaripSeylerMod.id("main"));
	public static final KeyMapping KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.garipseylermc.telekinesis", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));
	public static final KeyMapping BONE_BREAK = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.garipseylermc.bone_break", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));

	private TelekinesisKey() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(TelekinesisKey::tick);
	}

	private static void tick(Minecraft client) {
		while (KEY.consumeClick()) {
			sendIfWearingGlove(client, TelekinesisPayload.INSTANCE);
		}
		while (BONE_BREAK.consumeClick()) {
			sendIfWearingGlove(client, BoneBreakPayload.INSTANCE);
		}
	}

	private static void sendIfWearingGlove(Minecraft client, CustomPacketPayload payload) {
		if (client.player != null && client.gui.screen() == null
				&& (GloveTier.of(client.player.getOffhandItem()) != null || GloveTier.of(client.player.getMainHandItem()) != null)) {
			ClientPlayNetworking.send(payload);
		}
	}
}
