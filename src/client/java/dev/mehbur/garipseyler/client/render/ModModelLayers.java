package dev.mehbur.garipseyler.client.render;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.client.render.demobat.DemobatModel;
import dev.mehbur.garipseyler.client.render.demodog.DemodogModel;
import dev.mehbur.garipseyler.client.render.demogorgon.DemogorgonModel;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

/**
 * Model layer ids of the mod's entity models.
 */
public final class ModModelLayers {
	public static final ModelLayerLocation DEMOGORGON = create("demogorgon");
	public static final ModelLayerLocation DEMODOG = create("demodog");
	public static final ModelLayerLocation DEMOBAT = create("demobat");

	private ModModelLayers() {
	}

	private static ModelLayerLocation create(String name) {
		return new ModelLayerLocation(GaripSeylerMod.id(name), "main");
	}

	public static void register() {
		ModelLayerRegistry.registerModelLayer(DEMOGORGON, DemogorgonModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(DEMODOG, DemodogModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(DEMOBAT, DemobatModel::createBodyLayer);
	}
}
