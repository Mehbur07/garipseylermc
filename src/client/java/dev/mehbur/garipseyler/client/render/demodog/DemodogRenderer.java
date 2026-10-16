package dev.mehbur.garipseyler.client.render.demodog;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.mehbur.garipseyler.client.render.ModModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

/**
 * Renders Piglins, Piglin Brutes, Zombified Piglins, Hoglins and Zoglins as Demodogs. Behaviour, health and
 * hitboxes stay vanilla; only the look changes. Babies are drawn at half size.
 */
public class DemodogRenderer<T extends Mob> extends MobRenderer<T, DemodogRenderState, DemodogModel> {
	private static final float BASE_SHADOW_RADIUS = 0.5F;

	private final DemodogVariant variant;

	public DemodogRenderer(EntityRendererProvider.Context context, DemodogVariant variant) {
		super(context, new DemodogModel(context.bakeLayer(ModModelLayers.DEMODOG)), BASE_SHADOW_RADIUS * variant.scale());
		this.variant = variant;
	}

	@Override
	public Identifier getTextureLocation(DemodogRenderState state) {
		return this.variant.texture();
	}

	@Override
	public DemodogRenderState createRenderState() {
		return new DemodogRenderState();
	}

	@Override
	public void extractRenderState(T entity, DemodogRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.aggressive = entity.isAggressive();
		boolean headButting = entity instanceof HoglinBase hoglin && hoglin.getAttackAnimationRemainingTicks() > 0;
		state.attacking = headButting || entity.getAttackAnim(partialTicks) > 0.0F;
		state.converting = (entity instanceof AbstractPiglin piglin && piglin.isConverting())
				|| (entity instanceof Hoglin hoglin && hoglin.isConverting());
	}

	@Override
	protected void scale(DemodogRenderState state, PoseStack poseStack) {
		float scale = this.variant.scale() * state.ageScale;
		poseStack.scale(scale, scale, scale);
	}

	@Override
	protected boolean isShaking(DemodogRenderState state) {
		return super.isShaking(state) || state.converting;
	}
}
