package dev.mehbur.garipseyler.client.render.demobat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.client.render.ModModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Phantom;

/**
 * Renders every Phantom as a Demobat (same transforms as the vanilla Phantom renderer, no glowing eyes).
 */
public class DemobatRenderer extends MobRenderer<Phantom, PhantomRenderState, DemobatModel> {
	private static final Identifier TEXTURE = GaripSeylerMod.id("textures/entity/demobat/demobat.png");
	private static final float SHADOW_RADIUS = 0.75F;

	public DemobatRenderer(EntityRendererProvider.Context context) {
		super(context, new DemobatModel(context.bakeLayer(ModModelLayers.DEMOBAT)), SHADOW_RADIUS);
	}

	@Override
	public Identifier getTextureLocation(PhantomRenderState state) {
		return TEXTURE;
	}

	@Override
	public PhantomRenderState createRenderState() {
		return new PhantomRenderState();
	}

	@Override
	public void extractRenderState(Phantom entity, PhantomRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.flapTime = entity.getUniqueFlapTickOffset() + state.ageInTicks;
		state.size = entity.getPhantomSize();
	}

	@Override
	protected void scale(PhantomRenderState state, PoseStack poseStack) {
		float scale = 1.0F + 0.15F * state.size;
		poseStack.scale(scale, scale, scale);
		poseStack.translate(0.0F, 1.3125F, 0.1875F);
	}

	@Override
	protected void setupRotations(PhantomRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
		super.setupRotations(state, poseStack, bodyRot, entityScale);
		poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
	}
}
