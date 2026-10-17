package dev.mehbur.garipseyler.client.render.demobat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.client.render.ModModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Blaze;

/**
 * Renders every Blaze as an ember-colored Demobat hovering in the Blaze's hitbox. Fully lit like the vanilla
 * Blaze; the petal mouth opens while it is charged (about to shoot fireballs). Behaviour is untouched.
 */
public class BlazeDemobatRenderer extends MobRenderer<Blaze, GiantDemobatRenderState, DemobatModel> {
	private static final Identifier TEXTURE = GaripSeylerMod.id("textures/entity/demobat/demobat_blaze.png");
	private static final float SHADOW_RADIUS = 0.5F;
	private static final float MODEL_SCALE = 0.45F;
	/** Height (blocks) of the model's body above the entity's feet; the Blaze hitbox is 1.8 blocks tall. */
	private static final float BODY_HEIGHT = 1.1F;
	/** The vanilla living renderer shifts models down by this much after {@link #scale}. */
	private static final float VANILLA_MODEL_OFFSET = 1.501F;
	private static final float FLAP_RATE = 1.4F;

	public BlazeDemobatRenderer(EntityRendererProvider.Context context) {
		super(context, new DemobatModel(context.bakeLayer(ModModelLayers.DEMOBAT)), SHADOW_RADIUS);
	}

	@Override
	protected int getBlockLightLevel(Blaze entity, BlockPos pos) {
		return 15;
	}

	@Override
	public Identifier getTextureLocation(GiantDemobatRenderState state) {
		return TEXTURE;
	}

	@Override
	public GiantDemobatRenderState createRenderState() {
		return new GiantDemobatRenderState();
	}

	@Override
	public void extractRenderState(Blaze entity, GiantDemobatRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.flapTime = (entity.getId() * 3 + state.ageInTicks) * FLAP_RATE;
		state.charging = entity.isOnFire();
	}

	@Override
	protected void scale(GiantDemobatRenderState state, PoseStack poseStack) {
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
		// The pose stack is already flipped upside down here, so a positive y moves the model down.
		poseStack.translate(0.0F, VANILLA_MODEL_OFFSET - BODY_HEIGHT / MODEL_SCALE, 0.0F);
	}
}
