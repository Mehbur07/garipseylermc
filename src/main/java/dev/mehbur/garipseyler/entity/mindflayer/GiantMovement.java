package dev.mehbur.garipseyler.entity.mindflayer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Movement for the giant Mind Flayer. Vanilla path finding checks every block of a mob's hitbox per node, which is far
 * too expensive for a 10×36 block body, so it walks straight at its goal instead: it turns slowly, steps over the
 * island's bumps (high step height), and stops at edges rather than walking into the void.
 */
final class GiantMovement {
	/** Degrees per tick the body may turn while walking. */
	private static final float TURN_SPEED = 6.0F;
	private static final int MELEE_COOLDOWN_TICKS = 30;

	private GiantMovement() {
	}

	/**
	 * Like the vanilla move control, but with a slow turn and no jumping. When the body runs into something it cannot
	 * step over (an End spike), it veers 75 degrees to one side for a while to walk around it, and tries the other side
	 * if that is blocked too.
	 */
	static final class GiantMoveControl extends MoveControl<MindFlayer> {
		private static final int DETOUR_TICKS = 40;
		private static final double DETOUR_ANGLE = Math.toRadians(75.0);
		private int detourTicks;
		private int detourSide = 1;
		private int blockedTicks;

		GiantMoveControl(MindFlayer flayer) {
			super(flayer);
		}

		@Override
		public void tick() {
			if (this.operation != Operation.MOVE_TO) {
				this.operation = Operation.WAIT;
				this.mob.setZza(0.0F);
				return;
			}
			this.operation = Operation.WAIT;
			double dx = this.wantedX - this.mob.getX();
			double dz = this.wantedZ - this.mob.getZ();
			if (dx * dx + dz * dz < 1.0) {
				this.mob.setZza(0.0F);
				return;
			}
			float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
			this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yaw, TURN_SPEED));
			// Walk only once roughly facing the goal, so it turns on the spot first.
			float off = Math.abs(Mth.wrapDegrees(yaw - this.mob.getYRot()));
			float speed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
			this.mob.setSpeed(off < 45.0F ? speed : 0.0F);
		}

		void walkTo(Vec3 target, double speedModifier) {
			if (this.mob.horizontalCollision && this.mob.getSpeed() > 0.0F) {
				if (++this.blockedTicks >= 5) {
					this.blockedTicks = 0;
					// Already detouring and still stuck: try the other side.
					this.detourSide = this.detourTicks > 0 ? -this.detourSide : (this.mob.getRandom().nextBoolean() ? 1 : -1);
					this.detourTicks = DETOUR_TICKS;
				}
			} else {
				this.blockedTicks = 0;
			}
			Vec3 goal = target;
			if (this.detourTicks > 0) {
				this.detourTicks--;
				Vec3 flat = new Vec3(target.x - this.mob.getX(), 0.0, target.z - this.mob.getZ());
				if (flat.lengthSqr() > 1.0E-4) {
					goal = this.mob.position().add(flat.normalize().yRot((float) (DETOUR_ANGLE * this.detourSide)).scale(12.0));
				}
			}
			this.setWantedPosition(goal.x, goal.y, goal.z, speedModifier);
		}
	}

	/** True when there is ground within 12 blocks below the spot a few blocks past the front of the body. */
	static boolean groundAhead(MindFlayer flayer, Vec3 toward) {
		Vec3 flat = new Vec3(toward.x - flayer.getX(), 0.0, toward.z - flayer.getZ());
		if (flat.lengthSqr() < 1.0E-4) {
			return true;
		}
		Vec3 probe = flayer.position().add(flat.normalize().scale(flayer.getBbWidth() * 0.5 + 3.0));
		BlockPos.MutableBlockPos pos = BlockPos.containing(probe.x, flayer.getY() + 3.0, probe.z).mutable();
		for (int i = 0; i < 16; i++, pos.move(0, -1, 0)) {
			if (!flayer.level().getBlockState(pos).isAir()) {
				return true;
			}
		}
		return false;
	}

	/** Walks at the target and hits whatever is under or right next to the body. Pauses during special attacks. */
	static final class ChaseGoal extends Goal {
		private final MindFlayer flayer;
		private int meleeCooldown;

		ChaseGoal(MindFlayer flayer) {
			this.flayer = flayer;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity target = this.flayer.getTarget();
			return !this.flayer.isBusy() && target != null && target.isAlive()
					&& !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
		}

		@Override
		public boolean canContinueToUse() {
			return this.canUse();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			LivingEntity target = this.flayer.getTarget();
			if (target == null) {
				return;
			}
			this.flayer.getLookControl().setLookAt(target, 10.0F, 10.0F);
			boolean inReach = this.flayer.isWithinMeleeAttackRange(target);
			double flat = Mth.length(target.getX() - this.flayer.getX(), target.getZ() - this.flayer.getZ());
			if (!inReach && flat > 2.0 && groundAhead(this.flayer, target.position())) {
				((GiantMoveControl) this.flayer.getMoveControl()).walkTo(target.position(), 1.0);
			}
			if (--this.meleeCooldown <= 0 && inReach && this.flayer.level() instanceof ServerLevel level) {
				this.meleeCooldown = MELEE_COOLDOWN_TICKS;
				this.flayer.swing(InteractionHand.MAIN_HAND);
				this.flayer.doHurtTarget(level, target);
			}
		}
	}

	/** Without a target: goes back inside the arena when outside it, otherwise strolls to a random spot now and then. */
	static final class WanderGoal extends Goal {
		private final MindFlayer flayer;
		private Vec3 goal = Vec3.ZERO;
		private int ticksLeft;

		WanderGoal(MindFlayer flayer) {
			this.flayer = flayer;
			this.setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (this.flayer.isBusy() || this.flayer.getTarget() != null) {
				return false;
			}
			if (this.flayer.hasHome() && !this.flayer.isWithinHome()) {
				this.goal = Vec3.atBottomCenterOf(this.flayer.getHomePosition());
				return true;
			}
			if (this.flayer.getRandom().nextInt(240) != 0) {
				return false;
			}
			BlockPos center = this.flayer.hasHome() ? this.flayer.getHomePosition() : this.flayer.blockPosition();
			int radius = this.flayer.hasHome() ? Math.max(8, this.flayer.getHomeRadius() / 2) : 24;
			double angle = this.flayer.getRandom().nextDouble() * Math.PI * 2.0;
			double distance = this.flayer.getRandom().nextDouble() * radius;
			this.goal = new Vec3(center.getX() + Math.cos(angle) * distance, this.flayer.getY(), center.getZ() + Math.sin(angle) * distance);
			return true;
		}

		@Override
		public void start() {
			this.ticksLeft = 400;
		}

		@Override
		public boolean canContinueToUse() {
			return !this.flayer.isBusy() && this.flayer.getTarget() == null && this.ticksLeft > 0
					&& Mth.length(this.goal.x - this.flayer.getX(), this.goal.z - this.flayer.getZ()) > 3.0;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			this.ticksLeft--;
			if (groundAhead(this.flayer, this.goal)) {
				((GiantMoveControl) this.flayer.getMoveControl()).walkTo(this.goal, 0.7);
			} else {
				this.ticksLeft = 0;
			}
		}
	}
}
