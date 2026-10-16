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
