package dev.mehbur.garipseyler.entity;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.entity.vecna.PsychicBlast;
import dev.mehbur.garipseyler.entity.vecna.Vecna;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
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

	/** Boss; only ever spawned by {@code VecnaSpawning} (no spawn egg, {@code /summon} refused). */
	public static final EntityType<Vecna> VECNA = register("vecna",
			EntityType.Builder.<Vecna>of(Vecna::new, MobCategory.MONSTER)
					.noLootTable()
					.noSummon()
					.fireImmune()
					.sized(0.9F, 2.9F)
					.eyeHeight(2.6F)
					.clientTrackingRange(10));

	public static final EntityType<PsychicBlast> PSYCHIC_BLAST = register("psychic_blast",
			EntityType.Builder.<PsychicBlast>of(PsychicBlast::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(10));

	private ModEntities() {
	}

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, GaripSeylerMod.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	/** Forces class loading so the static fields are registered. */
	public static void init() {
		FabricDefaultAttributeRegistry.register(VECNA, Vecna.createAttributes());
	}
}
