package dev.mehbur.garipseyler.entity;

import dev.mehbur.garipseyler.GaripSeylerMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Entity types of the mod.
 */
public final class ModEntities {
	/** Client-only visual bolt; never spawned on the server and not summonable. */
	public static final EntityType<RedLightning> RED_LIGHTNING = register("red_lightning",
			EntityType.Builder.<RedLightning>of(RedLightning::new, MobCategory.MISC)
					.noLootTable()
					.noSave()
					.noSummon()
					.sized(0.0F, 0.0F)
					.clientTrackingRange(16)
					.updateInterval(Integer.MAX_VALUE));

	private ModEntities() {
	}

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, GaripSeylerMod.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	/** Forces class loading so the static fields are registered. */
	public static void init() {
	}
}
