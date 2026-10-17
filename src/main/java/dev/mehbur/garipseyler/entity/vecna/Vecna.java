package dev.mehbur.garipseyler.entity.vecna;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.constant.DefaultAnimations;
import com.geckolib.util.GeckoLibUtil;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.config.ModConfig;
import dev.mehbur.garipseyler.effect.ModEffects;
import dev.mehbur.garipseyler.effect.ModSounds;
import dev.mehbur.garipseyler.item.ModItems;
import dev.mehbur.garipseyler.network.VecnaMindControlPayload;
import dev.mehbur.garipseyler.network.VecnaVisionPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Vecna, the Upside Down's master. Spawns once per world in the Nether (see {@link VecnaSpawning}), never
 * despawns and cannot leave the Nether. Fights in melee and with the abilities in {@link VecnaAbility};
 * under 50 % health (phase 2) the boss bar turns red, abilities come faster and he teleports. Drops only
 * the Telekinesis Glove, and only with {@code gloveDropChance}.
 */
public class Vecna extends Monster implements GeoEntity {
	public static final String MINION_TAG = "garipseylermc.vecna_minion";
	public static final String ACTIONS_CONTROLLER = "actions";
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
	private static final RawAnimation CAST = RawAnimation.begin().thenPlay("attack.cast");
	private static final RawAnimation LEVITATE = RawAnimation.begin().thenPlay("attack.levitate");
	private static final RawAnimation SUMMON = RawAnimation.begin().thenPlay("attack.summon");
	private static final RawAnimation SWING = RawAnimation.begin().thenPlay("attack.swing");

	private static final int TELEGRAPH_TICKS = 80;
	private static final int CHIME_INTERVAL_TICKS = 20;
	private static final double ABILITY_RANGE = 32.0;
	private static final double TELEKINESIS_PULL_DISTANCE = 10.0;
	private static final List<SoundEvent> FAKE_SOUNDS = List.of(SoundEvents.ZOMBIE_AMBIENT, SoundEvents.SKELETON_AMBIENT,
			SoundEvents.SPIDER_AMBIENT, SoundEvents.CREEPER_PRIMED, SoundEvents.WARDEN_HEARTBEAT, SoundEvents.ENDERMAN_STARE);

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
	private final ServerBossEvent bossEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random), this.getDisplayName(),
			BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
	private final Map<VecnaAbility, Long> readyAt = new EnumMap<>(VecnaAbility.class);
	private int abilityCooldown = 60;
	private boolean phaseTwoAnnounced;
	private @Nullable VecnaAbility pending;
	private @Nullable ServerPlayer pendingTarget;
	private int telegraphTicks;

	public Vecna(EntityType<? extends Vecna> type, Level level) {
		super(type, level);
		this.bossEvent.setDarkenScreen(true);
		this.xpReward = 0;
		this.setPersistenceRequired();
	}

	public static AttributeSupplier.Builder createAttributes() {
		ModConfig.Vecna config = ConfigManager.get().vecna;
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, config.maxHealth)
				.add(Attributes.ATTACK_DAMAGE, config.attackDamage)
				.add(Attributes.ARMOR, config.armor)
				.add(Attributes.MOVEMENT_SPEED, 0.26)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(Attributes.FOLLOW_RANGE, 48.0)
				.add(Attributes.STEP_HEIGHT, 1.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 24.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
	}

	// ---------------------------------------------------------------- combat loop

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		this.updateBossBar();
		this.tickTelegraph(level);

		if (--this.abilityCooldown > 0 || this.pending != null) {
			return;
		}
		if (!(this.getTarget() instanceof ServerPlayer player) || !canAffect(player)
				|| this.distanceToSqr(player) > ABILITY_RANGE * ABILITY_RANGE || !this.hasLineOfSight(player)) {
			return;
		}
		VecnaAbility ability = this.pickAbility(level, player);
		if (ability != null) {
			this.readyAt.put(ability, level.getGameTime() + ability.cooldownSeconds * 20L);
			this.begin(level, ability, player);
		}
		int cooldown = ConfigManager.get().vecna.abilityCooldownSeconds * 20;
		this.abilityCooldown = this.isPhaseTwo() ? Math.round(cooldown * 0.7F) : cooldown;
	}

	/** Uses an ability right away (telegraphed ones still get their vision first); for admin commands. */
	public void forceAbility(VecnaAbility ability, ServerPlayer target) {
		if (this.level() instanceof ServerLevel level) {
			this.pending = null;
			this.begin(level, ability, target);
		}
	}

	public boolean isPhaseTwo() {
		return this.getHealth() < this.getMaxHealth() * 0.5F;
	}

	private @Nullable VecnaAbility pickAbility(ServerLevel level, ServerPlayer player) {
		double distance = this.distanceTo(player);
		List<VecnaAbility> candidates = new ArrayList<>();
		int totalWeight = 0;
		for (VecnaAbility ability : VecnaAbility.values()) {
			if (ability.weight <= 0 || this.readyAt.getOrDefault(ability, 0L) > level.getGameTime()) {
				continue;
			}
			boolean usable = switch (ability) {
				case CURSE -> !player.hasEffect(ModEffects.VECNA_CURSE);
				case PSYCHIC_BLAST -> distance > 4.0;
				case SUMMON -> this.countMinions(level) < ConfigManager.get().vecna.maxMinions;
				case TELEPORT -> this.isPhaseTwo();
				case LEVITATE -> !PlayerHolds.isLevitating(player);
				default -> true;
			};
			if (usable) {
				candidates.add(ability);
				totalWeight += ability.weight;
			}
		}
		if (totalWeight == 0) {
			return null;
		}
		int roll = this.random.nextInt(totalWeight);
		for (VecnaAbility ability : candidates) {
			roll -= ability.weight;
			if (roll < 0) {
				return ability;
			}
		}
		return null;
	}

	private void begin(ServerLevel level, VecnaAbility ability, ServerPlayer player) {
		if (ability.telegraphed || ability == VecnaAbility.VISION) {
			this.pending = ability;
			this.pendingTarget = player;
			this.telegraphTicks = TELEGRAPH_TICKS;
			ServerPlayNetworking.send(player, new VecnaVisionPayload(TELEGRAPH_TICKS));
		} else {
			this.perform(level, ability, player);
		}
	}

	/** Visions: four clock chimes a second apart, a red vignette and sounds of mobs that are not there. */
	private void tickTelegraph(ServerLevel level) {
		if (this.pending == null) {
			return;
		}
		ServerPlayer player = this.pendingTarget;
		if (player == null || !player.isAlive() || player.level() != level || this.distanceToSqr(player) > 48 * 48) {
			this.pending = null;
			this.pendingTarget = null;
			return;
		}
		int elapsed = TELEGRAPH_TICKS - this.telegraphTicks;
		if (elapsed % CHIME_INTERVAL_TICKS == 0) {
			playTo(player, ModSounds.VECNA_CLOCK, player.position(), 1.0F);
		}
		if (this.random.nextInt(14) == 0) {
			Vec3 offset = new Vec3(this.random.nextDouble() * 10.0 - 5.0, this.random.nextDouble() * 2.0, this.random.nextDouble() * 10.0 - 5.0);
			SoundEvent sound = FAKE_SOUNDS.get(this.random.nextInt(FAKE_SOUNDS.size()));
			playTo(player, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), player.position().add(offset), 0.9F);
		}
		if (--this.telegraphTicks <= 0) {
			VecnaAbility ability = this.pending;
			this.pending = null;
			this.pendingTarget = null;
			if (ability != VecnaAbility.VISION) {
				this.perform(level, ability, player);
			}
		}
	}

	private void perform(ServerLevel level, VecnaAbility ability, ServerPlayer player) {
		ModConfig.Vecna config = ConfigManager.get().vecna;
		this.getLookControl().setLookAt(player, 90.0F, 90.0F);
		switch (ability) {
			case CURSE -> {
				this.cast(level, "cast");
				if (MusicProtection.isProtected(player)) {
					MusicProtection.notifyShielded(player);
				} else {
					player.addEffect(new MobEffectInstance(ModEffects.VECNA_CURSE, config.curseDurationSeconds * 20, 0), this);
				}
			}
			case LEVITATE -> {
				this.cast(level, "levitate");
				if (MusicProtection.isProtected(player)) {
					MusicProtection.notifyShielded(player);
				} else {
					int ticks = config.levitationSeconds * 20;
					PlayerHolds.levitate(player, ticks);
					// A stronger, shorter curse; the longer weak one (if any) comes back after it.
					player.addEffect(new MobEffectInstance(ModEffects.VECNA_CURSE, ticks + 20, 1), this);
				}
			}
			case MIND_CONTROL -> {
				this.cast(level, "cast");
				int ticks = config.mindControlSeconds * 20;
				ServerPlayNetworking.send(player, new VecnaMindControlPayload(this.getId(), ticks));
				player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, ticks + 60, 0), this);
				player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, ticks, 0), this);
			}
			case VISION -> {
			}
			case TELEKINESIS -> {
				this.cast(level, "cast");
				Vec3 away = player.position().subtract(this.position()).normalize();
				Vec3 push = this.distanceTo(player) > TELEKINESIS_PULL_DISTANCE
						? away.scale(-1.7).add(0.0, 0.45, 0.0)
						: away.scale(2.3).add(0.0, 0.7, 0.0);
				player.setDeltaMovement(push);
				player.hurtMarked = true;
				level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY(1.0), player.getZ(), 30, 0.3, 0.6, 0.3, 0.2);
			}
			case VINE_BIND -> {
				this.cast(level, "cast");
				PlayerHolds.bind(player, config.vineBindSeconds * 20);
				level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SCULK_VEIN_PLACE, SoundSource.HOSTILE, 1.5F, 0.6F);
			}
			case PSYCHIC_BLAST -> {
				this.triggerAnim(ACTIONS_CONTROLLER, "cast");
				Vec3 from = this.getEyePosition();
				Vec3 direction = player.getEyePosition().subtract(from).normalize();
				PsychicBlast blast = new PsychicBlast(level, this, direction);
				blast.setPos(from.add(direction.scale(1.2)));
				level.addFreshEntity(blast);
				level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.VECNA_PSYCHIC_BLAST, SoundSource.HOSTILE, 2.0F, 1.0F);
			}
			case SUMMON -> {
				this.triggerAnim(ACTIONS_CONTROLLER, "summon");
				this.summonMinions(level, player);
			}
			case TELEPORT -> this.teleportNear(player);
		}
	}

	private void cast(ServerLevel level, String animation) {
		this.triggerAnim(ACTIONS_CONTROLLER, animation);
		level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.VECNA_CAST, SoundSource.HOSTILE, 2.0F, 1.0F);
	}

	private void summonMinions(ServerLevel level, ServerPlayer player) {
		int room = ConfigManager.get().vecna.maxMinions - this.countMinions(level);
		List<EntityType<? extends Mob>> wave = List.of(EntityTypes.ZOMBIFIED_PIGLIN, EntityTypes.ZOMBIFIED_PIGLIN, EntityTypes.PHANTOM);
		for (int i = 0; i < Math.min(room, wave.size()); i++) {
			EntityType<? extends Mob> type = wave.get(i);
			double angle = this.random.nextDouble() * Math.PI * 2.0;
			BlockPos pos = BlockPos.containing(this.getX() + Math.cos(angle) * 3.0,
					this.getY() + (type == EntityTypes.PHANTOM ? 4.0 : 0.0), this.getZ() + Math.sin(angle) * 3.0);
			Mob minion = type.spawn(level, pos, EntitySpawnReason.MOB_SUMMONED);
			if (minion == null) {
				continue;
			}
			minion.addTag(MINION_TAG);
			minion.setTarget(player);
			if (minion instanceof ZombifiedPiglin piglin) {
				piglin.setPersistentAngerTarget(EntityReference.of(player));
				piglin.startPersistentAngerTimer();
			}
			level.sendParticles(ParticleTypes.SOUL, minion.getX(), minion.getY(0.5), minion.getZ(), 20, 0.4, 0.6, 0.4, 0.05);
		}
	}

	private int countMinions(ServerLevel level) {
		return level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(48.0), mob -> mob.isAlive() && mob.entityTags().contains(MINION_TAG)).size();
	}

	/** Short teleport to a free spot 5–10 blocks around the target. */
	private boolean teleportNear(LivingEntity target) {
		for (int attempt = 0; attempt < 16; attempt++) {
			double angle = this.random.nextDouble() * Math.PI * 2.0;
			double distance = 5.0 + this.random.nextDouble() * 5.0;
			double x = target.getX() + Math.cos(angle) * distance;
			double z = target.getZ() + Math.sin(angle) * distance;
			double y = target.getY() + this.random.nextInt(5) - 2;
			Vec3 from = this.position();
			if (this.randomTeleport(x, y, z, true)) {
				this.level().playSound(null, from.x, from.y, from.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5F, 0.6F);
				this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.5F, 0.6F);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		boolean hurt = super.hurtServer(level, source, damage);
		if (hurt && this.isAlive() && this.isPhaseTwo() && source.getEntity() instanceof LivingEntity attacker
				&& this.readyAt.getOrDefault(VecnaAbility.TELEPORT, 0L) <= level.getGameTime() && this.random.nextFloat() < 0.2F) {
			this.readyAt.put(VecnaAbility.TELEPORT, level.getGameTime() + VecnaAbility.TELEPORT.cooldownSeconds * 20L);
			this.teleportNear(attacker);
		}
		return hurt;
	}

	private static boolean canAffect(ServerPlayer player) {
		return player.isAlive() && !player.isCreative() && !player.isSpectator();
	}

	private static void playTo(ServerPlayer player, Holder<SoundEvent> sound, Vec3 at, float volume) {
		player.connection.send(new ClientboundSoundPacket(sound, SoundSource.HOSTILE, at.x, at.y, at.z, volume, 1.0F, player.getRandom().nextLong()));
	}

	// ---------------------------------------------------------------- boss bar

	private void updateBossBar() {
		this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
		boolean phaseTwo = this.isPhaseTwo();
		this.bossEvent.setColor(phaseTwo ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PURPLE);
		if (phaseTwo && !this.phaseTwoAnnounced) {
			this.phaseTwoAnnounced = true;
			for (ServerPlayer player : this.bossEvent.getPlayers()) {
				player.sendSystemMessage(Component.translatable("message.garipseylermc.vecna.phase_two").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
			}
			this.playSound(ModSounds.VECNA_SPAWN.value(), 3.0F, 0.8F);
		}
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		this.bossEvent.addPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossEvent.removePlayer(player);
	}

	@Override
	public void setCustomName(@Nullable Component name) {
		super.setCustomName(name);
		this.bossEvent.setName(this.getDisplayName());
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

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		return !effect.is(ModEffects.VECNA_CURSE) && super.canBeAffected(effect);
	}

	@Override
	protected boolean considersEntityAsAlly(Entity other) {
		return other.entityTags().contains(MINION_TAG) || super.considersEntityAsAlly(other);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		// Nothing but the glove, and only rarely.
		if (this.random.nextDouble() < ConfigManager.get().vecna.gloveDropChance) {
			this.spawnAtLocation(level, new ItemStack(ModItems.TELEKINESIS_GLOVE));
		}
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (this.level() instanceof ServerLevel level) {
			VecnaWorldData.get(level.getServer()).markDefeated();
		}
	}

	// ---------------------------------------------------------------- sounds

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return ModSounds.VECNA_AMBIENT.value();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.VECNA_HURT.value();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.VECNA_DEATH.value();
	}

	@Override
	public int getAmbientSoundInterval() {
		return 160;
	}

	// ---------------------------------------------------------------- GeckoLib

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<Vecna>("main", 5, test -> test.setAndContinue(test.isMoving() ? WALK : IDLE)));
		controllers.add(new AnimationController<Vecna>(ACTIONS_CONTROLLER, 3, test -> PlayState.STOP)
				.triggerableAnim("cast", CAST)
				.triggerableAnim("levitate", LEVITATE)
				.triggerableAnim("summon", SUMMON)
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
