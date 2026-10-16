package dev.mehbur.garipseyler.client.render.demobat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.client.render.ModModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Ghast;

/**
 * Renders every Ghast as a Giant Demobat: the Demobat model scaled up to fill the 4×4 Ghast hitbox, with
 * slower wing beats. The petal mouth opens while the Ghast charges a fireball. Happy Ghasts are untouched.
 */
public class GiantDemobatRenderer extends MobRenderer<Ghast, GiantDemobatRenderState, DemobatModel> {
	private static final Identifier TEXTURE = GaripSeylerMod.id("textures/entity/demobat/demobat_giant.png");
	private static final float SHADOW_RADIUS = 1.5F;
	private static final float MODEL_SCALE = 2.75F;
	/** Height (blocks) of the model's body above the entity's feet; the Ghast hitbox is 4 blocks tall. */
	private static final float BODY_HEIGHT = 2.0F;
	/** The vanilla living renderer shifts models down by this much after {@link #scale}. */
	private static final float VANILLA_MODEL_OFFSET = 1.501F;
	private static final float FLAP_RATE = 0.55F;

	public GiantDemobatRenderer(EntityRendererProvider.Context context) {
		super(context, new DemobatModel(context.bakeLayer(ModModelLayers.DEMOBAT)), SHADOW_RADIUS);
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
	public void extractRenderState(Ghast entity, GiantDemobatRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.flapTime = (entity.getId() * 3 + state.ageInTicks) * FLAP_RATE;
		state.charging = entity.isCharging();
	}

	@Override
	protected void scale(GiantDemobatRenderState state, PoseStack poseStack) {
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
		// The pose stack is already flipped upside down here, so a positive y moves the model down.
		poseStack.translate(0.0F, VANILLA_MODEL_OFFSET - BODY_HEIGHT / MODEL_SCALE, 0.0F);
	}
}
