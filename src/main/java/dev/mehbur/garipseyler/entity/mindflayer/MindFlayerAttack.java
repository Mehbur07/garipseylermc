package dev.mehbur.garipseyler.entity.mindflayer;

import java.util.Locale;

/**
 * Special attacks of the Mind Flayer. Each plays its animation for {@code durationTicks}, the effect lands at
 * {@code impactTick}; {@code weight} is the chance share when picking and {@code cooldownSeconds} the per-attack cooldown.
 */
public enum MindFlayerAttack {
	/** Ground slam: a shockwave ring spreads along the ground. */
	STOMP("stomp", 4, 8, 34, 20),
	/** Throws a rock (three in phase 2) in an arc at the target. */
	ROCK("rock", 4, 4, 30, 16),
	/** Roar: Slowness for nearby players. */
	ROAR("roar", 2, 15, 40, 12);

	public final String animation;
	public final int weight;
	public final int cooldownSeconds;
	public final int durationTicks;
	public final int impactTick;

	MindFlayerAttack(String animation, int weight, int cooldownSeconds, int durationTicks, int impactTick) {
		this.animation = animation;
		this.weight = weight;
		this.cooldownSeconds = cooldownSeconds;
		this.durationTicks = durationTicks;
		this.impactTick = impactTick;
	}

	/** Name used by {@code /garipseyler mindflayer attack <name>}. */
	public String id() {
		return this.name().toLowerCase(Locale.ROOT);
	}
}
