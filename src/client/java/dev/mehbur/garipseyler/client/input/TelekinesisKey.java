package dev.mehbur.garipseyler.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.item.ModItems;
import dev.mehbur.garipseyler.network.TelekinesisPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Telekinesis key (default V, rebindable under Controls → Garip Şeyler MC). Each press asks the server to
 * grab or throw; the server checks the glove, range and cooldown.
 */
public final class TelekinesisKey {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(GaripSeylerMod.id("main"));
	public static final KeyMapping KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.garipseylermc.telekinesis", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));

	private TelekinesisKey() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(TelekinesisKey::tick);
	}

	private static void tick(Minecraft client) {
		while (KEY.consumeClick()) {
			if (client.player != null && client.gui.screen() == null
					&& (client.player.getOffhandItem().is(ModItems.TELEKINESIS_GLOVE) || client.player.getMainHandItem().is(ModItems.TELEKINESIS_GLOVE))) {
				ClientPlayNetworking.send(TelekinesisPayload.INSTANCE);
			}
		}
	}
}
