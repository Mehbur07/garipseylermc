package dev.mehbur.garipseyler.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mehbur.garipseyler.GaripSeylerMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-dimension list of portal frames that grew Upside Down vines. Saved with the world; a site is
 * dropped once all of its vines have been removed.
 */
public class VineSites extends SavedData {
	public record Site(BlockPos min, BlockPos max) {
		public static final Codec<Site> CODEC = RecordCodecBuilder.create(i -> i.group(
				BlockPos.CODEC.fieldOf("min").forGetter(Site::min),
				BlockPos.CODEC.fieldOf("max").forGetter(Site::max)
		).apply(i, Site::new));

		public Vec3 center() {
			return new Vec3((this.min.getX() + this.max.getX() + 1) / 2.0,
					(this.min.getY() + this.max.getY() + 1) / 2.0,
					(this.min.getZ() + this.max.getZ() + 1) / 2.0);
		}

		public boolean hasVines(ServerLevel level) {
			for (BlockPos pos : BlockPos.betweenClosed(this.min, this.max)) {
				if (level.getBlockState(pos).is(ModBlocks.UPSIDE_DOWN_VINE)) {
					return true;
				}
			}
			return false;
		}

		public boolean isLoaded(ServerLevel level) {
			return level.isLoaded(this.min) && level.isLoaded(this.max);
		}
	}

	public static final Codec<VineSites> CODEC = Site.CODEC.listOf()
			.xmap(VineSites::new, sites -> sites.sites)
			.fieldOf("sites").codec();
	/** No vanilla data fixer applies to mod data; Fabric API accepts a null DataFixTypes here. */
	public static final SavedDataType<VineSites> TYPE = new SavedDataType<>(
			GaripSeylerMod.id("vine_sites"), VineSites::new, CODEC, null);

	private final List<Site> sites;

	public VineSites() {
		this(List.of());
	}

	private VineSites(List<Site> sites) {
		this.sites = new ArrayList<>(sites);
	}

	public static VineSites get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	public List<Site> sites() {
		return List.copyOf(this.sites);
	}

	public void add(Site site) {
		if (!this.sites.contains(site)) {
			this.sites.add(site);
			this.setDirty();
		}
	}

	public void remove(Site site) {
		if (this.sites.remove(site)) {
			this.setDirty();
		}
	}
}
