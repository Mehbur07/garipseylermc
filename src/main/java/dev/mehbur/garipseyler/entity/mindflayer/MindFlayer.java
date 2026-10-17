package dev.mehbur.garipseyler.entity.mindflayer;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.constant.DefaultAnimations;
import com.geckolib.util.GeckoLibUtil;
import dev.mehbur.garipseyler.block.ModBlocks;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.config.ModConfig;
import dev.mehbur.garipseyler.effect.FireAspectBonus;
import dev.mehbur.garipseyler.effect.ModSounds;
import dev.mehbur.garipseyler.entity.ModEntities;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The Mind Flayer in its stone form; replaces the Ender Dragon in the End (see {@code EnderDragonFightMixin}).
 * A giant (model about 49 blocks tall, taller than the End spikes; hitbox 16x44 around its body, clickable 5 blocks beyond that) that cannot
 * fly and fights on the ground with melee hits strong enough to one-shot an Iron Golem and the attacks in
 * {@link MindFlayerAttack}. It walks with {@link GiantMovement} instead of vanilla path finding. End crystals
 * do not heal it; instead it regenerates on its own after {@code regenDelaySeconds} without damage, from 0 to full
 * in {@code fullRegenSeconds}. Its death keeps the vanilla ending: exit portal, a new gateway, the egg and the XP.
 */
public class MindFlayer extends Monster implements GeoEntity {
	public static final String ACTIONS_CONTROLLER = "actions";
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
	private static final RawAnimation STOMP = RawAnimation.begin().thenPlay("attack.stomp");
	private static final RawAnimation THROW = RawAnimation.begin().thenPlay("attack.throw");
	private static final RawAnimation ROAR = RawAnimation.begin().thenPlay("attack.roar");
	private static final RawAnimation SWING = RawAnimation.begin().thenPlay("attack.swing");

	/** Length of the crumbling death; XP drops during its second half, like the dragon's. */
	public static final int DEATH_TICKS = 100;
	private static final int FIRST_KILL_XP = 12000;
	private static final int LATER_KILL_XP = 500;
	private static final double ATTACK_RANGE = 64.0;
	private static final double WAVE_SPEED = 0.9;
	private static final double ROCK_SPEED = 1.3;
	/** Height above the feet where rocks leave its claws. */
	private static final double ROCK_RELEASE_HEIGHT = 34.0;
	private static final float PICK_MARGIN = 5.0F;
	private static final DustParticleOptions REGEN_PARTICLE = new DustParticleOptions(0xE0601A, 3.0F);

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
	private final Map<MindFlayerAttack, Long> readyAt = new EnumMap<>(MindFlayerAttack.class);
	private final IntSet waveHit = new IntOpenHashSet();
	private int attackCooldown = 60;
	private @Nullable MindFlayerAttack attack;
	private int attackTicks;
	private int ticksSinceDamage;
	private int spawnRoarTicks;
	private boolean phaseTwoAnnounced;
	private double waveRadius = -1.0;
	private Vec3 waveCenter = Vec3.ZERO;

	public MindFlayer(EntityType<? extends MindFlayer> type, Level level) {
		super(type, level);
		this.xpReward = 0;
		this.setPersistenceRequired();
		this.moveControl = new GiantMovement.GiantMoveControl(this);
	}

	public static AttributeSupplier.Builder createAttributes() {
		ModConfig.MindFlayer config = ConfigManager.get().mindFlayer;
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, config.maxHealth)
				.add(Attributes.ATTACK_DAMAGE, config.attackDamage)
				.add(Attributes.ATTACK_KNOCKBACK, 3.0)
				.add(Attributes.ARMOR, config.armor)
				.add(Attributes.ARMOR_TOUGHNESS, 4.0)
				.add(Attributes.MOVEMENT_SPEED, 0.32)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(Attributes.FOLLOW_RANGE, 96.0)
				.add(Attributes.STEP_HEIGHT, 6.0);
	}

	/**
	 * Spawns the fight's Mind Flayer on the ground 18–30 blocks from the End's center (never on the exit portal),
	 * with the arena as its home. It roars shortly after appearing.
	 */
	public static @Nullable MindFlayer spawnForFight(ServerLevel level, BlockPos origin) {
		RandomSource random = level.getRandom();
		BlockPos spot = null;
		for (int attempt = 0; attempt < 16 && spot == null; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double distance = 18.0 + random.nextDouble() * 12.0;
			BlockPos column = new BlockPos(origin.getX() + Mth.floor(Math.cos(angle) * distance), 0, origin.getZ() + Mth.floor(Math.sin(angle) * distance));
			level.getChunkAt(column);
			BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
			if (top.getY() > level.getMinY() + 8 && !level.getBlockState(top.below()).isAir()) {
				spot = top;
			}
		}
		BlockPos center = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin);
		if (spot == null) {
			spot = center.above(2);
		}
		MindFlayer flayer = ModEntities.MIND_FLAYER.create(level, EntitySpawnReason.EVENT);
		if (flayer == null) {
			return null;
		}
		flayer.snapTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
		flayer.setHomeTo(center, ConfigManager.get().mindFlayer.arenaRadius);
		flayer.spawnRoarTicks = 30;
		level.addFreshEntity(flayer);
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(spot.below())),
				flayer.getX(), flayer.getY() + 0.2, flayer.getZ(), 400, 8.0, 0.5, 8.0, 0.3);
		return flayer;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new GiantMovement.ChaseGoal(this));
		this.goalSelector.addGoal(7, new GiantMovement.WanderGoal(this));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 48.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, false));
	}

	/** True while a special attack plays; it stands still then. */
	public boolean isBusy() {
		return this.attack != null;
	}

	// ---------------------------------------------------------------- combat loop

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		this.ticksSinceDamage++;
		this.tickRegeneration(level);
		this.tickFight(level);
		this.tickWave(level);
		if (this.spawnRoarTicks > 0 && --this.spawnRoarTicks == 0) {
			this.triggerAnim(ACTIONS_CONTROLLER, MindFlayerAttack.ROAR.animation);
			this.playSound(ModSounds.MIND_FLAYER_ROAR.value(), 5.0F, 0.8F);
		}

		if (this.attack != null) {
			this.tickAttack(level);
			return;
		}
		if (--this.attackCooldown > 0) {
			return;
		}
		if (!(this.getTarget() instanceof LivingEntity player) || player instanceof ServerPlayer serverPlayer && !canAffect(serverPlayer)
				|| this.distanceToSqr(player) > ATTACK_RANGE * ATTACK_RANGE || !this.hasLineOfSight(player)) {
			return;
		}
		MindFlayerAttack picked = this.pickAttack(level, player);
		if (picked != null) {
			this.start(level, picked);
		}
		int cooldown = ConfigManager.get().mindFlayer.attackCooldownSeconds * 20;
		this.attackCooldown = this.isPhaseTwo() ? Math.round(cooldown * 0.7F) : cooldown;
	}

	/** Starts an attack right away on the given target; for admin commands. */
	public void forceAttack(MindFlayerAttack forced, LivingEntity target) {
		if (this.level() instanceof ServerLevel level) {
			this.setTarget(target);
			this.start(level, forced);
		}
	}

	public boolean isPhaseTwo() {
		return this.getHealth() < this.getMaxHealth() * 0.5F;
	}

	public boolean isRegenerating() {
		return this.isAlive() && this.getHealth() < this.getMaxHealth()
				&& this.ticksSinceDamage >= ConfigManager.get().mindFlayer.regenDelaySeconds * 20;
	}

	public int ticksSinceDamage() {
		return this.ticksSinceDamage;
	}

	private @Nullable MindFlayerAttack pickAttack(ServerLevel level, LivingEntity player) {
		ModConfig.MindFlayer config = ConfigManager.get().mindFlayer;
		double distance = this.distanceTo(player);
		List<MindFlayerAttack> candidates = new ArrayList<>();
		int totalWeight = 0;
		for (MindFlayerAttack candidate : MindFlayerAttack.values()) {
			if (this.readyAt.getOrDefault(candidate, 0L) > level.getGameTime()) {
				continue;
			}
			boolean usable = switch (candidate) {
				case STOMP -> distance <= config.stompRadius * 0.75;
				case ROCK -> distance >= 10.0;
				case ROAR -> distance <= config.roarRadius && !player.hasEffect(MobEffects.SLOWNESS);
			};
			if (usable) {
				candidates.add(candidate);
				totalWeight += candidate.weight;
			}
		}
		if (totalWeight == 0) {
			return null;
		}
		int roll = this.random.nextInt(totalWeight);
		for (MindFlayerAttack candidate : candidates) {
			roll -= candidate.weight;
			if (roll < 0) {
				return candidate;
			}
		}
		return null;
	}

	private void start(ServerLevel level, MindFlayerAttack started) {
		this.attack = started;
		this.attackTicks = 0;
		this.readyAt.put(started, level.getGameTime() + started.cooldownSeconds * 20L);
		this.triggerAnim(ACTIONS_CONTROLLER, started.animation);
	}

	private void tickAttack(ServerLevel level) {
		LivingEntity target = this.getTarget();
		if (target != null) {
			this.getLookControl().setLookAt(target, 30.0F, 30.0F);
		}
		if (++this.attackTicks == this.attack.impactTick) {
			this.perform(level, this.attack, target);
		}
		if (this.attackTicks >= this.attack.durationTicks) {
			this.attack = null;
		}
	}

	private void perform(ServerLevel level, MindFlayerAttack performed, @Nullable LivingEntity target) {
		ModConfig.MindFlayer config = ConfigManager.get().mindFlayer;
		switch (performed) {
			case STOMP -> {
				level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.MIND_FLAYER_STOMP, SoundSource.HOSTILE, 3.0F, 1.0F);
				Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
				for (double reach : new double[] {6.0, 12.0}) {
					Vec3 front = this.position().add(forward.scale(reach));
					level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, front.x, front.y + 0.3, front.z, 1, 1.0, 0.0, 1.0, 0.0);
				}
				this.waveCenter = this.position();
				this.waveRadius = 1.5;
				this.waveHit.clear();
			}
			case ROCK -> {
				if (target == null || !target.isAlive()) {
					return;
				}
				level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.MIND_FLAYER_THROW, SoundSource.HOSTILE, 2.0F, 1.0F);
				int count = this.isPhaseTwo() ? 3 : 1;
				for (int i = 0; i < count; i++) {
					this.throwRock(level, target, i);
				}
			}
			case ROAR -> {
				level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.MIND_FLAYER_ROAR, SoundSource.HOSTILE, 4.0F, 1.0F);
				Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
				Vec3 mouth = this.position().add(forward.scale(20.0)).add(0.0, 24.0, 0.0);
				for (int i = 0; i < 6; i++) {
					Vec3 boom = mouth.add(forward.scale(i * 3.0)).add(0.0, -i * 3.5, 0.0);
					level.sendParticles(ParticleTypes.SONIC_BOOM, boom.x, boom.y, boom.z, 1, 0.0, 0.0, 0.0, 0.0);
				}
				level.sendParticles(ParticleTypes.POOF, mouth.x, mouth.y, mouth.z, 200, 5.0, 3.0, 5.0, 0.4);
				double radius = config.roarRadius;
				for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(radius),
						player -> player instanceof ServerPlayer serverPlayer && canAffect(serverPlayer) && player.distanceTo(this) <= radius)) {
					if (config.roarSlownessSeconds > 0) {
						player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, config.roarSlownessSeconds * 20, 2), this);
					}
					Vec3 away = horizontalDirection(this.position(), player.position());
					player.push(away.x * 0.6, 0.2, away.z * 0.6);
					player.hurtMarked = true;
				}
			}
		}
	}

	/**
	 * Throws a rock so that it lands on the target (the first one leads a moving target); extra rocks land up to
	 * three blocks to the sides. Speed is solved for the projectile's own gravity and air drag.
	 */
	private void throwRock(ServerLevel level, LivingEntity target, int index) {
		Vec3 forward = Vec3.directionFromRotation(0.0F, this.yBodyRot);
		Vec3 from = this.position().add(forward.scale(10.0)).add(0.0, ROCK_RELEASE_HEIGHT, 0.0);
		Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.4, 0.0);
		double distance = Math.sqrt(Mth.square(aim.x - from.x) + Mth.square(aim.z - from.z));
		int ticks = Mth.clamp((int) (distance / ROCK_SPEED), 20, 60);
		if (index == 0) {
			Vec3 motion = target.getDeltaMovement();
			aim = aim.add(motion.x * ticks * 0.6, 0.0, motion.z * ticks * 0.6);
		} else {
			Vec3 side = new Vec3(-(aim.z - from.z), 0.0, aim.x - from.x).normalize();
			aim = aim.add(side.scale((index == 1 ? 1 : -1) * (3.0 + this.random.nextDouble() * 3.0)));
		}
		double dx = aim.x - from.x;
		double dz = aim.z - from.z;
		double horizontal = Math.max(Math.sqrt(dx * dx + dz * dz), 0.01);
		double drag = MindFlayerRock.AIR_DRAG;
		double sum = drag * (1.0 - Math.pow(drag, ticks)) / (1.0 - drag);
		double speed = horizontal / sum;
		double vertical = (aim.y - from.y + MindFlayerRock.GRAVITY * drag / (1.0 - drag) * (ticks - sum)) / sum;

		MindFlayerRock rock = new MindFlayerRock(level, this);
		rock.setPos(from);
		rock.setDeltaMovement(dx / horizontal * speed, vertical, dz / horizontal * speed);
		level.addFreshEntity(rock);
	}

	/**
	 * Ground slam: a ring that grows {@link #WAVE_SPEED} blocks per tick up to {@code stompRadius}. Whatever stands on
	 * the ground where the ring passes is hit once (less the farther out) and thrown outwards; jumping over it dodges.
	 */
	private void tickWave(ServerLevel level) {
		if (this.waveRadius < 0.0) {
			return;
		}
		ModConfig.MindFlayer config = ConfigManager.get().mindFlayer;
		double previous = this.waveRadius;
		this.waveRadius += WAVE_SPEED;
		double radius = this.waveRadius;

		int points = Math.max(8, (int) (radius * 4.0));
		for (int i = 0; i < points; i++) {
			double angle = Math.PI * 2.0 * i / points + this.random.nextDouble() * 0.1;
			double x = this.waveCenter.x + Math.cos(angle) * radius;
			double z = this.waveCenter.z + Math.sin(angle) * radius;
			BlockPos ground = BlockPos.containing(x, this.waveCenter.y - 0.5, z);
			for (int down = 0; down < 3 && level.getBlockState(ground).isAir(); down++) {
				ground = ground.below();
			}
			BlockState state = level.getBlockState(ground);
			if (!state.isAir()) {
				level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), x, ground.getY() + 1.05, z, 3, 0.15, 0.05, 0.15, 0.15);
			}
		}

		AABB area = new AABB(this.waveCenter, this.waveCenter).inflate(radius + 1.0, 2.5, radius + 1.0);
		for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area, living -> living != this && living.isAlive() && !living.is(FireAspectBonus.UPSIDE_DOWN_CREATURES)
				&& !this.waveHit.contains(living.getId()) && !(living instanceof Player player && (player.isCreative() || player.isSpectator())))) {
			double distance = Math.sqrt(Mth.square(living.getX() - this.waveCenter.x) + Mth.square(living.getZ() - this.waveCenter.z));
			double height = living.getY() - this.waveCenter.y;
			if (distance > radius + 0.3 || distance < previous - 0.6 || height > 1.2 || height < -2.5) {
				continue;
			}
			if (!living.onGround() && height > 0.25) {
				continue;
			}
			this.waveHit.add(living.getId());
			float damage = (float) (config.stompDamage * (1.0 - 0.5 * radius / config.stompRadius));
			living.hurtServer(level, this.damageSources().mobAttack(this), damage);
			Vec3 away = horizontalDirection(this.waveCenter, living.position());
			living.push(away.x * 1.4, 0.8, away.z * 1.4);
			living.hurtMarked = true;
		}
		if (this.waveRadius >= config.stompRadius) {
			this.waveRadius = -1.0;
		}
	}

	private static Vec3 horizontalDirection(Vec3 from, Vec3 to) {
		Vec3 flat = new Vec3(to.x - from.x, 0.0, to.z - from.z);
		return flat.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : flat.normalize();
	}

	private static boolean canAffect(ServerPlayer player) {
		return player.isAlive() && !player.isCreative() && !player.isSpectator();
	}

	// ---------------------------------------------------------------- regeneration, fight, arena

	/** No crystal healing: after {@code regenDelaySeconds} without damage it heals max/fullRegen HP per second. */
	private void tickRegeneration(ServerLevel level) {
		if (!this.isRegenerating()) {
			return;
		}
		ModConfig.MindFlayer config = ConfigManager.get().mindFlayer;
		if (this.ticksSinceDamage == config.regenDelaySeconds * 20) {
			level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.MIND_FLAYER_REGEN, SoundSource.HOSTILE, 2.0F, 1.0F);
		}
		this.setHealth(Math.min(this.getMaxHealth(), this.getHealth() + this.getMaxHealth() / (config.fullRegenSeconds * 20.0F)));
		if (this.tickCount % 4 == 0) {
			level.sendParticles(REGEN_PARTICLE, this.getRandomX(1.0), this.getY() + 22.0 + this.random.nextDouble() * 20.0, this.getRandomZ(1.0), 10, 3.0, 3.0, 3.0, 0.0);
		}
	}

	private void tickFight(ServerLevel level) {
		if (level.getDragonFight() instanceof MindFlayerFight fight) {
			fight.garipseylermc$updateMindFlayer(this);
			if (!this.hasHome()) {
				this.setHomeTo(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, fight.garipseylermc$origin()),
						ConfigManager.get().mindFlayer.arenaRadius);
			}
		}
		if (this.isPhaseTwo() && !this.phaseTwoAnnounced) {
			this.phaseTwoAnnounced = true;
			for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(96.0))) {
				player.sendSystemMessage(Component.translatable("message.garipseylermc.mind_flayer.phase_two").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
			}
			this.playSound(ModSounds.MIND_FLAYER_ROAR.value(), 5.0F, 0.7F);
			this.triggerAnim(ACTIONS_CONTROLLER, MindFlayerAttack.ROAR.animation);
		}
	}

	/** Falling into the void puts it back in the middle of the arena instead of killing it. */
	@Override
	protected void onBelowWorld() {
		if (this.level() instanceof ServerLevel level && this.hasHome()) {
			BlockPos home = this.getHomePosition();
			BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, home.offset(20, 0, 0));
			if (top.getY() > level.getMinY() + 8) {
				this.teleportTo(top.getX() + 0.5, top.getY(), top.getZ() + 0.5);
				this.setDeltaMovement(Vec3.ZERO);
				this.resetFallDistance();
				this.playSound(ModSounds.MIND_FLAYER_ROAR.value(), 5.0F, 0.6F);
				return;
			}
		}
		super.onBelowWorld();
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypeTags.IS_FALL)) {
			return false;
		}
		boolean hurt = super.hurtServer(level, source, damage);
		if (hurt) {
			this.ticksSinceDamage = 0;
		}
		return hurt;
	}

	// ---------------------------------------------------------------- death

	/** A 5 second crumble; then the fight opens the exit portal, adds a gateway and (first kill) places the egg. */
	@Override
	protected void tickDeath() {
		this.deathTime++;
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}
		EnderDragonFight fight = level.getDragonFight();
		if (fight instanceof MindFlayerFight flayerFight) {
			flayerFight.garipseylermc$updateMindFlayer(this);
		}
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.UPSIDE_DOWN_STONE.defaultBlockState()),
				this.getRandomX(2.0), this.getRandomY(), this.getRandomZ(2.0), 30, 3.0, 3.0, 3.0, 0.1);
		if (this.deathTime % 6 == 0) {
			level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getRandomX(2.0), this.getRandomY(), this.getRandomZ(2.0), 1, 0.0, 0.0, 0.0, 0.0);
			this.playSound(ModSounds.MIND_FLAYER_ROCK_IMPACT.value(), 2.0F, 0.6F + this.random.nextFloat() * 0.3F);
		}

		int xp = fight != null && fight.hasPreviouslyKilledDragon() ? LATER_KILL_XP : FIRST_KILL_XP;
		boolean drops = level.getGameRules().get(GameRules.MOB_DROPS);
		if (drops && this.deathTime > DEATH_TICKS / 2 && this.deathTime % 5 == 0) {
			ExperienceOrb.award(level, this.position(), Mth.floor(xp * 0.08F));
		}
		if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
			if (drops) {
				ExperienceOrb.award(level, this.position(), Mth.floor(xp * 0.2F));
			}
			if (fight instanceof MindFlayerFight flayerFight) {
				flayerFight.garipseylermc$setMindFlayerKilled(this);
			}
			this.remove(Entity.RemovalReason.KILLED);
		}
	}

	// ---------------------------------------------------------------- rules

	@Override
	public void checkDespawn() {
		// Never despawns, not even on Peaceful.
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	public boolean canUsePortal(boolean ignorePassenger) {
		return false;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
		return false;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	/** Other Upside Down creatures (Demogorgons, Demodogs, Demobats, Vecna) are on its side. */
	@Override
	protected boolean considersEntityAsAlly(Entity other) {
		return other.is(FireAspectBonus.UPSIDE_DOWN_CREATURES) || super.considersEntityAsAlly(other);
	}

	/**
	 * Players aim at it from up to {@code PICK_MARGIN} blocks outside the hitbox (legs, head). The server accepts hits
	 * up to 6 blocks from the hitbox (3 reach + 3 tolerance), so this stays below that.
	 */
	@Override
	public float getPickRadius() {
		return PICK_MARGIN;
	}

	/** Players may stand under its body (inside the hitbox) without being shoved out. */
	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	public boolean isPushable() {
		return false;
	}


	/** It cannot fly, so it cannot be lifted either. */
	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		return !effect.is(MobEffects.LEVITATION) && super.canBeAffected(effect);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("garipseylermc_phase_two", this.phaseTwoAnnounced);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.phaseTwoAnnounced = input.getBooleanOr("garipseylermc_phase_two", false);
		// Saved attribute values would otherwise hide config edits made since it spawned.
		ModConfig.MindFlayer config = ConfigManager.get().mindFlayer;
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(config.maxHealth);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(config.attackDamage);
		this.getAttribute(Attributes.ARMOR).setBaseValue(config.armor);
	}

	// ---------------------------------------------------------------- sounds

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return ModSounds.MIND_FLAYER_AMBIENT.value();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.MIND_FLAYER_HURT.value();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.MIND_FLAYER_DEATH.value();
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(ModSounds.MIND_FLAYER_STEP.value(), 1.5F, 1.0F);
	}

	@Override
	protected float getSoundVolume() {
		return 6.0F;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 200;
	}

	// ---------------------------------------------------------------- GeckoLib

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<MindFlayer>("main", 5, test -> test.setAndContinue(test.isMoving() ? WALK : IDLE)));
		controllers.add(new AnimationController<MindFlayer>(ACTIONS_CONTROLLER, 3, test -> PlayState.STOP)
				.triggerableAnim(MindFlayerAttack.STOMP.animation, STOMP)
				.triggerableAnim(MindFlayerAttack.ROCK.animation, THROW)
				.triggerableAnim(MindFlayerAttack.ROAR.animation, ROAR)
				.triggerableAnim("swing", SWING));
		controllers.add(DefaultAnimations.genericDeathController());
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		this.triggerAnim(ACTIONS_CONTROLLER, "swing");
		return super.doHurtTarget(level, target);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.animationCache;
	}
}
