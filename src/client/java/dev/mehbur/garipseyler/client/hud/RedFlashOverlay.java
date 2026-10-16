package dev.mehbur.garipseyler.client.hud;

import dev.mehbur.garipseyler.GaripSeylerMod;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

/**
 * Full-screen red "sky flash" drawn under the HUD when a red bolt strikes: a bright first pulse, a weaker
 * second flicker, then a fast fade. Respects the vanilla "Hide Lightning Flashes" accessibility option.
 */
public final class RedFlashOverlay {
	private static final long DURATION_MS = 900L;
	private static final int FLASH_RGB = 0xD0141A;

	private static long startMillis = -DURATION_MS;
	private static float strength;

	private RedFlashOverlay() {
	}

	public static void register() {
		HudElementRegistry.addFirst(GaripSeylerMod.id("red_flash"), RedFlashOverlay::extract);
	}

	/** Starts a flash; {@code intensity} is the peak opacity (0–1). A stronger running flash is kept. */
	public static void trigger(float intensity) {
		if (Minecraft.getInstance().options.hideLightningFlash().get()) {
			return;
		}
		if (currentAlpha() < intensity) {
			startMillis = Util.getMillis();
			strength = intensity;
		}
	}

	private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		float alpha = currentAlpha();
		if (alpha <= 0.0F) {
			return;
		}
		graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), ARGB.color(Mth.floor(alpha * 255.0F), FLASH_RGB));
	}

	private static float currentAlpha() {
		long elapsed = Util.getMillis() - startMillis;
		if (elapsed >= DURATION_MS) {
			return 0.0F;
		}
		float t = elapsed / (float) DURATION_MS;
		// Two flickers (peaks at t=0 and t≈0.3) under an exponential fade.
		float flicker = t < 0.15F ? 1.0F : t < 0.25F ? 0.35F : 0.75F;
		return strength * flicker * (float) Math.exp(-4.0 * t);
	}
}
