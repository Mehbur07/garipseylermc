package dev.mehbur.garipseyler.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Every tunable number of the mod. Serialized as {@code config/garipseylermc.json}.
 * Field defaults are the shipped values; {@link #validate()} clamps hand-edited values into a safe range.
 */
public class ModConfig {
	public Nether nether = new Nether();
	public End end = new End();
	public Demobat demobat = new Demobat();
	public Vine vine = new Vine();
	public Vecna vecna = new Vecna();
	public MindFlayer mindFlayer = new MindFlayer();
	public Glove glove = new Glove();

	/** Upside Down (Nether) atmosphere. Colors and fog apply on world/server start. */
	public static class Nether {
		public boolean titleOnEnter = true;
		public String fogColor = "#3d0a0d";
		public String ambientLightColor = "#3a1414";
		public double fogStartDistance = 4.0;
		public double fogEndDistance = 72.0;
		/** Chance per ambient particle tick that a spore spawns (vanilla crimson forest: 0.025). */
		public double sporeProbability = 0.03;
		public boolean redLightningEnabled = true;
		public int redLightningIntervalSeconds = 7;
		/** Horizontal distance range (blocks) from a player where a bolt strikes. */
		public int redLightningMinDistance = 16;
		public int redLightningMaxDistance = 48;
		/** Client-side: thunder sound and red screen flash (flash also obeys the vanilla "Hide Lightning Flashes" option). */
		public boolean redLightningSound = true;
		public boolean redLightningFlash = true;
		/** Peak opacity of the red screen flash, 0–1. */
		public double redLightningFlashIntensity = 0.35;
	}

	/** Deepest World (End) atmosphere. Colors apply on world/server start. */
	public static class End {
		public boolean titleOnEnter = true;
		public String fogColor = "#14070a";
		public String ambientLightColor = "#3a1f22";
		public String skyLightColor = "#8a2a3a";
		/** Chance per ambient particle tick that falling ash spawns. */
		public double ashProbability = 0.004;
		/** Build newly generated End spikes from Upside Down stone instead of obsidian. */
		public boolean upsideDownStoneSpikes = true;
		/** Client-side: every block except End stone is drawn in dripstone colors while in the End (F3+T to apply). */
		public boolean dripstoneBlocks = true;
		/** Client-side: extra block ids (e.g. "minecraft:crafting_table") that keep their own texture in the End. */
		public List<String> keepOriginalTexture = new ArrayList<>();
	}

	/** Demobat (re-skinned Phantom) spawning. Spawn weights apply on world/server start. */
	public static class Demobat {
		/** Disables the vanilla insomnia spawner, so Phantoms never appear in the Overworld. */
		public boolean disableOverworldPhantoms = true;
		/** Demobats (Phantoms and Ghasts) only exist in the Nether: any that appear in the Overworld or the End are removed. */
		public boolean netherOnly = true;
		/** Natural spawn weight in every Nether biome (vanilla Nether Wastes: zombified piglin 100, ghast 50). */
		public int netherSpawnWeight = 10;
		public int netherMinGroup = 1;
		public int netherMaxGroup = 2;
	}

	/** Upside Down vine that grows on lit Nether portals. */
	public static class Vine {
		/** Grow vines on portals lit by players. */
		public boolean growOnLitPortals = true;
		/** Grow vines on portals the game builds on the other side when someone travels. */
		public boolean growOnGeneratedPortals = true;
		/** Share of free obsidian faces that get a vine, 0–1. */
		public double coverage = 0.65;
		/** How long the bottom-to-top emergence takes (Warden emerge animation: 134 ticks). */
		public int growthDurationTicks = 120;
		/** Minutes spent in the Nether near a vined portal before health is dropped. */
		public int drainIntervalMinutes = 10;
		/** Health (in HP, 2 HP = 1 heart) players are dropped to. */
		public double drainTargetHealth = 1.0;
		public int effectRadius = 32;
		public boolean warningEnabled = true;
		public int warningSeconds = 30;
	}

	/** Vecna boss. */
	public static class Vecna {
		public double maxHealth = 2000.0;
		/** Total minutes a player must spend in the Nether before the one-time spawn. */
		public int spawnDelayMinutes = 15;
		/** 0.001 = 0.1 %. */
		public double gloveDropChance = 0.001;
		public double attackDamage = 12.0;
		public double armor = 10.0;
		/** Seconds between two abilities (phase 2, under 50 % health, uses 70 % of it). */
		public int abilityCooldownSeconds = 6;
		/** Curse: 1 damage every this many seconds (twice as fast while levitating); natural regeneration stops. */
		public int curseDamageIntervalSeconds = 2;
		public int curseDurationSeconds = 20;
		public int levitationSeconds = 4;
		public int mindControlSeconds = 3;
		public int vineBindSeconds = 3;
		public double psychicBlastDamage = 8.0;
		/** Most minions (Demodogs + Demobats) alive at once. */
		public int maxMinions = 6;
		/** A playing jukebox this close, or a music disc in hand, protects from levitation and the curse. */
		public int musicProtectionRadius = 12;
	}

	/** Mind Flayer (stone form) — replaces the Ender Dragon. */
	public static class MindFlayer {
		public double maxHealth = 1500.0;
		public int regenDelaySeconds = 10;
		/** Seconds to regenerate from 0 to full health. */
		public int fullRegenSeconds = 60;
	}

	/** Telekinesis glove (offhand item). */
	public static class Glove {
		/** How far (blocks) a mob can be grabbed. */
		public int range = 20;
		/** Starts when a mob is thrown or dropped. */
		public int cooldownSeconds = 5;
		/** Throw speed in blocks per tick (2.0 = 40 blocks/s). */
		public double throwStrength = 2.0;
		/** A held mob is dropped after this long. */
		public int maxHoldSeconds = 15;
		/** Thrown mobs that slam into a wall take this much damage per block/tick of speed. */
		public double impactDamage = 5.0;
	}

	/** Fills missing sections and clamps values into sane ranges. */
	public void validate() {
		if (nether == null) nether = new Nether();
		if (end == null) end = new End();
		if (demobat == null) demobat = new Demobat();
		if (vine == null) vine = new Vine();
		if (vecna == null) vecna = new Vecna();
		if (mindFlayer == null) mindFlayer = new MindFlayer();
		if (glove == null) glove = new Glove();

		Nether netherDefaults = new Nether();
		nether.fogColor = validColor(nether.fogColor, netherDefaults.fogColor);
		nether.ambientLightColor = validColor(nether.ambientLightColor, netherDefaults.ambientLightColor);
		nether.fogStartDistance = clamp(nether.fogStartDistance, -64.0, 1024.0);
		nether.fogEndDistance = clamp(nether.fogEndDistance, 1.0, 1024.0);
		nether.sporeProbability = clamp(nether.sporeProbability, 0.0, 1.0);
		nether.redLightningIntervalSeconds = clamp(nether.redLightningIntervalSeconds, 1, 3600);
		nether.redLightningMinDistance = clamp(nether.redLightningMinDistance, 0, 128);
		nether.redLightningMaxDistance = clamp(nether.redLightningMaxDistance, nether.redLightningMinDistance + 1, 160);
		nether.redLightningFlashIntensity = clamp(nether.redLightningFlashIntensity, 0.0, 1.0);

		End endDefaults = new End();
		end.fogColor = validColor(end.fogColor, endDefaults.fogColor);
		end.ambientLightColor = validColor(end.ambientLightColor, endDefaults.ambientLightColor);
		end.skyLightColor = validColor(end.skyLightColor, endDefaults.skyLightColor);
		end.ashProbability = clamp(end.ashProbability, 0.0, 1.0);
		if (end.keepOriginalTexture == null) end.keepOriginalTexture = new ArrayList<>();

		demobat.netherSpawnWeight = clamp(demobat.netherSpawnWeight, 0, 1000);
		demobat.netherMinGroup = clamp(demobat.netherMinGroup, 1, 16);
		demobat.netherMaxGroup = clamp(demobat.netherMaxGroup, demobat.netherMinGroup, 16);

		vine.coverage = clamp(vine.coverage, 0.0, 1.0);
		vine.growthDurationTicks = clamp(vine.growthDurationTicks, 1, 1200);
		vine.drainIntervalMinutes = clamp(vine.drainIntervalMinutes, 1, 1440);
		vine.drainTargetHealth = clamp(vine.drainTargetHealth, 0.5, 1024.0);
		vine.effectRadius = clamp(vine.effectRadius, 1, 512);
		vine.warningSeconds = clamp(vine.warningSeconds, 0, vine.drainIntervalMinutes * 60 - 1);

		vecna.maxHealth = clamp(vecna.maxHealth, 1.0, 1_000_000.0);
		vecna.spawnDelayMinutes = clamp(vecna.spawnDelayMinutes, 0, 100_000);
		vecna.gloveDropChance = clamp(vecna.gloveDropChance, 0.0, 1.0);
		vecna.attackDamage = clamp(vecna.attackDamage, 0.0, 2048.0);
		vecna.armor = clamp(vecna.armor, 0.0, 30.0);
		vecna.abilityCooldownSeconds = clamp(vecna.abilityCooldownSeconds, 1, 600);
		vecna.curseDamageIntervalSeconds = clamp(vecna.curseDamageIntervalSeconds, 1, 60);
		vecna.curseDurationSeconds = clamp(vecna.curseDurationSeconds, 1, 600);
		vecna.levitationSeconds = clamp(vecna.levitationSeconds, 1, 30);
		vecna.mindControlSeconds = clamp(vecna.mindControlSeconds, 1, 30);
		vecna.vineBindSeconds = clamp(vecna.vineBindSeconds, 1, 30);
		vecna.psychicBlastDamage = clamp(vecna.psychicBlastDamage, 0.0, 2048.0);
		vecna.maxMinions = clamp(vecna.maxMinions, 0, 32);
		vecna.musicProtectionRadius = clamp(vecna.musicProtectionRadius, 0, 32);

		mindFlayer.maxHealth = clamp(mindFlayer.maxHealth, 1.0, 1_000_000.0);
		mindFlayer.regenDelaySeconds = clamp(mindFlayer.regenDelaySeconds, 0, 3600);
		mindFlayer.fullRegenSeconds = clamp(mindFlayer.fullRegenSeconds, 1, 3600);

		glove.range = clamp(glove.range, 1, 128);
		glove.cooldownSeconds = clamp(glove.cooldownSeconds, 0, 3600);
		glove.throwStrength = clamp(glove.throwStrength, 0.0, 10.0);
		glove.maxHoldSeconds = clamp(glove.maxHoldSeconds, 1, 3600);
		glove.impactDamage = clamp(glove.impactDamage, 0.0, 100.0);
	}

	/** Parses {@code #RRGGBB} into an opaque ARGB int. Call only on validated values. */
	public static int parseColor(String hex) {
		return 0xFF000000 | Integer.parseInt(hex.substring(1), 16);
	}

	private static String validColor(String value, String fallback) {
		return value != null && value.matches("#[0-9a-fA-F]{6}") ? value : fallback;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static double clamp(double value, double min, double max) {
		return Double.isNaN(value) ? min : Math.max(min, Math.min(max, value));
	}
}
