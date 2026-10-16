package dev.mehbur.garipseyler.client.render;

import dev.mehbur.garipseyler.client.render.demobat.DemobatRenderer;
import dev.mehbur.garipseyler.client.render.demodog.DemodogRenderer;
import dev.mehbur.garipseyler.client.render.demodog.DemodogVariant;
import dev.mehbur.garipseyler.client.render.demogorgon.DemogorgonRenderer;
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

		EntityRenderers.register(EntityTypes.PIGLIN, context -> new DemodogRenderer<>(context, DemodogVariant.PIGLIN));
		EntityRenderers.register(EntityTypes.PIGLIN_BRUTE, context -> new DemodogRenderer<>(context, DemodogVariant.PIGLIN_BRUTE));
		EntityRenderers.register(EntityTypes.ZOMBIFIED_PIGLIN, context -> new DemodogRenderer<>(context, DemodogVariant.ZOMBIFIED_PIGLIN));
		EntityRenderers.register(EntityTypes.HOGLIN, context -> new DemodogRenderer<>(context, DemodogVariant.HOGLIN));
		EntityRenderers.register(EntityTypes.ZOGLIN, context -> new DemodogRenderer<>(context, DemodogVariant.ZOGLIN));

		EntityRenderers.register(EntityTypes.PHANTOM, DemobatRenderer::new);
	}
}
