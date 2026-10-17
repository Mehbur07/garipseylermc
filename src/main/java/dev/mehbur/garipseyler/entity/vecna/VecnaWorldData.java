package dev.mehbur.garipseyler.entity.vecna;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mehbur.garipseyler.GaripSeylerMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * World-wide Vecna state, saved with the Overworld: whether he has spawned (he only ever spawns once per
 * world) and whether he was defeated.
 */
public class VecnaWorldData extends SavedData {
	public static final Codec<VecnaWorldData> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.BOOL.optionalFieldOf("spawned", false).forGetter(data -> data.spawned),
			Codec.BOOL.optionalFieldOf("defeated", false).forGetter(data -> data.defeated)
	).apply(i, VecnaWorldData::new));
	/** No vanilla data fixer applies to mod data; Fabric API accepts a null DataFixTypes here. */
	public static final SavedDataType<VecnaWorldData> TYPE = new SavedDataType<>(
			GaripSeylerMod.id("vecna"), VecnaWorldData::new, CODEC, null);

	private boolean spawned;
	private boolean defeated;

	public VecnaWorldData() {
		this(false, false);
	}

	private VecnaWorldData(boolean spawned, boolean defeated) {
		this.spawned = spawned;
		this.defeated = defeated;
	}

	public static VecnaWorldData get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	public boolean spawned() {
		return this.spawned;
	}

	public boolean defeated() {
		return this.defeated;
	}

	public void markSpawned() {
		this.spawned = true;
		this.setDirty();
	}

	public void markDefeated() {
		this.defeated = true;
		this.setDirty();
	}

	/** Admin reset (testing): Vecna may spawn again. */
	public void reset() {
		this.spawned = false;
		this.defeated = false;
		this.setDirty();
	}
}
