package dev.mehbur.garipseyler.item;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.config.ModConfig;
import dev.mehbur.garipseyler.effect.ModSounds;
import dev.mehbur.garipseyler.entity.mindflayer.MindFlayer;
import dev.mehbur.garipseyler.entity.vecna.Vecna;
import dev.mehbur.garipseyler.network.BoneBreakPayload;
import dev.mehbur.garipseyler.network.TelekinesisPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
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
 * The Telekinesis Glove's power, server-authoritative. With a glove in the offhand, the key (V) grabs
 * the mob the player looks at (within the tier's range); it then floats in front of the player and follows
 * their gaze. Pressing again throws it where the player looks; a thrown mob that slams into something
 * takes impact damage. Which mobs can be grabbed depends on the {@link GloveTier}; players never. Strong
 * (high max health) mobs fly slower and so not as far. The cooldown starts when the mob leaves the glove.
 * <p>
 * With no mob in sight, the glove tears the looked-at block out as a vanilla falling block: it lands and is
 * placed again (or drops as an item), so nothing is lost or duplicated. A thrown block hurts every living
 * entity it flies into (attributed to the thrower, never hitting them), then drops.
 * <p>
 * The bone-break key (001 and 011 only) cracks the bones of the mob the glove holds in the air: 001 halves its
 * health (a boss loses a quarter), 011 kills it (a boss loses half). Bosses are the {@link #IMMUNE} tag, the ones
 * a plain glove cannot lift. A thrown Vecna catches himself with his own telekinesis and glides down before landing.
 */
public final class Telekinesis {
	public static final TagKey<EntityType<?>> IMMUNE = TagKey.create(Registries.ENTITY_TYPE, GaripSeylerMod.id("telekinesis_immune"));
	private static final double MIN_HOLD_DISTANCE = 2.5;
	private static final double MAX_HOLD_DISTANCE = 6.0;
	private static final double MAX_STEP = 1.5;
	private static final double LEASH_SLACK = 4.0;
	private static final int THROW_TRACK_TICKS = 40;
	/** Vecna is followed until he lands, so his glide can kick in at the end of a long flight. */
	private static final int VECNA_TRACK_TICKS = 400;
	private static final double IMPACT_MIN_SPEED = 0.6;
	private static final double MIN_WEIGHT_FACTOR = 0.2;
	/** Vecna starts gliding when he falls faster than this with ground this close below. */
	private static final double GLIDE_TRIGGER_SPEED = -0.25;
	private static final double GLIDE_GROUND_DISTANCE = 4.0;
	private static final double GLIDE_FALL_SPEED = -0.12;
	private static final DustParticleOptions AURA = new DustParticleOptions(0xB0206A, 1.0F);

	private record Hold(Entity entity, double distance, boolean hadNoAi, boolean hadNoGravity, int ticks) {
		Hold aged() {
			return new Hold(this.entity, this.distance, this.hadNoAi, this.hadNoGravity, this.ticks + 1);
		}
	}

	private record Thrown(Entity entity, @Nullable UUID thrower, int ticksLeft, double lastSpeed) {
		Thrown next() {
			return new Thrown(this.entity, this.thrower, this.ticksLeft - 1, this.entity.getDeltaMovement().length());
		}
	}

	private static final Map<UUID, Hold> HOLDS = new HashMap<>();
	private static final Map<UUID, Thrown> THROWN = new HashMap<>();
	/** Game time at which each player may break bones again. */
	private static final Map<UUID, Long> BONE_BREAK_READY = new HashMap<>();

	private Telekinesis() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(TelekinesisPayload.TYPE, (payload, context) -> onKey(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(BoneBreakPayload.TYPE, (payload, context) -> onBoneBreak(context.player()));
		ServerTickEvents.END_SERVER_TICK.register(Telekinesis::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> release(listener.getPlayer().getUUID()));
		// Never save a mob with the glove's NoAI / NoGravity still applied.
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			for (UUID player : HOLDS.keySet().toArray(UUID[]::new)) {
				release(player);
			}
			THROWN.clear();
			BONE_BREAK_READY.clear();
		});
	}

	public static boolean isHolding(ServerPlayer player) {
		return HOLDS.containsKey(player.getUUID());
	}

	/** Whether some player's glove holds this entity right now. */
	public static boolean isHeld(Entity entity) {
		for (Hold hold : HOLDS.values()) {
			if (hold.entity() == entity) {
				return true;
			}
		}
		return false;
	}

	/** The tier of the glove in the player's offhand; warns (and returns null) when it is in the main hand. */
	private static @Nullable GloveTier offhandTier(ServerPlayer player) {
		GloveTier tier = GloveTier.of(player.getOffhandItem());
		if (tier == null && GloveTier.of(player.getMainHandItem()) != null) {
			actionBar(player, Component.translatable("message.garipseylermc.glove.offhand_only").withStyle(ChatFormatting.GRAY));
		}
		return tier;
	}

	private static void onKey(ServerPlayer player) {
		if (HOLDS.containsKey(player.getUUID())) {
			throwHeld(player);
			return;
		}
		if (!player.isAlive() || player.isSpectator()) {
			return;
		}
		GloveTier tier = offhandTier(player);
		if (tier == null) {
			return;
		}
		ItemStack offhand = player.getOffhandItem();
		if (player.getCooldowns().isOnCooldown(offhand)) {
			actionBar(player, Component.translatable("message.garipseylermc.glove.cooldown").withStyle(ChatFormatting.GRAY));
			return;
		}
		ModConfig.Glove config = ConfigManager.get().glove;
		Entity target = findTarget(player, tier.range(config));
		if (target == null) {
			if (config.grabBlocks) {
				grabBlock(player, tier.range(config));
			}
			return;
		}
		if (!canAffect(tier, target)) {
			resists(player, target);
			return;
		}
		if (isHeld(target)) {
			return;
		}
		grab(player, target);
	}

	/** A Vecna still rising out of the ground cannot be touched yet. */
	private static boolean canAffect(GloveTier tier, Entity target) {
		return tier.canGrab(target) && !(target instanceof Vecna vecna && vecna.isEmerging());
	}

	private static void resists(ServerPlayer player, Entity target) {
		actionBar(player, Component.translatable("message.garipseylermc.glove.immune", target.getDisplayName()).withStyle(ChatFormatting.DARK_RED));
		player.level().sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(0.6), target.getZ(), 15, 0.3, 0.4, 0.3, 0.02);
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
		// Big mobs float further out so they do not swallow the player.
		double clearance = target.getBbWidth() / 2.0;
		double distance = Math.clamp(player.getEyePosition().distanceTo(target.position()), MIN_HOLD_DISTANCE + clearance, MAX_HOLD_DISTANCE + clearance);
		boolean hadNoAi = target instanceof Mob mob && mob.isNoAi();
		boolean hadNoGravity = target.isNoGravity();
		if (target instanceof Mob mob) {
			mob.setNoAi(true);
		}
		target.setNoGravity(true);
		target.stopRiding();
		HOLDS.put(player.getUUID(), new Hold(target, distance, hadNoAi, hadNoGravity, 0));
		grabEffects(player, target);
	}

	/** Tears the looked-at block out of the world and holds it as a falling block. */
	private static void grabBlock(ServerPlayer player, double range) {
		ServerLevel level = player.level();
		Vec3 eye = player.getEyePosition();
		BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(range)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		if (hit.getType() != HitResult.Type.BLOCK) {
			return;
		}
		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		// Grass, fire, snow layers and other replaceable blocks are not worth lifting.
		if (state.isAir() || state.canBeReplaced()) {
			return;
		}
		// Spawn protection, adventure mode and claim mods (through Fabric's break event) apply as if mining it.
		if (!player.mayInteract(level, pos) || player.blockActionRestricted(level, pos, player.gameMode())
				|| !PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, state, null)) {
			return;
		}
		if (!canLift(level, pos, state)) {
			actionBar(player, Component.translatable("message.garipseylermc.glove.immovable", state.getBlock().getName()).withStyle(ChatFormatting.GRAY));
			level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 0.3, 0.3, 0.3, 0.02);
			return;
		}
		FallingBlockEntity block = FallingBlockEntity.fall(level, pos, state);
		block.setNoGravity(true);
		double distance = Math.clamp(eye.distanceTo(block.position()), MIN_HOLD_DISTANCE, MAX_HOLD_DISTANCE);
		HOLDS.put(player.getUUID(), new Hold(block, distance, false, false, 0));
		grabEffects(player, block);
	}

	/**
	 * Unbreakable blocks, blocks with contents (chests, furnaces, signs...), two-part blocks (doors, beds,
	 * tall plants) and extended pistons stay put: lifting them would lose data or tear the other half.
	 */
	private static boolean canLift(ServerLevel level, BlockPos pos, BlockState state) {
		return state.getDestroySpeed(level, pos) >= 0.0F
				&& !state.hasBlockEntity()
				&& !state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
				&& !state.hasProperty(BlockStateProperties.BED_PART)
				&& !(state.hasProperty(BlockStateProperties.EXTENDED) && state.getValue(BlockStateProperties.EXTENDED))
				&& !state.is(Blocks.PISTON_HEAD)
				&& !state.is(Blocks.MOVING_PISTON);
	}

	private static void grabEffects(ServerPlayer player, Entity target) {
		ServerLevel level = player.level();
		level.playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.GLOVE_GRAB, SoundSource.PLAYERS, 1.0F, 1.0F);
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.5), target.getZ(), 30, 0.4, 0.5, 0.4, 0.1);
		player.swing(InteractionHand.OFF_HAND, true);
	}

	private static void throwHeld(ServerPlayer player) {
		Hold hold = HOLDS.get(player.getUUID());
		Entity entity = hold.entity();
		GloveTier tier = GloveTier.of(player.getOffhandItem());
		release(player.getUUID());
		if (!entity.isAlive()) {
			return;
		}
		ModConfig.Glove config = ConfigManager.get().glove;
		double strength = (tier == null ? GloveTier.BASIC : tier).throwStrength(config) * weightFactor(entity, config);
		Vec3 velocity = player.getLookAngle().scale(strength).add(0.0, 0.15, 0.0);
		entity.setDeltaMovement(velocity);
		entity.hurtMarked = true;
		int track = entity instanceof Vecna ? VECNA_TRACK_TICKS : THROW_TRACK_TICKS;
		THROWN.put(entity.getUUID(), new Thrown(entity, player.getUUID(), track, velocity.length()));
		player.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.GLOVE_THROW, SoundSource.PLAYERS, 1.2F, 1.0F);
		player.swing(InteractionHand.OFF_HAND, true);
	}

	/** Heavier (higher max health) mobs leave the glove slower; blocks and light mobs at full speed. */
	private static double weightFactor(Entity entity, ModConfig.Glove config) {
		if (!(entity instanceof LivingEntity living)) {
			return 1.0;
		}
		return Mth.clamp(Math.sqrt(config.throwWeightHealth / Math.max(1.0F, living.getMaxHealth())), MIN_WEIGHT_FACTOR, 1.0);
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
				ItemStack glove = GloveTier.of(player.getOffhandItem()) != null ? player.getOffhandItem() : new ItemStack(ModItems.TELEKINESIS_GLOVE);
				player.getCooldowns().addCooldown(glove, ConfigManager.get().glove.cooldownSeconds * 20);
			}
		}
	}

	// ---------------------------------------------------------------- bone break

	private static void onBoneBreak(ServerPlayer player) {
		if (!player.isAlive() || player.isSpectator()) {
			return;
		}
		GloveTier tier = offhandTier(player);
		if (tier == null) {
			return;
		}
		if (!tier.breaksBones()) {
			actionBar(player, Component.translatable("message.garipseylermc.glove.bone_break_tier").withStyle(ChatFormatting.GRAY));
			return;
		}
		ServerLevel level = player.level();
		long now = level.getGameTime();
		if (BONE_BREAK_READY.getOrDefault(player.getUUID(), 0L) > now) {
			actionBar(player, Component.translatable("message.garipseylermc.glove.bone_break_cooldown").withStyle(ChatFormatting.GRAY));
			return;
		}
		Hold hold = HOLDS.get(player.getUUID());
		if (hold == null || !(hold.entity() instanceof LivingEntity victim) || !victim.isAlive()) {
			actionBar(player, Component.translatable("message.garipseylermc.glove.bone_break_hold").withStyle(ChatFormatting.GRAY));
			return;
		}
		BONE_BREAK_READY.put(player.getUUID(), now + ConfigManager.get().glove.boneBreakCooldownSeconds * 20L);
		player.swing(InteractionHand.OFF_HAND, true);
		level.playSound(null, victim.getX(), victim.getY(0.5), victim.getZ(), ModSounds.GLOVE_BONE_BREAK, SoundSource.PLAYERS, 1.6F, 1.0F);
		level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.BONE), victim.getX(), victim.getY(0.6), victim.getZ(),
				24, victim.getBbWidth() / 3.0, victim.getBbHeight() / 4.0, victim.getBbWidth() / 3.0, 0.15);
		level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, victim.getX(), victim.getY(0.6), victim.getZ(), 10, 0.3, 0.4, 0.3, 0.1);

		DamageSource source = level.damageSources().indirectMagic(player, player);
		victim.setLastHurtByPlayer(player, 100);
		boolean boss = victim.is(IMMUNE);
		if (tier == GloveTier.TIER_011 && !boss) {
			kill(level, victim, source);
			return;
		}
		// An exact share of the current health, whatever armor or resistance the mob has: 001 takes half from
		// a mob and a quarter from a boss, 011 half from a boss. Bosses never die from one bone break.
		float keep = tier == GloveTier.TIER_001 && boss ? 0.75F : 0.5F;
		victim.setHealth(victim.getHealth() * keep);
		victim.setLastHurtByMob(player);
		level.broadcastDamageEvent(victim, source);
		if (victim instanceof MindFlayer mindFlayer) {
			mindFlayer.resetRegenDelay();
		}
	}

	/** 011 on a non-boss: the mob dies, even one that shrugs off normal damage (attributed to the player for loot). */
	private static void kill(ServerLevel level, LivingEntity victim, DamageSource source) {
		victim.hurtServer(level, source, Float.MAX_VALUE);
		if (victim.isAlive()) {
			victim.kill(level);
		}
		if (victim.isAlive()) {
			victim.setHealth(0.0F);
			victim.die(source);
		}
	}

	// ---------------------------------------------------------------- tick

	private static void tick(MinecraftServer server) {
		ModConfig.Glove config = ConfigManager.get().glove;
		List<UUID> dropped = new ArrayList<>();
		for (Map.Entry<UUID, Hold> entry : HOLDS.entrySet()) {
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			Hold hold = entry.getValue();
			Entity entity = hold.entity();
			GloveTier tier = player == null ? null : GloveTier.of(player.getOffhandItem());
			boolean valid = player != null && player.isAlive() && entity.isAlive() && !entity.isRemoved()
					&& entity.level() == player.level() && tier != null
					&& hold.ticks() < config.maxHoldSeconds * 20
					&& entity.distanceTo(player) <= tier.range(config) + LEASH_SLACK + entity.getBbWidth() / 2.0;
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
			if (entity instanceof FallingBlockEntity block) {
				// A held block skips its own tick (FallingBlockEntityMixin) so it never lands; clients still move
				// it by its velocity, which keeps it smooth on screen despite its slow position updates.
				block.setDeltaMovement(step);
				block.move(MoverType.SELF, step);
				block.hurtMarked = true;
			} else {
				entity.setDeltaMovement(Vec3.ZERO);
				entity.move(MoverType.SELF, step);
			}
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
			if (!entity.isAlive() || flight.ticksLeft() <= 0 || !(entity.level() instanceof ServerLevel level)) {
				thrown.remove();
				continue;
			}
			if (entity instanceof FallingBlockEntity block) {
				if (hitWithBlock(server, level, block, flight, config)) {
					thrown.remove();
				} else {
					entry.setValue(flight.next());
				}
				continue;
			}
			boolean slammed = entity.horizontalCollision || (entity.verticalCollision && !entity.verticalCollisionBelow);
			if (slammed && flight.lastSpeed() >= IMPACT_MIN_SPEED) {
				ServerPlayer thrower = flight.thrower() == null ? null : server.getPlayerList().getPlayer(flight.thrower());
				float damage = (float) (flight.lastSpeed() * config.impactDamage);
				entity.hurtServer(level, thrower != null ? level.damageSources().playerAttack(thrower) : level.damageSources().flyIntoWall(), damage);
				level.sendParticles(ParticleTypes.CRIT, entity.getX(), entity.getY(0.5), entity.getZ(), 20, 0.3, 0.3, 0.3, 0.3);
				if (!(entity instanceof Vecna)) {
					thrown.remove();
					continue;
				}
			}
			if (entity instanceof Vecna vecna) {
				if (vecna.onGround()) {
					thrown.remove();
					continue;
				}
				glide(level, vecna);
			}
			entry.setValue(flight.next());
		}
	}

	/** Vecna's own telekinesis: falling fast with the ground close below, he slows down and floats to the ground. */
	private static void glide(ServerLevel level, Vecna vecna) {
		Vec3 motion = vecna.getDeltaMovement();
		if (motion.y > GLIDE_TRIGGER_SPEED) {
			return;
		}
		Vec3 feet = vecna.position();
		BlockHitResult ground = level.clip(new ClipContext(feet, feet.add(motion.x, -GLIDE_GROUND_DISTANCE, motion.z),
				ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, vecna));
		if (ground.getType() == HitResult.Type.MISS) {
			return;
		}
		vecna.setDeltaMovement(motion.x * 0.5, GLIDE_FALL_SPEED, motion.z * 0.5);
		vecna.hurtMarked = true;
		vecna.resetFallDistance();
		level.sendParticles(AURA, vecna.getX(), vecna.getY(0.3), vecna.getZ(), 12, 0.6, 0.6, 0.6, 0.0);
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, vecna.getX(), vecna.getY(), vecna.getZ(), 10, 0.5, 0.1, 0.5, 0.05);
	}

	/**
	 * Damages every living entity in the thrown block's path (the thrower and held mobs excepted). Looks one
	 * tick ahead along its velocity so a fast block cannot pass through someone between ticks.
	 */
	private static boolean hitWithBlock(MinecraftServer server, ServerLevel level, FallingBlockEntity block, Thrown flight, ModConfig.Glove config) {
		if (flight.lastSpeed() < IMPACT_MIN_SPEED) {
			return false;
		}
		ServerPlayer thrower = flight.thrower() == null ? null : server.getPlayerList().getPlayer(flight.thrower());
		Vec3 motion = block.getDeltaMovement();
		AABB path = block.getBoundingBox().expandTowards(motion).inflate(0.25);
		List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, path, victim -> victim.isAlive() && !victim.isSpectator()
				&& victim != thrower && !isHeld(victim));
		if (victims.isEmpty()) {
			return false;
		}
		float damage = (float) (flight.lastSpeed() * config.blockHitDamage);
		for (LivingEntity victim : victims) {
			victim.hurtServer(level, level.damageSources().thrown(block, thrower), damage);
		}
		// The block bounces back a little and drops in front of the victim (not inside it), where it lands and is
		// placed like any falling block.
		block.setDeltaMovement(motion.multiply(-0.15, 0.0, -0.15));
		block.hurtMarked = true;
		BlockState state = block.getBlockState();
		level.playSound(null, block.getX(), block.getY(), block.getZ(), state.getSoundType().getBreakSound(), SoundSource.PLAYERS, 1.2F, 0.8F);
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), block.getX(), block.getY(0.5), block.getZ(), 30, 0.4, 0.4, 0.4, 0.15);
		return true;
	}

	private static void actionBar(ServerPlayer player, Component message) {
		player.sendSystemMessage(message, true);
	}
}
