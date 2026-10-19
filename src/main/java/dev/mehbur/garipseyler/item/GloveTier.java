package dev.mehbur.garipseyler.item;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.config.ModConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Telekinesis Glove strength. Two gloves craft into a 001, two 001s into a 011; each tier reaches further,
 * throws harder and can grab stronger mobs. 001 and 011 also break bones (the bone-break key).
 */
public enum GloveTier {
	/** Vecna's drop: grabs everything but the {@code telekinesis_immune} bosses. */
	BASIC(Telekinesis.IMMUNE),
	/** Two gloves: also the Warden, the Wither and the Elder Guardian; halves a mob's health with bone break. */
	TIER_001(TagKey.create(Registries.ENTITY_TYPE, GaripSeylerMod.id("telekinesis_001_immune"))),
	/** Two 001s: every mob (Vecna and the Mind Flayer too); bone break kills. */
	TIER_011(null);

	private final @Nullable TagKey<EntityType<?>> immune;

	GloveTier(@Nullable TagKey<EntityType<?>> immune) {
		this.immune = immune;
	}

	/** The tier of a glove stack, or null when it is not a Telekinesis Glove. */
	public static @Nullable GloveTier of(ItemStack stack) {
		return stack.getItem() instanceof TelekinesisGloveItem glove ? glove.tier() : null;
	}

	public boolean canGrab(Entity entity) {
		return this.immune == null || !entity.is(this.immune);
	}

	public boolean breaksBones() {
		return this != BASIC;
	}

	public int range(ModConfig.Glove config) {
		return switch (this) {
			case BASIC -> config.range;
			case TIER_001 -> config.range001;
			case TIER_011 -> config.range011;
		};
	}

	public double throwStrength(ModConfig.Glove config) {
		return switch (this) {
			case BASIC -> config.throwStrength;
			case TIER_001 -> config.throwStrength001;
			case TIER_011 -> config.throwStrength011;
		};
	}
}
