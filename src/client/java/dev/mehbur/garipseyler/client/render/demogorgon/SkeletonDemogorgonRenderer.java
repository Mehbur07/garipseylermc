package dev.mehbur.garipseyler.client.render.demogorgon;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.client.render.ModModelLayers;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

/**
 * Renders skeletons (Skeleton, Stray, Bogged, Parched, Wither Skeleton) as Demogorgons with the Demogorgon model.
 * The petal mouth opens while the skeleton is aggressive (aiming or attacking). Held bows are not drawn; arrows
 * still fly. Behaviour is untouched.
 */
public class SkeletonDemogorgonRenderer<T extends AbstractSkeleton> extends MobRenderer<T, EndermanRenderState, EndermanModel<EndermanRenderState>> {
	private static final float SHADOW_RADIUS = 0.5F;

	private final Identifier texture;
	private final float modelScale;

	public SkeletonDemogorgonRenderer(EntityRendererProvider.Context context, String texture, float modelScale) {
		super(context, new DemogorgonModel(context.bakeLayer(ModModelLayers.DEMOGORGON)), SHADOW_RADIUS * modelScale);
		this.texture = GaripSeylerMod.id("textures/entity/demogorgon/" + texture + ".png");
		this.modelScale = modelScale;
	}

	@Override
	public Identifier getTextureLocation(EndermanRenderState state) {
		return this.texture;
	}

	@Override
	public EndermanRenderState createRenderState() {
		return new EndermanRenderState();
	}

	@Override
	public void extractRenderState(T entity, EndermanRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
		state.isCreepy = entity.isAggressive();
		state.carriedBlock.clear();
	}

	@Override
	protected void scale(EndermanRenderState state, PoseStack poseStack) {
		if (this.modelScale != 1.0F) {
			poseStack.scale(this.modelScale, this.modelScale, this.modelScale);
		}
	}
}
