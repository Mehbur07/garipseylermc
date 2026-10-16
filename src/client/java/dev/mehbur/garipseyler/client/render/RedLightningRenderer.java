package dev.mehbur.garipseyler.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.mehbur.garipseyler.entity.RedLightning;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4fc;

/**
 * Vanilla lightning bolt geometry in Upside Down red, squashed vertically so it spans exactly from the
 * cave floor to the ceiling.
 */
public class RedLightningRenderer extends EntityRenderer<RedLightning, RedLightningRenderState> {
	private static final float RED = 0.95F;
	private static final float GREEN = 0.12F;
	private static final float BLUE = 0.14F;
	/** Vanilla uses 0.3; a bit stronger so the bolt cuts through the dense Upside Down fog. */
	private static final float ALPHA = 0.45F;
	/** Lower bound for the horizontal zigzag scale of very short bolts. */
	private static final float MIN_SPREAD = 0.1F;

	public RedLightningRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void submit(RedLightningRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		if (!state.flashing) {
			return;
		}

		float[] xOffs = new float[8];
		float[] zOffs = new float[8];
		float xOff = 0.0F;
		float zOff = 0.0F;
		RandomSource random = RandomSource.createThreadLocalInstance(state.seed);

		for (int h = 7; h >= 0; h--) {
			xOffs[h] = xOff;
			zOffs[h] = zOff;
			xOff += random.nextInt(11) - 5;
			zOff += random.nextInt(11) - 5;
		}

		float finalXOff = xOff;
		float finalZOff = zOff;
		// Shrink the zigzag together with the height so short cave bolts stay vertical; thickness is kept.
		float spread = Math.max(state.heightScale, MIN_SPREAD);
		poseStack.pushPose();
		poseStack.scale(1.0F, state.heightScale, 1.0F);
		submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
			Matrix4fc poseMatrix = pose.pose();

			for (int r = 0; r < 4; r++) {
				RandomSource branchRandom = RandomSource.createThreadLocalInstance(state.seed);

				for (int p = 0; p < 3; p++) {
					int hs = p > 0 ? 7 - p : 7;
					int ht = p > 0 ? hs - 2 : 0;

					float xo0 = xOffs[hs] - finalXOff;
					float zo0 = zOffs[hs] - finalZOff;

					for (int h = hs; h >= ht; h--) {
						float xo1 = xo0;
						float zo1 = zo0;
						if (p == 0) {
							xo0 += branchRandom.nextInt(11) - 5;
							zo0 += branchRandom.nextInt(11) - 5;
						} else {
							xo0 += branchRandom.nextInt(31) - 15;
							zo0 += branchRandom.nextInt(31) - 15;
						}

						float rr1 = 0.1F + r * 0.2F;
						if (p == 0) {
							rr1 *= h * 0.1F + 1.0F;
						}

						float rr2 = 0.1F + r * 0.2F;
						if (p == 0) {
							rr2 *= (h - 1.0F) * 0.1F + 1.0F;
						}

						float x0 = xo0 * spread;
						float z0 = zo0 * spread;
						float x1 = xo1 * spread;
						float z1 = zo1 * spread;
						quad(poseMatrix, buffer, x0, z0, h, x1, z1, rr1, rr2, false, false, true, false);
						quad(poseMatrix, buffer, x0, z0, h, x1, z1, rr1, rr2, true, false, true, true);
						quad(poseMatrix, buffer, x0, z0, h, x1, z1, rr1, rr2, true, true, false, true);
						quad(poseMatrix, buffer, x0, z0, h, x1, z1, rr1, rr2, false, true, false, false);
					}
				}
			}
		});
		poseStack.popPose();
	}

	private static void quad(Matrix4fc pose, VertexConsumer buffer, float xo0, float zo0, int h, float xo1, float zo1,
			float rr1, float rr2, boolean px1, boolean pz1, boolean px2, boolean pz2) {
		buffer.addVertex(pose, xo0 + (px1 ? rr2 : -rr2), h * 16, zo0 + (pz1 ? rr2 : -rr2)).setColor(RED, GREEN, BLUE, ALPHA);
		buffer.addVertex(pose, xo1 + (px1 ? rr1 : -rr1), (h + 1) * 16, zo1 + (pz1 ? rr1 : -rr1)).setColor(RED, GREEN, BLUE, ALPHA);
		buffer.addVertex(pose, xo1 + (px2 ? rr1 : -rr1), (h + 1) * 16, zo1 + (pz2 ? rr1 : -rr1)).setColor(RED, GREEN, BLUE, ALPHA);
		buffer.addVertex(pose, xo0 + (px2 ? rr2 : -rr2), h * 16, zo0 + (pz2 ? rr2 : -rr2)).setColor(RED, GREEN, BLUE, ALPHA);
	}

	@Override
	public RedLightningRenderState createRenderState() {
		return new RedLightningRenderState();
	}

	@Override
	public void extractRenderState(RedLightning entity, RedLightningRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.seed = entity.getSeed();
		state.heightScale = entity.getBoltHeight() / RedLightning.VANILLA_BOLT_HEIGHT;
		state.flashing = entity.isFlashing();
	}

	@Override
	protected boolean affectedByCulling(RedLightning entity) {
		return false;
	}
}
