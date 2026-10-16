package dev.mehbur.garipseyler.effect;

import dev.mehbur.garipseyler.GaripSeylerMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Particle types of the mod. Client-side providers are registered in the client entrypoint.
 */
public final class ModParticles {
	/** Floating Upside Down spores (ambient particle of every Nether biome). */
	public static final SimpleParticleType UPSIDE_DOWN_SPORE = Registry.register(
			BuiltInRegistries.PARTICLE_TYPE, GaripSeylerMod.id("upside_down_spore"), FabricParticleTypes.simple());

	private ModParticles() {
	}

	/** Forces class loading so the static fields are registered. */
	public static void init() {
	}
}
