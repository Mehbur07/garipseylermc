package dev.mehbur.garipseyler.effect;

import dev.mehbur.garipseyler.GaripSeylerMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Upside Down creatures fear fire: a melee hit with a Fire Aspect sword deals 2x damage (Fire Aspect I) or 4x
 * (Fire Aspect II) to anything in the {@code garipseylermc:upside_down_creatures} entity tag (Demogorgons, Demodogs,
 * Demobats, Vecna, Mind Flayer). Applied before armor, from {@code LivingEntityMixin}.
 */
public final class FireAspectBonus {
	public static final TagKey<EntityType<?>> UPSIDE_DOWN_CREATURES = TagKey.create(Registries.ENTITY_TYPE, GaripSeylerMod.id("upside_down_creatures"));

	private FireAspectBonus() {
	}

	public static float apply(LivingEntity target, ServerLevel level, DamageSource source, float damage) {
		if (!target.is(UPSIDE_DOWN_CREATURES)
				|| !(source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK))
				|| source.getDirectEntity() != source.getEntity()
				|| !(source.getEntity() instanceof LivingEntity attacker)) {
			return damage;
		}
		ItemStack weapon = source.getWeaponItem() != null ? source.getWeaponItem() : attacker.getMainHandItem();
		if (!weapon.is(ItemTags.SWORDS)) {
			return damage;
		}
		int fireAspect = EnchantmentHelper.getItemEnchantmentLevel(
				level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FIRE_ASPECT), weapon);
		return fireAspect >= 2 ? damage * 4.0F : fireAspect == 1 ? damage * 2.0F : damage;
	}
}
