package dev.mehbur.garipseyler.client.render.mindflayer;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import dev.mehbur.garipseyler.entity.ModEntities;
import dev.mehbur.garipseyler.entity.mindflayer.MindFlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.phys.AABB;

/**
 * GeckoLib renderer for the Mind Flayer. Files follow GeckoLib's defaults for {@code garipseylermc:mind_flayer}:
 * {@code geckolib/models/entity/mind_flayer.geo.json}, {@code geckolib/animations/entity/mind_flayer.animation.json},
 * {@code textures/entity/mind_flayer.png} and the emissive {@code textures/entity/mind_flayer_glowmask.png} (cracks, mouth).
 */
public class MindFlayerRenderer extends GeoEntityRenderer<MindFlayer, LivingEntityRenderState> {
	/** Model units are drawn 8x: one model unit is half a block, so the ~98 unit model stands ~49 blocks tall. */
	public static final float SCALE = 8.0F;
	private static final float SHADOW_RADIUS = 9.0F;

	public MindFlayerRenderer(EntityRendererProvider.Context context) {
		super(context, ModEntities.MIND_FLAYER);
		this.shadowRadius = SHADOW_RADIUS;
		this.withScale(SCALE);
		this.withRenderLayer(AutoGlowingGeoLayer::new);
	}

	/** It crumbles in place (death animation) instead of tipping over like a normal mob. */
	@Override
	protected float getDeathMaxRotation(GeoRenderState renderState) {
		return 0.0F;
	}

	/** The legs reach far past the hitbox; keep drawing it while any of it is on screen. */
	@Override
	protected AABB getBoundingBoxForCulling(MindFlayer entity) {
		return entity.getBoundingBox().inflate(26.0, 14.0, 26.0);
	}
}
