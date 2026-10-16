package dev.mehbur.garipseyler.entity;

import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.config.ModConfig;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Demobats are vanilla Phantoms and Ghasts with a new look. Phantoms spawn naturally in every Nether biome
 * (vanilla Phantoms have no biome spawns; their vanilla placement rule already allows mid-air spawning),
 * while the Overworld insomnia spawner is switched off by a mixin.
 *
 * <p>Demobats belong to the Nether only: any Phantom or Ghast that shows up in the Overworld or the End
 * (spawn, portal travel, command, old chunk) is removed on the next tick. Happy Ghasts are a different
 * entity type and are not touched.
 */
public final class DemobatSpawns {
	/** Entities flagged while being added to a level; removed at the end of the tick, outside entity iteration. */
	private static final List<Entity> PENDING_REMOVAL = new ArrayList<>();

	private DemobatSpawns() {
	}

	public static void register() {
		ModConfig.Demobat config = ConfigManager.get().demobat;
		if (config.netherSpawnWeight > 0) {
			BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), MobCategory.MONSTER, EntityTypes.PHANTOM,
					config.netherSpawnWeight, config.netherMinGroup, config.netherMaxGroup);
		}

		ServerEntityEvents.ENTITY_LOAD.register(DemobatSpawns::onEntityLoad);
		ServerTickEvents.END_SERVER_TICK.register(DemobatSpawns::removePending);
	}

	public static boolean isDemobat(Entity entity) {
		return entity.getType() == EntityTypes.PHANTOM || entity.getType() == EntityTypes.GHAST;
	}

	private static void onEntityLoad(Entity entity, ServerLevel level) {
		if (!ConfigManager.get().demobat.netherOnly || !isDemobat(entity)) {
			return;
		}
		if (level.dimension() == Level.OVERWORLD || level.dimension() == Level.END) {
			PENDING_REMOVAL.add(entity);
		}
	}

	private static void removePending(MinecraftServer server) {
		if (PENDING_REMOVAL.isEmpty()) {
			return;
		}
		for (Entity entity : PENDING_REMOVAL) {
			if (!entity.isRemoved()) {
				entity.discard();
			}
		}
		PENDING_REMOVAL.clear();
	}
}
