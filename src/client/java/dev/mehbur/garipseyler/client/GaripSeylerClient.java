package dev.mehbur.garipseyler.client;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.client.hud.RedFlashOverlay;
import dev.mehbur.garipseyler.client.hud.VecnaOverlay;
import dev.mehbur.garipseyler.client.input.TelekinesisKey;
import dev.mehbur.garipseyler.client.render.MobReskins;
import dev.mehbur.garipseyler.client.render.ModModelLayers;
import dev.mehbur.garipseyler.client.render.RedLightningRenderer;
import dev.mehbur.garipseyler.client.render.UpsideDownSporeParticle;
import dev.mehbur.garipseyler.client.render.endtint.EndTint;
import dev.mehbur.garipseyler.client.render.vecna.VecnaRenderer;
import dev.mehbur.garipseyler.effect.ModParticles;
import dev.mehbur.garipseyler.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;

/**
 * Client-only entrypoint: renderers, sky/fog, HUD and key bindings are registered from here.
 */
public class GaripSeylerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ParticleProviderRegistry.getInstance().register(ModParticles.UPSIDE_DOWN_SPORE, UpsideDownSporeParticle.Provider::new);
		EntityRenderers.register(ModEntities.RED_LIGHTNING, RedLightningRenderer::new);
		EntityRenderers.register(ModEntities.VECNA, VecnaRenderer::new);
		EntityRenderers.register(ModEntities.PSYCHIC_BLAST, NoopRenderer::new);
		ModModelLayers.register();
		MobReskins.register();
		RedFlashOverlay.register();
		RedLightningClient.register();
		EndTint.register();
		VecnaOverlay.register();
		VecnaClient.register();
		TelekinesisKey.register();

		GaripSeylerMod.LOGGER.info("Garip Şeyler MC client initialized.");
	}
}
