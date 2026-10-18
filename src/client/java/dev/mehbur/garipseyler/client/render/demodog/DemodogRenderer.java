package dev.mehbur.garipseyler.client.render.demodog;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.mehbur.garipseyler.client.render.ModModelLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

/**
 * Renders Piglins, Piglin Brutes, Zombified Piglins, Hoglins, Zoglins, Striders and Magma Cubes as Demodogs. Behaviour,
 * health and hitboxes stay vanilla; only the look changes. Babies are drawn at half size; Magma Cubes are sized by
 * their cube size and stretch/squash with their hops like the vanilla cube.
 */
public class DemodogRenderer<T extends Mob> extends MobRenderer<T, DemodogRenderState, DemodogModel> {
	private static final float BASE_SHADOW_RADIUS = 0.5F;
	/** Model scale per cube size: size 4 (2.08-block hitbox) draws a ~1.9-block-tall Demodog. */
	private static final float CUBE_SCALE_PER_SIZE = 0.42F;
	/** Squish above this (launching into a hop) opens the petal mouth wide. */
	private static final float LEAP_SQUISH = 0.5F;

	private final DemodogVariant variant;

	public DemodogRenderer(EntityRendererProvider.Context context, DemodogVariant variant) {
		super(context, new DemodogModel(context.bakeLayer(ModModelLayers.DEMODOG)), BASE_SHADOW_RADIUS * variant.scale());
		this.variant = variant;
	}

	@Override
	public Identifier getTextureLocation(DemodogRenderState state) {
		return state.cold ? this.variant.coldTexture() : this.variant.texture();
	}

	@Override
	public DemodogRenderState createRenderState() {
		return new DemodogRenderState();
	}

	@Override
	public void extractRenderState(T entity, DemodogRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		if (entity instanceof AbstractCubeMob cube) {
			state.cubeSize = cube.getSize();
			state.squish = Mth.lerp(partialTicks, cube.oSquish, cube.squish);
		} else {
			state.cubeSize = 0;
			state.squish = 0.0F;
		}
		// Magma Cubes are always hostile and never set the aggressive flag; they leap with the mouth wide open.
		state.aggressive = entity.isAggressive() || state.cubeSize > 0;
		boolean headButting = entity instanceof HoglinBase hoglin && hoglin.getAttackAnimationRemainingTicks() > 0;
		boolean leaping = state.squish > LEAP_SQUISH;
		state.attacking = headButting || leaping || entity.getAttackAnim(partialTicks) > 0.0F;
		state.converting = (entity instanceof AbstractPiglin piglin && piglin.isConverting())
				|| (entity instanceof Hoglin hoglin && hoglin.isConverting());
		state.saddled = entity.isSaddled();
		state.cold = entity instanceof Strider strider && strider.isSuffocating();
	}

	@Override
	protected void scale(DemodogRenderState state, PoseStack poseStack) {
		float scale = this.variant.scale() * state.ageScale;
		if (state.cubeSize > 0) {
			// Same stretch/squash as the vanilla cube renderer, on top of the size scale.
			float size = state.cubeSize;
			float squish = state.squish / (size * 0.5F + 1.0F);
			float width = 1.0F / (squish + 1.0F);
			scale *= CUBE_SCALE_PER_SIZE * size;
			poseStack.scale(width * scale, scale / width, width * scale);
			return;
		}
		poseStack.scale(scale, scale, scale);
	}

	@Override
	protected float getShadowRadius(DemodogRenderState state) {
		return state.cubeSize > 0 ? state.cubeSize * 0.25F : super.getShadowRadius(state);
	}

	@Override
	protected int getBlockLightLevel(T entity, BlockPos blockPos) {
		return this.variant.fullBright() ? 15 : super.getBlockLightLevel(entity, blockPos);
	}

	@Override
	protected boolean isShaking(DemodogRenderState state) {
		return super.isShaking(state) || state.converting || state.cold;
	}
}
