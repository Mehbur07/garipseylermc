package dev.mehbur.garipseyler.client;

import dev.mehbur.garipseyler.client.hud.VecnaOverlay;
import dev.mehbur.garipseyler.network.VecnaMindControlPayload;
import dev.mehbur.garipseyler.network.VecnaVisionPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Client side of Vecna's visions and mind control. While mind-controlled the camera is dragged towards
 * Vecna and the movement keys are reversed (see the KeyboardInput mixin).
 */
public final class VecnaClient {
	private static final float TURN_SPEED = 0.25F;

	private static int mindControlTicks;
	private static int vecnaId;

	private VecnaClient() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(VecnaVisionPayload.TYPE, (payload, context) -> VecnaOverlay.show(payload.ticks()));
		ClientPlayNetworking.registerGlobalReceiver(VecnaMindControlPayload.TYPE, (payload, context) -> {
			mindControlTicks = payload.ticks();
			vecnaId = payload.vecnaId();
			VecnaOverlay.show(payload.ticks());
		});
		ClientTickEvents.END_CLIENT_TICK.register(VecnaClient::tick);
	}

	/** True while the movement keys should be reversed. */
	public static boolean isMindControlled() {
		return mindControlTicks > 0;
	}

	private static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (mindControlTicks <= 0 || player == null || client.level == null) {
			mindControlTicks = 0;
			return;
		}
		mindControlTicks--;
		Entity vecna = client.level.getEntity(vecnaId);
		if (vecna == null) {
			return;
		}
		Vec3 delta = vecna.getEyePosition().subtract(player.getEyePosition());
		float targetYaw = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
		float targetPitch = (float) -(Mth.atan2(delta.y, delta.horizontalDistance()) * Mth.RAD_TO_DEG);
		player.setYRot(player.getYRot() + Mth.wrapDegrees(targetYaw - player.getYRot()) * TURN_SPEED);
		player.setXRot(player.getXRot() + (targetPitch - player.getXRot()) * TURN_SPEED);
	}
}
