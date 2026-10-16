package dev.mehbur.garipseyler.effect;

import dev.mehbur.garipseyler.block.ModBlocks;
import dev.mehbur.garipseyler.block.VineSites;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.config.ModConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Upside Down vine's curse: every {@code drainIntervalMinutes} a player spends in the Nether within
 * {@code effectRadius} blocks of a vined portal, their health drops to {@code drainTargetHealth}. A warning
 * comes {@code warningSeconds} earlier. Creative and spectator players are ignored; it never kills.
 */
public final class VineDrain {
	private static final int CHECK_INTERVAL_TICKS = 20;
	private static final int SITE_RECHECK_TICKS = 100;
	private static final int TICKS_PER_MINUTE = 1200;
	private static final int DRAIN_DARKNESS_TICKS = 60;

	/** Last time each site was confirmed to still have vines. */
	private static final Map<VineSites.Site, Long> SITE_CHECKED_AT = new HashMap<>();
	private static int tickCounter;

	private VineDrain() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(VineDrain::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SITE_CHECKED_AT.clear());
	}

	private static void tick(MinecraftServer server) {
		if (++tickCounter < CHECK_INTERVAL_TICKS) {
			return;
		}
		tickCounter = 0;

		ServerLevel nether = server.getLevel(Level.NETHER);
		if (nether == null || nether.players().isEmpty()) {
			return;
		}
		ModConfig.Vine config = ConfigManager.get().vine;
		List<VineSites.Site> sites = liveSites(nether);
		if (sites.isEmpty()) {
			return;
		}

		int interval = config.drainIntervalMinutes * TICKS_PER_MINUTE;
		int warnAt = interval - config.warningSeconds * 20;
		double radiusSqr = (double) config.effectRadius * config.effectRadius;

		for (ServerPlayer player : nether.players()) {
			if (player.isCreative() || player.isSpectator() || !player.isAlive()) {
				continue;
			}
			boolean nearVines = sites.stream().anyMatch(site -> site.center().distanceToSqr(player.position()) <= radiusSqr);
			if (!nearVines) {
				continue;
			}

			int before = player.getAttachedOrCreate(ModAttachments.VINE_DRAIN_TICKS);
			int after = before + CHECK_INTERVAL_TICKS;
			if (config.warningEnabled && before < warnAt && after >= warnAt && after < interval) {
				warn(player, config.warningSeconds);
			}
			if (after >= interval) {
				drain(player, (float) config.drainTargetHealth);
				after = 0;
			}
			player.setAttached(ModAttachments.VINE_DRAIN_TICKS, after);
		}
	}

	/** Sites in the Nether that still have vines; sites whose vines are all gone are forgotten. */
	private static List<VineSites.Site> liveSites(ServerLevel nether) {
		VineSites data = VineSites.get(nether);
		long now = nether.getGameTime();
		for (VineSites.Site site : data.sites()) {
			Long checkedAt = SITE_CHECKED_AT.get(site);
			if (checkedAt != null && now - checkedAt < SITE_RECHECK_TICKS) {
				continue;
			}
			if (!site.isLoaded(nether)) {
				continue;
			}
			if (site.hasVines(nether)) {
				SITE_CHECKED_AT.put(site, now);
			} else {
				data.remove(site);
				SITE_CHECKED_AT.remove(site);
			}
		}
		return data.sites();
	}

	private static void warn(ServerPlayer player, int seconds) {
		player.sendSystemMessage(Component.translatable("message.garipseylermc.vine.warning", seconds)
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		playTo(player, ModSounds.VINE_WARNING, 1.0F);
	}

	private static void drain(ServerPlayer player, float targetHealth) {
		if (player.getHealth() > targetHealth) {
			player.setHealth(targetHealth);
		}
		player.setAbsorptionAmount(0.0F);
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DRAIN_DARKNESS_TICKS, 0, false, false));
		player.sendSystemMessage(Component.translatable("message.garipseylermc.vine.drained")
				.withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
		playTo(player, ModSounds.VINE_DRAIN, 1.0F);
		player.level().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.UPSIDE_DOWN_VINE.defaultBlockState()),
				player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.4, 0.8, 0.4, 0.05);
	}

	/** Plays a sound only this player hears. */
	private static void playTo(ServerPlayer player, Holder<SoundEvent> sound, float volume) {
		player.connection.send(new ClientboundSoundPacket(sound, SoundSource.HOSTILE,
				player.getX(), player.getY(), player.getZ(), volume, 1.0F, player.getRandom().nextLong()));
	}
}
