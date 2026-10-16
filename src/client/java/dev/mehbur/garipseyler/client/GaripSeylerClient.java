package dev.mehbur.garipseyler.client;

import dev.mehbur.garipseyler.GaripSeylerMod;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-only entrypoint: renderers, sky/fog, HUD and key bindings are registered from here.
 */
public class GaripSeylerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		GaripSeylerMod.LOGGER.info("Garip Şeyler MC client initialized.");
	}
}
