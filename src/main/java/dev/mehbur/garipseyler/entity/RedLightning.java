package dev.mehbur.garipseyler.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Purely visual red lightning bolt of the Upside Down. It only ever exists on the client (spawned from a
 * network payload), so it cannot deal damage, start fires or convert mobs. Flash timing mirrors vanilla
 * lightning: a few short flickers, each with a new bolt shape.
 */
public class RedLightning extends Entity {
	/** Vanilla bolt geometry is 8 segments of 16 blocks. */
	public static final float VANILLA_BOLT_HEIGHT = 128.0F;

	private int life = 2;
	private int flashes;
	private long seed;
	private float boltHeight = VANILLA_BOLT_HEIGHT;

	public RedLightning(EntityType<? extends RedLightning> type, Level level) {
		super(type, level);
		this.seed = this.random.nextLong();
		this.flashes = this.random.nextInt(3) + 1;
	}

	@Override
	public void tick() {
		super.tick();
		this.life--;
		if (this.life < 0) {
			if (this.flashes == 0) {
				this.discard();
			} else if (this.life < -this.random.nextInt(10)) {
				this.flashes--;
				this.life = 1;
				this.seed = this.random.nextLong();
			}
		}
	}

	/** True while a flicker is visible; the bolt is hidden between flickers. */
	public boolean isFlashing() {
		return this.life >= 0;
	}

	public long getSeed() {
		return this.seed;
	}

	public float getBoltHeight() {
		return this.boltHeight;
	}

	public void setBoltHeight(float boltHeight) {
		this.boltHeight = boltHeight;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		double size = 64.0 * getViewScale();
		return distance < size * size;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}
}
