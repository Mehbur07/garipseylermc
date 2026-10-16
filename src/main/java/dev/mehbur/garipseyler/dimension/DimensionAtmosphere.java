package dev.mehbur.garipseyler.dimension;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.config.ModConfig;
import dev.mehbur.garipseyler.effect.ModParticles;
import dev.mehbur.garipseyler.effect.ModSounds;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.Music;
import net.minecraft.world.attribute.AmbientAdditionsSettings;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Overrides the environment attributes of every Nether biome so the whole Nether looks and sounds like
 * one red Upside Down biome, and of every End biome for the Deepest World. Terrain, features and mob
 * spawns are untouched. Values are read from the config when the biome registry loads (world start).
 */
public final class DimensionAtmosphere {
	private static final int MOOD_TICK_DELAY = 6000;
	private static final int MOOD_BLOCK_SEARCH_EXTENT = 8;
	private static final double MOOD_SOUND_OFFSET = 2.0;
	private static final double ADDITIONS_TICK_CHANCE = 0.0111;
	private static final int MUSIC_MIN_DELAY = 12000;
	private static final int MUSIC_MAX_DELAY = 24000;

	private DimensionAtmosphere() {
	}

	public static void register() {
		BiomeModifications.create(GaripSeylerMod.id("upside_down_atmosphere"))
				.add(ModificationPhase.POST_PROCESSING, BiomeSelectors.foundInTheNether(), context -> applyUpsideDown(context.getAttributes()));

		BiomeModifications.create(GaripSeylerMod.id("deepest_world_atmosphere"))
				.add(ModificationPhase.POST_PROCESSING, BiomeSelectors.foundInTheEnd(), context -> applyDeepestWorld(context.getAttributes()));
	}

	private static void applyUpsideDown(BiomeModificationContext.AttributesContext attributes) {
		ModConfig.Nether config = ConfigManager.get().nether;

		attributes.set(EnvironmentAttributes.FOG_COLOR, ModConfig.parseColor(config.fogColor));
		attributes.set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ModConfig.parseColor(config.ambientLightColor));
		attributes.set(EnvironmentAttributes.FOG_START_DISTANCE, (float) config.fogStartDistance);
		attributes.set(EnvironmentAttributes.FOG_END_DISTANCE, (float) config.fogEndDistance);
		attributes.set(EnvironmentAttributes.AMBIENT_PARTICLES,
				AmbientParticle.of(ModParticles.UPSIDE_DOWN_SPORE, (float) config.sporeProbability));
		attributes.set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(
				Optional.of(ModSounds.UPSIDE_DOWN_LOOP),
				Optional.of(new AmbientMoodSettings(ModSounds.UPSIDE_DOWN_MOOD, MOOD_TICK_DELAY, MOOD_BLOCK_SEARCH_EXTENT, MOOD_SOUND_OFFSET)),
				List.of(new AmbientAdditionsSettings(ModSounds.UPSIDE_DOWN_ADDITIONS, ADDITIONS_TICK_CHANCE))));
		attributes.set(EnvironmentAttributes.BACKGROUND_MUSIC,
				new BackgroundMusic(new Music(ModSounds.UPSIDE_DOWN_MUSIC, MUSIC_MIN_DELAY, MUSIC_MAX_DELAY, false)));
	}

	private static void applyDeepestWorld(BiomeModificationContext.AttributesContext attributes) {
		ModConfig.End config = ConfigManager.get().end;

		attributes.set(EnvironmentAttributes.FOG_COLOR, ModConfig.parseColor(config.fogColor));
		attributes.set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ModConfig.parseColor(config.ambientLightColor));
		attributes.set(EnvironmentAttributes.SKY_LIGHT_COLOR, ModConfig.parseColor(config.skyLightColor));
		attributes.set(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(ParticleTypes.ASH, (float) config.ashProbability));
		attributes.set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(
				Optional.of(ModSounds.DEEPEST_WORLD_LOOP),
				Optional.of(new AmbientMoodSettings(ModSounds.DEEPEST_WORLD_MOOD, MOOD_TICK_DELAY, MOOD_BLOCK_SEARCH_EXTENT, MOOD_SOUND_OFFSET)),
				List.of()));
	}
}
