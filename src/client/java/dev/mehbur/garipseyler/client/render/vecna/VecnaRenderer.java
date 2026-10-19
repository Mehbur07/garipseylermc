package dev.mehbur.garipseyler.client.render.vecna;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.mehbur.garipseyler.entity.ModEntities;
import dev.mehbur.garipseyler.entity.vecna.Vecna;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * GeckoLib renderer for Vecna. Files follow GeckoLib's defaults for {@code garipseylermc:vecna}:
 * {@code geckolib/models/entity/vecna.geo.json}, {@code geckolib/animations/entity/vecna.animation.json},
 * {@code textures/entity/vecna.png} and the emissive {@code textures/entity/vecna_glowmask.png} (eyes, veins).
 * While he emerges from the ground the whole model is drawn lower, so it rises out of the terrain.
 */
public class VecnaRenderer extends GeoEntityRenderer<Vecna, LivingEntityRenderState> {
	private static final float SHADOW_RADIUS = 0.7F;
	private static final DataTicket<Float> EMERGE_DEPTH = DataTicket.create("garipseylermc_vecna_emerge_depth", Float.class);

	public VecnaRenderer(EntityRendererProvider.Context context) {
		super(context, ModEntities.VECNA);
		this.shadowRadius = SHADOW_RADIUS;
		this.withRenderLayer(AutoGlowingGeoLayer::new);
	}

	@Override
	public void extractRenderState(Vecna entity, LivingEntityRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		((GeoRenderState) state).addGeckolibData(EMERGE_DEPTH, entity.emergeDepth(partialTicks));
	}

	@Override
	public void submit(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		float depth = ((GeoRenderState) state).getOrDefaultGeckolibData(EMERGE_DEPTH, 0.0F);
		if (depth <= 0.0F) {
			super.submit(state, poseStack, collector, camera);
			return;
		}
		poseStack.pushPose();
		poseStack.translate(0.0F, -depth, 0.0F);
		super.submit(state, poseStack, collector, camera);
		poseStack.popPose();
	}
}
