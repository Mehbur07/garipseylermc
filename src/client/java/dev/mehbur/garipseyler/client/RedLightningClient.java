package dev.mehbur.garipseyler.client;

import dev.mehbur.garipseyler.client.hud.RedFlashOverlay;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.config.ModConfig;
import dev.mehbur.garipseyler.effect.ModSounds;
import dev.mehbur.garipseyler.entity.ModEntities;
import dev.mehbur.garipseyler.entity.RedLightning;
import dev.mehbur.garipseyler.network.RedLightningPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Turns {@link RedLightningPayload}s into a client-only red bolt, a thunder sound and a red screen flash.
 */
public final class RedLightningClient {
	/** Client-only entities use negative ids so they never collide with server-assigned entity ids. */
	private static final AtomicInteger CLIENT_ENTITY_IDS = new AtomicInteger(-1_000_000);
	private static final float THUNDER_VOLUME = 6.0F;
	/** Beyond this distance the flash is at its weakest. */
	private static final double FLASH_FALLOFF_DISTANCE = 160.0;
	private static final float MIN_FLASH_FACTOR = 0.3F;

	private RedLightningClient() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(RedLightningPayload.TYPE, (payload, context) -> strike(context.client(), payload));
	}

	private static void strike(Minecraft client, RedLightningPayload payload) {
		ClientLevel level = client.level;
		if (level == null) {
			return;
		}
		ModConfig.Nether config = ConfigManager.get().nether;

		RedLightning bolt = new RedLightning(ModEntities.RED_LIGHTNING, level);
		bolt.setId(CLIENT_ENTITY_IDS.getAndDecrement());
		bolt.setPos(payload.x(), payload.floorY(), payload.z());
		bolt.setBoltHeight(payload.height());
		level.addEntity(bolt);

		if (config.redLightningSound) {
			level.playLocalSound(payload.x(), payload.floorY(), payload.z(), ModSounds.RED_LIGHTNING_THUNDER.value(),
					SoundSource.WEATHER, THUNDER_VOLUME, 0.8F + level.getRandom().nextFloat() * 0.2F, false);
		}

		if (config.redLightningFlash && client.player != null) {
			double distance = Math.sqrt(client.player.distanceToSqr(payload.x(), payload.floorY(), payload.z()));
			float falloff = Mth.clamp(1.0F - (float) (distance / FLASH_FALLOFF_DISTANCE), MIN_FLASH_FACTOR, 1.0F);
			RedFlashOverlay.trigger((float) config.redLightningFlashIntensity * falloff);
		}
	}
}
