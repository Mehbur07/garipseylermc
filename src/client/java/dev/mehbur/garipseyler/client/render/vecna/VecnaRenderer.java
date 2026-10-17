package dev.mehbur.garipseyler.client.render.vecna;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import dev.mehbur.garipseyler.entity.ModEntities;
import dev.mehbur.garipseyler.entity.vecna.Vecna;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * GeckoLib renderer for Vecna. Files follow GeckoLib's defaults for {@code garipseylermc:vecna}:
 * {@code geckolib/models/entity/vecna.geo.json}, {@code geckolib/animations/entity/vecna.animation.json},
 * {@code textures/entity/vecna.png} and the emissive {@code textures/entity/vecna_glowmask.png} (eyes, veins).
 */
public class VecnaRenderer extends GeoEntityRenderer<Vecna, LivingEntityRenderState> {
	private static final float SHADOW_RADIUS = 0.7F;

	public VecnaRenderer(EntityRendererProvider.Context context) {
		super(context, ModEntities.VECNA);
		this.shadowRadius = SHADOW_RADIUS;
		this.withRenderLayer(AutoGlowingGeoLayer::new);
	}
}
