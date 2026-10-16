package dev.mehbur.garipseyler.client.render.demodog;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Demodog: a lean four-legged hunter with a long tail and a four-petal mouth that opens when it attacks.
 */
public class DemodogModel extends EntityModel<DemodogRenderState> {
	private static final int PETAL_COUNT = 4;
	private static final float PETAL_CLOSED = 0.35F;
	private static final float PETAL_AGGRESSIVE = -0.45F;
	private static final float PETAL_ATTACK = -1.0F;
	private static final float DEG_TO_RAD = Mth.DEG_TO_RAD;

	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;
	private final ModelPart[] petals = new ModelPart[PETAL_COUNT];

	public DemodogModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.tail = root.getChild("tail");
		this.rightHindLeg = root.getChild("right_hind_leg");
		this.leftHindLeg = root.getChild("left_hind_leg");
		this.rightFrontLeg = root.getChild("right_front_leg");
		this.leftFrontLeg = root.getChild("left_front_leg");
		for (int i = 0; i < PETAL_COUNT; i++) {
			this.petals[i] = this.head.getChild("petal_holder_" + i).getChild("petal");
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.5F, -8.0F, 7.0F, 7.0F, 16.0F),
				PartPose.offset(0.0F, 11.0F, 0.0F));

		PartDefinition head = root.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 24).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 6.0F, 5.0F),
				PartPose.offset(0.0F, 9.0F, -8.0F));
		for (int i = 0; i < PETAL_COUNT; i++) {
			// Holders spin the petal around the snout axis: top, right, bottom, left.
			PartDefinition holder = head.addOrReplaceChild("petal_holder_" + i, CubeListBuilder.create(),
					PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, 0.0F, 0.0F, i * Mth.HALF_PI));
			holder.addOrReplaceChild("petal",
					CubeListBuilder.create().texOffs(24, 24).addBox(-2.0F, -0.5F, -5.0F, 4.0F, 1.0F, 5.0F),
					PartPose.offsetAndRotation(0.0F, -2.5F, 0.0F, PETAL_CLOSED, 0.0F, 0.0F));
		}

		root.addOrReplaceChild("tail",
				CubeListBuilder.create().texOffs(0, 36).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 10.0F),
				PartPose.offsetAndRotation(0.0F, 9.0F, 8.0F, -0.5F, 0.0F, 0.0F));

		CubeListBuilder leg = CubeListBuilder.create().texOffs(48, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F);
		CubeListBuilder mirroredLeg = CubeListBuilder.create().mirror().texOffs(48, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F);
		root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-2.5F, 13.0F, 6.0F));
		root.addOrReplaceChild("left_hind_leg", mirroredLeg, PartPose.offset(2.5F, 13.0F, 6.0F));
		root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-2.5F, 13.0F, -6.0F));
		root.addOrReplaceChild("left_front_leg", mirroredLeg, PartPose.offset(2.5F, 13.0F, -6.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(DemodogRenderState state) {
		super.setupAnim(state);

		this.head.yRot = state.yRot * DEG_TO_RAD;
		this.head.xRot = state.xRot * DEG_TO_RAD;

		// Classic quadruped trot: diagonal legs move together.
		float swing = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
		this.rightHindLeg.xRot = swing;
		this.leftFrontLeg.xRot = swing;
		this.leftHindLeg.xRot = -swing;
		this.rightFrontLeg.xRot = -swing;

		this.tail.yRot = Mth.sin(state.ageInTicks * 0.15F) * 0.25F;

		float open;
		if (state.attacking) {
			open = PETAL_ATTACK;
		} else if (state.aggressive) {
			open = PETAL_AGGRESSIVE + Mth.sin(state.ageInTicks * 0.6F) * 0.15F;
		} else {
			open = PETAL_CLOSED + Mth.sin(state.ageInTicks * 0.08F) * 0.05F;
		}
		for (ModelPart petal : this.petals) {
			petal.xRot = open;
		}
	}
}
