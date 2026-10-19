package dev.mehbur.garipseyler.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * Registers all custom payload types (both sides).
 */
public final class ModNetworking {
	private ModNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(RedLightningPayload.TYPE, RedLightningPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(VecnaVisionPayload.TYPE, VecnaVisionPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(VecnaMindControlPayload.TYPE, VecnaMindControlPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(TelekinesisPayload.TYPE, TelekinesisPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(BoneBreakPayload.TYPE, BoneBreakPayload.CODEC);
	}
}
