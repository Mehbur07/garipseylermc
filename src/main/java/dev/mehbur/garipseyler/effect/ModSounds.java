package dev.mehbur.garipseyler.effect;

import dev.mehbur.garipseyler.GaripSeylerMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Sound events of the mod. The actual .ogg files are mapped in {@code assets/garipseylermc/sounds.json}.
 */
public final class ModSounds {
	public static final Holder.Reference<SoundEvent> UPSIDE_DOWN_LOOP = register("ambient.upside_down.loop");
	public static final Holder.Reference<SoundEvent> UPSIDE_DOWN_MOOD = register("ambient.upside_down.mood");
	public static final Holder.Reference<SoundEvent> UPSIDE_DOWN_ADDITIONS = register("ambient.upside_down.additions");
	public static final Holder.Reference<SoundEvent> UPSIDE_DOWN_MUSIC = register("music.upside_down");
	public static final Holder.Reference<SoundEvent> DEEPEST_WORLD_LOOP = register("ambient.deepest_world.loop");
	public static final Holder.Reference<SoundEvent> DEEPEST_WORLD_MOOD = register("ambient.deepest_world.mood");
	public static final Holder.Reference<SoundEvent> RED_LIGHTNING_THUNDER = register("weather.red_lightning.thunder");
	public static final Holder.Reference<SoundEvent> VINE_WARNING = register("block.upside_down_vine.warning");
	public static final Holder.Reference<SoundEvent> VINE_DRAIN = register("block.upside_down_vine.drain");
	public static final Holder.Reference<SoundEvent> VECNA_AMBIENT = register("entity.vecna.ambient");
	public static final Holder.Reference<SoundEvent> VECNA_HURT = register("entity.vecna.hurt");
	public static final Holder.Reference<SoundEvent> VECNA_DEATH = register("entity.vecna.death");
	public static final Holder.Reference<SoundEvent> VECNA_SPAWN = register("entity.vecna.spawn");
	public static final Holder.Reference<SoundEvent> VECNA_CAST = register("entity.vecna.cast");
	public static final Holder.Reference<SoundEvent> VECNA_CLOCK = register("entity.vecna.clock");
	public static final Holder.Reference<SoundEvent> VECNA_PSYCHIC_BLAST = register("entity.vecna.psychic_blast");
	public static final Holder.Reference<SoundEvent> VECNA_SHIELDED = register("entity.vecna.shielded");
	public static final Holder.Reference<SoundEvent> MIND_FLAYER_AMBIENT = register("entity.mind_flayer.ambient");
	public static final Holder.Reference<SoundEvent> MIND_FLAYER_HURT = register("entity.mind_flayer.hurt");
	public static final Holder.Reference<SoundEvent> MIND_FLAYER_DEATH = register("entity.mind_flayer.death");
	public static final Holder.Reference<SoundEvent> MIND_FLAYER_STEP = register("entity.mind_flayer.step");
	public static final Holder.Reference<SoundEvent> MIND_FLAYER_ROAR = register("entity.mind_flayer.roar");
	public static final Holder.Reference<SoundEvent> MIND_FLAYER_STOMP = register("entity.mind_flayer.stomp");
	public static final Holder.Reference<SoundEvent> MIND_FLAYER_THROW = register("entity.mind_flayer.throw");
	public static final Holder.Reference<SoundEvent> MIND_FLAYER_ROCK_IMPACT = register("entity.mind_flayer.rock_impact");
	public static final Holder.Reference<SoundEvent> MIND_FLAYER_REGEN = register("entity.mind_flayer.regen");
	public static final Holder.Reference<SoundEvent> GLOVE_GRAB = register("item.telekinesis_glove.grab");
	public static final Holder.Reference<SoundEvent> GLOVE_THROW = register("item.telekinesis_glove.throw");
	public static final Holder.Reference<SoundEvent> GLOVE_BONE_BREAK = register("item.telekinesis_glove.bone_break");

	private ModSounds() {
	}

	private static Holder.Reference<SoundEvent> register(String name) {
		Identifier id = GaripSeylerMod.id(name);
		return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	/** Forces class loading so the static fields are registered. */
	public static void init() {
	}
}
