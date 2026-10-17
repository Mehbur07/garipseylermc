package dev.mehbur.garipseyler.entity.vecna;

import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.effect.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Counter mechanic against Vecna (like Max and "Running Up That Hill"): a player holding a music disc, or
 * standing near a jukebox that is playing, cannot be levitated or cursed.
 */
public final class MusicProtection {
	/** Result cache: a block scan per check would be wasteful, music does not change every tick. */
	private static final Map<UUID, Long> PROTECTED_UNTIL = new WeakHashMap<>();
	private static final Map<UUID, Long> CHECKED_AT = new WeakHashMap<>();
	private static final Map<UUID, Long> NOTIFIED_AT = new WeakHashMap<>();
	private static final int CACHE_TICKS = 20;
	private static final int NOTIFY_COOLDOWN_TICKS = 60;

	private MusicProtection() {
	}

	public static boolean isProtected(ServerPlayer player) {
		if (holdsMusicDisc(player)) {
			return true;
		}
		long now = player.level().getGameTime();
		UUID id = player.getUUID();
		Long checked = CHECKED_AT.get(id);
		if (checked == null || now - checked >= CACHE_TICKS) {
			CHECKED_AT.put(id, now);
			PROTECTED_UNTIL.put(id, nearPlayingJukebox(player) ? now + CACHE_TICKS : -1L);
		}
		return PROTECTED_UNTIL.getOrDefault(id, -1L) >= now;
	}

	/** Action bar line + a soft chime telling the player the music saved them. */
	public static void notifyShielded(ServerPlayer player) {
		long now = player.level().getGameTime();
		Long last = NOTIFIED_AT.get(player.getUUID());
		if (last != null && now - last < NOTIFY_COOLDOWN_TICKS) {
			return;
		}
		NOTIFIED_AT.put(player.getUUID(), now);
		player.sendSystemMessage(Component.translatable("message.garipseylermc.vecna.shielded")
				.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), true);
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.VECNA_SHIELDED, SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	private static boolean holdsMusicDisc(ServerPlayer player) {
		for (InteractionHand hand : InteractionHand.values()) {
			if (player.getItemInHand(hand).has(DataComponents.JUKEBOX_PLAYABLE)) {
				return true;
			}
		}
		return false;
	}

	private static boolean nearPlayingJukebox(ServerPlayer player) {
		int radius = ConfigManager.get().vecna.musicProtectionRadius;
		if (radius <= 0) {
			return false;
		}
		BlockPos center = player.blockPosition();
		long radiusSqr = (long) radius * radius;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
			if (pos.distSqr(center) > radiusSqr) {
				continue;
			}
			BlockState state = player.level().getBlockState(pos);
			if (state.is(Blocks.JUKEBOX) && state.getValue(JukeboxBlock.HAS_RECORD)
					&& player.level().getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox
					&& jukebox.getSongPlayer().isPlaying()) {
				return true;
			}
		}
		return false;
	}
}
