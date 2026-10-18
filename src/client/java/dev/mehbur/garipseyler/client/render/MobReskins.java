package dev.mehbur.garipseyler.client.render;

import dev.mehbur.garipseyler.client.render.demobat.BlazeDemobatRenderer;
import dev.mehbur.garipseyler.client.render.demobat.DemobatRenderer;
import dev.mehbur.garipseyler.client.render.demobat.GiantDemobatRenderer;
import dev.mehbur.garipseyler.client.render.demodog.DemodogRenderer;
import dev.mehbur.garipseyler.client.render.demodog.DemodogVariant;
import dev.mehbur.garipseyler.client.render.demogorgon.DemogorgonRenderer;
import dev.mehbur.garipseyler.client.render.demogorgon.SkeletonDemogorgonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityTypes;

/**
 * Swaps the renderers of vanilla mobs for their Upside Down look. Registering after vanilla replaces the
 * vanilla renderer for that entity type; entity behaviour is not touched.
 */
public final class MobReskins {
	private MobReskins() {
	}

	public static void register() {
		EntityRenderers.register(EntityTypes.ENDERMAN, DemogorgonRenderer::new);
		EntityRenderers.register(EntityTypes.SKELETON, context -> new SkeletonDemogorgonRenderer<>(context, "demogorgon", 1.0F));
		EntityRenderers.register(EntityTypes.STRAY, context -> new SkeletonDemogorgonRenderer<>(context, "demogorgon_frozen", 1.0F));
		EntityRenderers.register(EntityTypes.BOGGED, context -> new SkeletonDemogorgonRenderer<>(context, "demogorgon_bog", 1.0F));
		EntityRenderers.register(EntityTypes.PARCHED, context -> new SkeletonDemogorgonRenderer<>(context, "demogorgon_parched", 1.0F));
		EntityRenderers.register(EntityTypes.WITHER_SKELETON, context -> new SkeletonDemogorgonRenderer<>(context, "demogorgon_wither", 1.2F));

		EntityRenderers.register(EntityTypes.PIGLIN, context -> new DemodogRenderer<>(context, DemodogVariant.PIGLIN));
		EntityRenderers.register(EntityTypes.PIGLIN_BRUTE, context -> new DemodogRenderer<>(context, DemodogVariant.PIGLIN_BRUTE));
		EntityRenderers.register(EntityTypes.ZOMBIFIED_PIGLIN, context -> new DemodogRenderer<>(context, DemodogVariant.ZOMBIFIED_PIGLIN));
		EntityRenderers.register(EntityTypes.HOGLIN, context -> new DemodogRenderer<>(context, DemodogVariant.HOGLIN));
		EntityRenderers.register(EntityTypes.ZOGLIN, context -> new DemodogRenderer<>(context, DemodogVariant.ZOGLIN));
		EntityRenderers.register(EntityTypes.STRIDER, context -> new DemodogRenderer<>(context, DemodogVariant.STRIDER));
		EntityRenderers.register(EntityTypes.MAGMA_CUBE, context -> new DemodogRenderer<>(context, DemodogVariant.MAGMA_CUBE));

		EntityRenderers.register(EntityTypes.PHANTOM, DemobatRenderer::new);
		EntityRenderers.register(EntityTypes.GHAST, GiantDemobatRenderer::new);
		EntityRenderers.register(EntityTypes.BLAZE, BlazeDemobatRenderer::new);
	}
}
