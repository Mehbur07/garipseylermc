package dev.mehbur.garipseyler.entity.vecna;

import java.util.Locale;

/**
 * Vecna's abilities. {@link #weight} is the pick weight in combat, {@link #cooldownSeconds} the time before
 * the same ability can be picked again; {@link #telegraphed} abilities are announced first by a vision
 * (four clock chimes, red vignette, fake mob sounds). The music counter mechanic is passive and lives in
 * {@link MusicProtection}.
 */
public enum VecnaAbility {
	CURSE(3, 15, false),
	LEVITATE(2, 20, true),
	MIND_CONTROL(2, 20, true),
	VISION(0, 0, false),
	TELEKINESIS(2, 8, false),
	VINE_BIND(2, 12, false),
	PSYCHIC_BLAST(3, 4, false),
	SUMMON(1, 30, false),
	TELEPORT(2, 6, false);

	public final int weight;
	public final int cooldownSeconds;
	public final boolean telegraphed;

	VecnaAbility(int weight, int cooldownSeconds, boolean telegraphed) {
		this.weight = weight;
		this.cooldownSeconds = cooldownSeconds;
		this.telegraphed = telegraphed;
	}

	/** Lower-case name used by commands, e.g. {@code mind_control}. */
	public String id() {
		return this.name().toLowerCase(Locale.ROOT);
	}
}
