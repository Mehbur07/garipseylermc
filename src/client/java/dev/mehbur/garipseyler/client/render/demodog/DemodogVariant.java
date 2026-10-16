package dev.mehbur.garipseyler.client.render.demodog;

import dev.mehbur.garipseyler.GaripSeylerMod;
import net.minecraft.resources.Identifier;

/**
 * The vanilla mobs shown as Demodogs, told apart by size and texture tone.
 */
public enum DemodogVariant {
	/** Piglin: the standard pack hunter. */
	PIGLIN("demodog", 1.0F),
	/** Piglin Brute: larger, scarred. */
	PIGLIN_BRUTE("demodog_brute", 1.15F),
	/** Zombified Piglin: slightly smaller, rotten. */
	ZOMBIFIED_PIGLIN("demodog_rotten", 0.95F),
	/** Hoglin: the big alpha. */
	HOGLIN("demodog_alpha", 1.35F),
	/** Zoglin (zombified Hoglin): rotten alpha. */
	ZOGLIN("demodog_alpha_rotten", 1.35F);

	private final Identifier texture;
	private final float scale;

	DemodogVariant(String textureName, float scale) {
		this.texture = GaripSeylerMod.id("textures/entity/demodog/" + textureName + ".png");
		this.scale = scale;
	}

	public Identifier texture() {
		return this.texture;
	}

	public float scale() {
		return this.scale;
	}
}
