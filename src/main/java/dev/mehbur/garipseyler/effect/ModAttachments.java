package dev.mehbur.garipseyler.effect;

import com.mojang.serialization.Codec;
import dev.mehbur.garipseyler.GaripSeylerMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/**
 * Data attached to entities and saved with them.
 */
public final class ModAttachments {
	/**
	 * Ticks a player has spent in the Nether near a vined portal since their last drain. Saved with the player,
	 * so relogging does not reset it; dying does.
	 */
	public static final AttachmentType<Integer> VINE_DRAIN_TICKS = AttachmentRegistry.create(
			GaripSeylerMod.id("vine_drain_ticks"),
			builder -> builder.persistent(Codec.INT).initializer(() -> 0));

	private ModAttachments() {
	}

	/** Forces class loading so the static fields are registered. */
	public static void init() {
	}
}
