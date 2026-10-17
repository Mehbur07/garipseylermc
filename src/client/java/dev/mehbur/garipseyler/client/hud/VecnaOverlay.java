package dev.mehbur.garipseyler.client.hud;

import dev.mehbur.garipseyler.GaripSeylerMod;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

/**
 * Pulsing red vignette for Vecna's visions and mind control: a frame that is darkest at the screen edges
 * and fades towards the centre, beating roughly once a second (with the clock chimes).
 */
public final class VecnaOverlay {
	private static final int VIGNETTE_RGB = 0x8A0A14;
	private static final int BANDS = 24;
	private static final float MAX_ALPHA = 0.6F;

	private static long untilMillis;
	private static long startMillis;

	private VecnaOverlay() {
	}

	public static void register() {
		HudElementRegistry.addFirst(GaripSeylerMod.id("vecna_vignette"), VecnaOverlay::extract);
	}

	/** Shows the vignette for {@code ticks} game ticks (extends a running one). */
	public static void show(int ticks) {
		long now = Util.getMillis();
		if (now >= untilMillis) {
			startMillis = now;
		}
		untilMillis = Math.max(untilMillis, now + ticks * 50L);
	}

	private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		long now = Util.getMillis();
		if (now >= untilMillis) {
			return;
		}
		float fadeIn = Mth.clamp((now - startMillis) / 400.0F, 0.0F, 1.0F);
		float fadeOut = Mth.clamp((untilMillis - now) / 400.0F, 0.0F, 1.0F);
		float pulse = 0.75F + 0.25F * Mth.sin((now - startMillis) / 1000.0F * Mth.TWO_PI);
		float strength = MAX_ALPHA * fadeIn * fadeOut * pulse;

		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		int depth = Math.min(width, height) / 3;
		for (int band = 0; band < BANDS; band++) {
			float t = 1.0F - band / (float) BANDS;
			int alpha = Mth.floor(strength * t * t * 255.0F / 3.0F);
			if (alpha <= 0) {
				continue;
			}
			int color = ARGB.color(alpha, VIGNETTE_RGB);
			int inset = band * depth / BANDS;
			int next = (band + 1) * depth / BANDS;
			graphics.fill(inset, inset, width - inset, next, color);
			graphics.fill(inset, height - next, width - inset, height - inset, color);
			graphics.fill(inset, next, next, height - next, color);
			graphics.fill(width - next, next, width - inset, height - next, color);
		}
	}
}
