package dev.mehbur.garipseyler;

import dev.mehbur.garipseyler.block.ModBlocks;
import dev.mehbur.garipseyler.block.VineGrowth;
import dev.mehbur.garipseyler.command.ModCommands;
import dev.mehbur.garipseyler.config.ConfigManager;
import dev.mehbur.garipseyler.dimension.DimensionAtmosphere;
import dev.mehbur.garipseyler.dimension.DimensionTitles;
import dev.mehbur.garipseyler.dimension.RedLightningScheduler;
import dev.mehbur.garipseyler.effect.ModAttachments;
import dev.mehbur.garipseyler.effect.ModEffects;
import dev.mehbur.garipseyler.effect.ModParticles;
import dev.mehbur.garipseyler.effect.ModSounds;
import dev.mehbur.garipseyler.effect.VineDrain;
import dev.mehbur.garipseyler.entity.DemobatSpawns;
import dev.mehbur.garipseyler.entity.ModEntities;
import dev.mehbur.garipseyler.entity.vecna.PlayerHolds;
import dev.mehbur.garipseyler.entity.vecna.VecnaSpawning;
import dev.mehbur.garipseyler.item.ModItems;
import dev.mehbur.garipseyler.item.Telekinesis;
import dev.mehbur.garipseyler.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (client + server) entrypoint of Garip Şeyler MC.
 */
public class GaripSeylerMod implements ModInitializer {
	public static final String MOD_ID = "garipseylermc";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ConfigManager.load();
		ModSounds.init();
		ModParticles.init();
		ModBlocks.init();
		ModItems.init();
		ModAttachments.init();
		ModEffects.init();
		ModEntities.init();
		DemobatSpawns.register();
		ModNetworking.register();
		DimensionAtmosphere.register();
		DimensionTitles.register();
		RedLightningScheduler.register();
		VineGrowth.register();
		VineDrain.register();
		VecnaSpawning.register();
		PlayerHolds.register();
		Telekinesis.register();
		ModCommands.register();

		LOGGER.info("Garip Şeyler MC initialized — welcome to the Upside Down.");
	}

	/** Builds an identifier in this mod's namespace. */
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
