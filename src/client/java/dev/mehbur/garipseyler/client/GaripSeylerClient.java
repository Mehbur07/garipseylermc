package dev.mehbur.garipseyler.client;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.client.render.UpsideDownSporeParticle;
import dev.mehbur.garipseyler.effect.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

/**
 * Client-only entrypoint: renderers, sky/fog, HUD and key bindings are registered from here.
 */
public class GaripSeylerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ParticleProviderRegistry.getInstance().register(ModParticles.UPSIDE_DOWN_SPORE, UpsideDownSporeParticle.Provider::new);

		GaripSeylerMod.LOGGER.info("Garip Şeyler MC client initialized.");
	}
}
