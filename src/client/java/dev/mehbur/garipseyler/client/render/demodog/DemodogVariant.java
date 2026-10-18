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
	ZOGLIN("demodog_alpha_rotten", 1.35F),
	/**
	 * Strider: a lava-cracked mount, sized so a rider sits on its back. Turns grey-purple and shivers out of lava,
	 * like the vanilla cold Strider.
	 */
	STRIDER("demodog_lava", "demodog_lava_cold", 1.6F),
	/** Magma Cube: a glowing magma-seamed hopper; its drawn size follows the cube size (see DemodogRenderer). */
	MAGMA_CUBE("demodog_magma", 1.0F);

	private final Identifier texture;
	private final Identifier coldTexture;
	private final float scale;

	DemodogVariant(String textureName, float scale) {
		this(textureName, textureName, scale);
	}

	DemodogVariant(String textureName, String coldTextureName, float scale) {
		this.texture = texture(textureName);
		this.coldTexture = texture(coldTextureName);
		this.scale = scale;
	}

	private static Identifier texture(String name) {
		return GaripSeylerMod.id("textures/entity/demodog/" + name + ".png");
	}

	public Identifier texture() {
		return this.texture;
	}

	/** Texture of a Strider out of lava; the normal texture for the other variants. */
	public Identifier coldTexture() {
		return this.coldTexture;
	}

	/** Drawn at full brightness, like the vanilla Magma Cube. */
	public boolean fullBright() {
		return this == MAGMA_CUBE;
	}

	public float scale() {
		return this.scale;
	}
}
