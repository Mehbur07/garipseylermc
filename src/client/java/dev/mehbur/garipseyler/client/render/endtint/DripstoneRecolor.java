package dev.mehbur.garipseyler.client.render.endtint;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.ARGB;

/**
 * Gradient-maps a texture onto the dripstone palette. Each texture's brightness is re-centred on the
 * dripstone mid tone (contrast kept, very flat textures get a little more) so every block ends up clearly
 * dripstone-coloured while its pattern stays readable. Alpha is untouched.
 */
public final class DripstoneRecolor {
	/** Luminance stops and colours: the five vanilla dripstone_block colours plus darker/lighter ends. */
	private static final float[] STOPS = {0.00F, 0.29F, 0.34F, 0.40F, 0.48F, 0.55F, 1.00F};
	private static final int[] COLORS = {0x1C1311, 0x634A47, 0x735450, 0x836356, 0x927965, 0xA08D71, 0xE2D3B6};
	private static final float TARGET_MEAN = 0.42F;
	private static final float MIN_SPREAD = 0.05F;
	private static final float MAX_SPREAD = 0.16F;

	private DripstoneRecolor() {
	}

	public static void apply(NativeImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		double sum = 0.0;
		double sumSquares = 0.0;
		int count = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = image.getPixel(x, y);
				if (ARGB.alpha(pixel) == 0) {
					continue;
				}
				float luminance = luminance(pixel);
				sum += luminance;
				sumSquares += luminance * luminance;
				count++;
			}
		}
		if (count == 0) {
			return;
		}

		float mean = (float) (sum / count);
		float spread = (float) Math.sqrt(Math.max(0.0, sumSquares / count - mean * mean));
		float contrast = 1.0F;
		if (spread < MIN_SPREAD) {
			contrast = spread > 1.0E-4F ? Math.min(4.0F, MIN_SPREAD / spread) : 0.0F;
		} else if (spread > MAX_SPREAD) {
			contrast = MAX_SPREAD / spread;
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = image.getPixel(x, y);
				int alpha = ARGB.alpha(pixel);
				if (alpha == 0) {
					continue;
				}
				float mapped = TARGET_MEAN + (luminance(pixel) - mean) * contrast;
				image.setPixel(x, y, ARGB.color(alpha, ramp(mapped)));
			}
		}
	}

	private static float luminance(int pixel) {
		return (0.299F * ARGB.red(pixel) + 0.587F * ARGB.green(pixel) + 0.114F * ARGB.blue(pixel)) / 255.0F;
	}

	private static int ramp(float luminance) {
		float value = Math.max(0.0F, Math.min(1.0F, luminance));
		for (int i = 1; i < STOPS.length; i++) {
			if (value <= STOPS[i]) {
				float t = (value - STOPS[i - 1]) / (STOPS[i] - STOPS[i - 1]);
				return ARGB.srgbLerp(t, COLORS[i - 1], COLORS[i]) & 0xFFFFFF;
			}
		}
		return COLORS[COLORS.length - 1];
	}
}
