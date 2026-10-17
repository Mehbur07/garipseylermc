package dev.mehbur.garipseyler.item;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.config.ModConfig;
import dev.mehbur.garipseyler.effect.ModSounds;
import dev.mehbur.garipseyler.network.TelekinesisPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Telekinesis Glove's power, server-authoritative. With the glove in the offhand, the key (V) grabs
 * the mob the player looks at (within {@code range}); it then floats in front of the player and follows
 * their gaze. Pressing again throws it where the player looks; a thrown mob that slams into something
 * takes impact damage. Bosses (the {@code garipseylermc:telekinesis_immune} entity tag) and players cannot
 * be grabbed. The cooldown starts when the mob leaves the glove.
 */
public final class Telekinesis {
	public static final TagKey<EntityType<?>> IMMUNE = TagKey.create(Registries.ENTITY_TYPE, GaripSeylerMod.id("telekinesis_immune"));
	private static final double MIN_HOLD_DISTANCE = 2.5;
	private static final double MAX_HOLD_DISTANCE = 6.0;
	private static final double MAX_STEP = 1.5;
	private static final double LEASH_SLACK = 4.0;
	private static final int THROW_TRACK_TICKS = 40;
	private static final double IMPACT_MIN_SPEED = 0.6;
	private static final DustParticleOptions AURA = new DustParticleOptions(0xB0206A, 1.0F);

	private record Hold(Entity entity, double distance, boolean hadNoAi, boolean hadNoGravity, int ticks) {
		Hold aged() {
			return new Hold(this.entity, this.distance, this.hadNoAi, this.hadNoGravity, this.ticks + 1);
		}
	}

	private record Thrown(Entity entity, @Nullable UUID thrower, int ticksLeft, double lastSpeed) {
	}

	private static final Map<UUID, Hold> HOLDS = new HashMap<>();
	private static final Map<UUID, Thrown> THROWN = new HashMap<>();

	private Telekinesis() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(TelekinesisPayload.TYPE, (payload, context) -> onKey(context.player()));
		ServerTickEvents.END_SERVER_TICK.register(Telekinesis::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> release(listener.getPlayer().getUUID()));
		// Never save a mob with the glove's NoAI / NoGravity still applied.
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			for (UUID player : HOLDS.keySet().toArray(UUID[]::new)) {
				release(player);
			}
			THROWN.clear();
		});
	}

	public static boolean isHolding(ServerPlayer player) {
		return HOLDS.containsKey(player.getUUID());
	}

	private static void onKey(ServerPlayer player) {
		if (HOLDS.containsKey(player.getUUID())) {
			throwHeld(player);
			return;
		}
		if (!player.isAlive() || player.isSpectator()) {
			return;
		}
		ItemStack offhand = player.getOffhandItem();
		if (!offhand.is(ModItems.TELEKINESIS_GLOVE)) {
			if (player.getMainHandItem().is(ModItems.TELEKINESIS_GLOVE)) {
				actionBar(player, Component.translatable("message.garipseylermc.glove.offhand_only").withStyle(ChatFormatting.GRAY));
			}
			return;
		}
		if (player.getCooldowns().isOnCooldown(offhand)) {
			actionBar(player, Component.translatable("message.garipseylermc.glove.cooldown").withStyle(ChatFormatting.GRAY));
			return;
		}
		Entity target = findTarget(player, ConfigManager.get().glove.range);
		if (target == null) {
			return;
		}
		if (target.is(IMMUNE)) {
			actionBar(player, Component.translatable("message.garipseylermc.glove.immune", target.getDisplayName()).withStyle(ChatFormatting.DARK_RED));
			player.level().sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(0.6), target.getZ(), 15, 0.3, 0.4, 0.3, 0.02);
			return;
		}
		if (HOLDS.values().stream().anyMatch(hold -> hold.entity() == target)) {
			return;
		}
		grab(player, target);
	}

	/** The living, non-player entity the player is looking at, stopping at blocks. */
	private static @Nullable Entity findTarget(ServerPlayer player, double range) {
		Vec3 eye = player.getEyePosition();
		Vec3 end = eye.add(player.getLookAngle().scale(range));
		HitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		if (block.getType() != HitResult.Type.MISS) {
			end = block.getLocation();
		}
		AABB search = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
		EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, search,
				entity -> entity instanceof LivingEntity && !(entity instanceof Player) && entity.isAlive() && !entity.isSpectator(),
				eye.distanceToSqr(end));
		return hit == null ? null : hit.getEntity();
	}

	private static void grab(ServerPlayer player, Entity target) {
		double distance = Math.clamp(player.getEyePosition().distanceTo(target.position()), MIN_HOLD_DISTANCE, MAX_HOLD_DISTANCE);
		boolean hadNoAi = target instanceof Mob mob && mob.isNoAi();
		boolean hadNoGravity = target.isNoGravity();
		if (target instanceof Mob mob) {
			mob.setNoAi(true);
		}
		target.setNoGravity(true);
		target.stopRiding();
		HOLDS.put(player.getUUID(), new Hold(target, distance, hadNoAi, hadNoGravity, 0));
		ServerLevel level = player.level();
		level.playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.GLOVE_GRAB, SoundSource.PLAYERS, 1.0F, 1.0F);
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.5), target.getZ(), 30, 0.4, 0.5, 0.4, 0.1);
		player.swing(InteractionHand.OFF_HAND, true);
	}

	private static void throwHeld(ServerPlayer player) {
		Hold hold = HOLDS.get(player.getUUID());
		Entity entity = hold.entity();
		release(player.getUUID());
		if (!entity.isAlive()) {
			return;
		}
		double strength = ConfigManager.get().glove.throwStrength;
		Vec3 velocity = player.getLookAngle().scale(strength).add(0.0, 0.15, 0.0);
		entity.setDeltaMovement(velocity);
		entity.hurtMarked = true;
		THROWN.put(entity.getUUID(), new Thrown(entity, player.getUUID(), THROW_TRACK_TICKS, velocity.length()));
		player.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.GLOVE_THROW, SoundSource.PLAYERS, 1.2F, 1.0F);
		player.swing(InteractionHand.OFF_HAND, true);
	}

	/** Lets go of whatever the player holds (no throw) and starts the cooldown. */
	private static void release(UUID playerId) {
		Hold hold = HOLDS.remove(playerId);
		if (hold == null) {
			return;
		}
		Entity entity = hold.entity();
		if (entity instanceof Mob mob) {
			mob.setNoAi(hold.hadNoAi());
		}
		entity.setNoGravity(hold.hadNoGravity());
		entity.resetFallDistance();
		if (entity.level().getServer() != null) {
			ServerPlayer player = entity.level().getServer().getPlayerList().getPlayer(playerId);
			if (player != null) {
				ItemStack glove = player.getOffhandItem().is(ModItems.TELEKINESIS_GLOVE) ? player.getOffhandItem() : new ItemStack(ModItems.TELEKINESIS_GLOVE);
				player.getCooldowns().addCooldown(glove, ConfigManager.get().glove.cooldownSeconds * 20);
			}
		}
	}

	private static void tick(MinecraftServer server) {
		ModConfig.Glove config = ConfigManager.get().glove;
		List<UUID> dropped = new ArrayList<>();
		for (Map.Entry<UUID, Hold> entry : HOLDS.entrySet()) {
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			Hold hold = entry.getValue();
			Entity entity = hold.entity();
			boolean valid = player != null && player.isAlive() && entity.isAlive() && !entity.isRemoved()
					&& entity.level() == player.level() && player.getOffhandItem().is(ModItems.TELEKINESIS_GLOVE)
					&& hold.ticks() < config.maxHoldSeconds * 20
					&& entity.distanceTo(player) <= config.range + LEASH_SLACK;
			if (!valid) {
				dropped.add(entry.getKey());
				continue;
			}
			Vec3 anchor = player.getEyePosition().add(player.getLookAngle().scale(hold.distance()))
					.subtract(0.0, entity.getBbHeight() / 2.0, 0.0);
			Vec3 step = anchor.subtract(entity.position());
			if (step.length() > MAX_STEP) {
				step = step.normalize().scale(MAX_STEP);
			}
			entity.setDeltaMovement(Vec3.ZERO);
			entity.move(MoverType.SELF, step);
			entity.resetFallDistance();
			if (hold.ticks() % 3 == 0) {
				player.level().sendParticles(AURA, entity.getX(), entity.getY(0.5), entity.getZ(), 4, entity.getBbWidth() / 2.0, entity.getBbHeight() / 2.0, entity.getBbWidth() / 2.0, 0.0);
			}
			entry.setValue(hold.aged());
		}
		dropped.forEach(Telekinesis::release);

		Iterator<Map.Entry<UUID, Thrown>> thrown = THROWN.entrySet().iterator();
		while (thrown.hasNext()) {
			Map.Entry<UUID, Thrown> entry = thrown.next();
			Thrown flight = entry.getValue();
			Entity entity = flight.entity();
			if (!entity.isAlive() || flight.ticksLeft() <= 0) {
				thrown.remove();
				continue;
			}
			boolean slammed = entity.horizontalCollision || (entity.verticalCollision && !entity.verticalCollisionBelow);
			if (slammed && flight.lastSpeed() >= IMPACT_MIN_SPEED && entity.level() instanceof ServerLevel level) {
				ServerPlayer thrower = flight.thrower() == null ? null : server.getPlayerList().getPlayer(flight.thrower());
				float damage = (float) (flight.lastSpeed() * config.impactDamage);
				entity.hurtServer(level, thrower != null ? level.damageSources().playerAttack(thrower) : level.damageSources().flyIntoWall(), damage);
				level.sendParticles(ParticleTypes.CRIT, entity.getX(), entity.getY(0.5), entity.getZ(), 20, 0.3, 0.3, 0.3, 0.3);
				thrown.remove();
				continue;
			}
			entry.setValue(new Thrown(entity, flight.thrower(), flight.ticksLeft() - 1, entity.getDeltaMovement().length()));
		}
	}

	private static void actionBar(ServerPlayer player, Component message) {
		player.sendSystemMessage(message, true);
	}
}
