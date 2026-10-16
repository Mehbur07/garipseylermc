package dev.mehbur.garipseyler.entity;

import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.config.ModConfig;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;

/**
 * Demobats are vanilla Phantoms with a new look. They spawn naturally in every Nether biome (vanilla
 * Phantoms have no biome spawns; their vanilla placement rule already allows mid-air spawning), while the
 * Overworld insomnia spawner is switched off by a mixin.
 */
public final class DemobatSpawns {
	private DemobatSpawns() {
	}

	public static void register() {
		ModConfig.Demobat config = ConfigManager.get().demobat;
		if (config.netherSpawnWeight > 0) {
			BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), MobCategory.MONSTER, EntityTypes.PHANTOM,
					config.netherSpawnWeight, config.netherMinGroup, config.netherMaxGroup);
		}
	}
}
