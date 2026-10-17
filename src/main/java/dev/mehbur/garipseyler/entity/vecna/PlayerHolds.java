package dev.mehbur.garipseyler.entity.vecna;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.block.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps players in place for Vecna's levitation (lifted a few blocks and frozen in mid-air) and vine bind
 * (rooted to the ground, can still look around and fight back). Server-side; movement is enforced by
 * teleports (levitation) or by zeroing movement attributes (bind), so it works without client code.
 */
public final class PlayerHolds {
	private static final int RISE_TICKS = 20;
	private static final double LIFT_HEIGHT = 4.0;
	private static final Identifier BIND_SPEED = GaripSeylerMod.id("vine_bind_speed");
	private static final Identifier BIND_JUMP = GaripSeylerMod.id("vine_bind_jump");
	private static final DustParticleOptions LEVITATION_DUST = new DustParticleOptions(0xB01838, 1.2F);

	private record Levitation(ServerLevel level, Vec3 start, double height, int duration, int age) {
		Levitation next() {
			return new Levitation(this.level, this.start, this.height, this.duration, this.age + 1);
		}
	}

	private static final Map<UUID, Levitation> LEVITATING = new HashMap<>();
	private record Bind(Vec3 anchor, int ticksLeft) {
	}

	private static final Map<UUID, Bind> BOUND = new HashMap<>();
	/** How far (blocks) a rooted player may drift before being snapped back to the bind spot. */
	private static final double BIND_SLACK = 0.2;

	private PlayerHolds() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(PlayerHolds::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			LEVITATING.clear();
			BOUND.clear();
		});
	}

	public static boolean isLevitating(ServerPlayer player) {
		return LEVITATING.containsKey(player.getUUID());
	}

	/** Lifts the player up to {@link #LIFT_HEIGHT} blocks (less under a ceiling) and freezes them there. */
	public static void levitate(ServerPlayer player, int ticks) {
		double height = 0.0;
		while (height < LIFT_HEIGHT && player.level().noCollision(player, player.getBoundingBox().move(0.0, height + 0.5, 0.0))) {
			height += 0.5;
		}
		LEVITATING.put(player.getUUID(), new Levitation(player.level(), player.position(), height, RISE_TICKS + ticks, 0));
	}

	/** Roots the player: no walking, no jumping, for {@code ticks}. */
	public static void bind(ServerPlayer player, int ticks) {
		addModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), BIND_SPEED);
		addModifier(player.getAttribute(Attributes.JUMP_STRENGTH), BIND_JUMP);
		player.setDeltaMovement(Vec3.ZERO);
		player.hurtMarked = true;
		BOUND.put(player.getUUID(), new Bind(player.position(), ticks));
	}

	private static void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, Levitation>> levitations = LEVITATING.entrySet().iterator();
		while (levitations.hasNext()) {
			Map.Entry<UUID, Levitation> entry = levitations.next();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			Levitation levitation = entry.getValue();
			if (player == null || !player.isAlive() || player.level() != levitation.level() || levitation.age() >= levitation.duration()) {
				levitations.remove();
				continue;
			}
			float rise = Math.min(1.0F, (levitation.age() + 1) / (float) RISE_TICKS);
			Vec3 target = levitation.start().add(0.0, levitation.height() * rise, 0.0);
			player.connection.teleport(new PositionMoveRotation(target, Vec3.ZERO, 0.0F, 0.0F), Relative.ROTATION);
			player.resetFallDistance();
			if (levitation.age() % 4 == 0) {
				levitation.level().sendParticles(LEVITATION_DUST, player.getX(), player.getY() + 1.0, player.getZ(), 6, 0.35, 0.8, 0.35, 0.0);
			}
			entry.setValue(levitation.next());
		}

		Iterator<Map.Entry<UUID, Bind>> binds = BOUND.entrySet().iterator();
		while (binds.hasNext()) {
			Map.Entry<UUID, Bind> entry = binds.next();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			Bind bind = entry.getValue();
			int left = bind.ticksLeft() - 1;
			if (player == null) {
				binds.remove();
				continue;
			}
			if (left <= 0 || !player.isAlive()) {
				removeModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), BIND_SPEED);
				removeModifier(player.getAttribute(Attributes.JUMP_STRENGTH), BIND_JUMP);
				binds.remove();
				continue;
			}
			// Movement attributes stop walking; this also stops sliding from knockback or client prediction.
			Vec3 drift = player.position().subtract(bind.anchor());
			if (drift.x * drift.x + drift.z * drift.z > BIND_SLACK * BIND_SLACK) {
				player.connection.teleport(new PositionMoveRotation(new Vec3(bind.anchor().x, player.getY(), bind.anchor().z), Vec3.ZERO, 0.0F, 0.0F),
						Relative.ROTATION);
			}
			if (left % 5 == 0) {
				player.level().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.UPSIDE_DOWN_VINE.defaultBlockState()),
						player.getX(), player.getY() + 0.3, player.getZ(), 8, 0.35, 0.3, 0.35, 0.02);
			}
			entry.setValue(new Bind(bind.anchor(), left));
		}
	}

	private static void addModifier(AttributeInstance attribute, Identifier id) {
		if (attribute != null && !attribute.hasModifier(id)) {
			attribute.addTransientModifier(new AttributeModifier(id, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
	}

	private static void removeModifier(AttributeInstance attribute, Identifier id) {
		if (attribute != null) {
			attribute.removeModifier(id);
		}
	}
}
