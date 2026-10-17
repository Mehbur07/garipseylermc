package dev.mehbur.garipseyler.effect;

import dev.mehbur.garipseyler.GaripSeylerMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Status effects of the mod.
 */
public final class ModEffects {
	/** Vecna's curse: steady damage and no natural regeneration. */
	public static final Holder<MobEffect> VECNA_CURSE = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
			GaripSeylerMod.id("vecna_curse"), new VecnaCurseEffect(MobEffectCategory.HARMFUL, 0x6A1030));

	private ModEffects() {
	}

	/** Forces class loading so the static fields are registered. */
	public static void init() {
	}
}
