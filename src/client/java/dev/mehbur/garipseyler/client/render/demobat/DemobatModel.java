package dev.mehbur.garipseyler.client.render.demobat;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.util.Mth;

/**
 * Demobat: a bat-like flyer with bony membrane wings, a petal mouth and a whip tail. Uses the same layout
 * and flap timing as the vanilla Phantom model so the Phantom renderer transforms still line up.
 */
public class DemobatModel extends EntityModel<PhantomRenderState> {
	private static final int PETAL_COUNT = 4;
	private static final float FLAP_SPEED = 7.448451F * Mth.DEG_TO_RAD;
	private static final float FLAP_AMOUNT = 22.0F * Mth.DEG_TO_RAD;

	private final ModelPart leftWingBase;
	private final ModelPart leftWingTip;
	private final ModelPart rightWingBase;
	private final ModelPart rightWingTip;
	private final ModelPart tailBase;
	private final ModelPart tailTip;
	private final ModelPart[] petals = new ModelPart[PETAL_COUNT];

	public DemobatModel(ModelPart root) {
		super(root);
		ModelPart body = root.getChild("body");
		this.tailBase = body.getChild("tail_base");
		this.tailTip = this.tailBase.getChild("tail_tip");
		this.leftWingBase = body.getChild("left_wing_base");
		this.leftWingTip = this.leftWingBase.getChild("left_wing_tip");
		this.rightWingBase = body.getChild("right_wing_base");
		this.rightWingTip = this.rightWingBase.getChild("right_wing_tip");
		ModelPart head = body.getChild("head");
		for (int i = 0; i < PETAL_COUNT; i++) {
			this.petals[i] = head.getChild("petal_holder_" + i).getChild("petal");
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition body = root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.0F, -8.0F, 5.0F, 4.0F, 9.0F),
				PartPose.rotation(-0.1F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 13).addBox(-2.5F, -2.0F, -4.0F, 5.0F, 4.0F, 4.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, -8.0F, 0.2F, 0.0F, 0.0F));
		for (int i = 0; i < PETAL_COUNT; i++) {
			PartDefinition holder = head.addOrReplaceChild("petal_holder_" + i, CubeListBuilder.create(),
					PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 0.0F, 0.0F, i * Mth.HALF_PI));
			holder.addOrReplaceChild("petal",
					CubeListBuilder.create().texOffs(18, 13).addBox(-1.5F, -0.5F, -3.0F, 3.0F, 1.0F, 3.0F),
					PartPose.offsetAndRotation(0.0F, -1.5F, 0.0F, 0.3F, 0.0F, 0.0F));
		}

		PartDefinition tailBase = body.addOrReplaceChild("tail_base",
				CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 7.0F),
				PartPose.offset(0.0F, -1.0F, 1.0F));
		tailBase.addOrReplaceChild("tail_tip",
				CubeListBuilder.create().texOffs(0, 30).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 8.0F)
						.texOffs(18, 30).addBox(-1.5F, 0.0F, 7.0F, 3.0F, 1.0F, 2.0F),
				PartPose.offset(0.0F, 0.5F, 7.0F));

		// Wing = bony arm along the front edge plus a thin membrane behind it.
		PartDefinition leftWingBase = body.addOrReplaceChild("left_wing_base",
				CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, -0.5F, 0.0F, 7.0F, 1.0F, 1.0F)
						.texOffs(24, 2).addBox(0.0F, 0.0F, 1.0F, 7.0F, 0.0F, 9.0F),
				PartPose.offsetAndRotation(2.0F, -1.5F, -7.0F, 0.0F, 0.0F, 0.1F));
		leftWingBase.addOrReplaceChild("left_wing_tip",
				CubeListBuilder.create().texOffs(24, 11).addBox(0.0F, -0.5F, 0.0F, 14.0F, 1.0F, 1.0F)
						.texOffs(24, 13).addBox(0.0F, 0.0F, 1.0F, 14.0F, 0.0F, 11.0F),
				PartPose.offsetAndRotation(7.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1F));
		PartDefinition rightWingBase = body.addOrReplaceChild("right_wing_base",
				CubeListBuilder.create().mirror().texOffs(24, 0).addBox(-7.0F, -0.5F, 0.0F, 7.0F, 1.0F, 1.0F)
						.texOffs(24, 2).addBox(-7.0F, 0.0F, 1.0F, 7.0F, 0.0F, 9.0F),
				PartPose.offsetAndRotation(-2.0F, -1.5F, -7.0F, 0.0F, 0.0F, -0.1F));
		rightWingBase.addOrReplaceChild("right_wing_tip",
				CubeListBuilder.create().mirror().texOffs(24, 11).addBox(-14.0F, -0.5F, 0.0F, 14.0F, 1.0F, 1.0F)
						.texOffs(24, 13).addBox(-14.0F, 0.0F, 1.0F, 14.0F, 0.0F, 11.0F),
				PartPose.offsetAndRotation(-7.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(PhantomRenderState state) {
		super.setupAnim(state);
		float anim = state.flapTime * FLAP_SPEED;
		this.leftWingBase.zRot = Mth.cos(anim) * FLAP_AMOUNT;
		this.leftWingTip.zRot = Mth.cos(anim - 0.6F) * FLAP_AMOUNT;
		this.rightWingBase.zRot = -this.leftWingBase.zRot;
		this.rightWingTip.zRot = -this.leftWingTip.zRot;
		this.tailBase.xRot = -(5.0F + Mth.cos(anim * 2.0F) * 5.0F) * Mth.DEG_TO_RAD;
		this.tailTip.xRot = -(5.0F + Mth.cos(anim * 2.0F) * 8.0F) * Mth.DEG_TO_RAD;
		this.tailTip.yRot = Mth.sin(anim) * 0.2F;

		// The petal mouth snaps open and shut every few seconds.
		float snap = Mth.clamp(Mth.sin(state.ageInTicks * 0.12F) * 3.0F - 2.0F, 0.0F, 1.0F);
		for (ModelPart petal : this.petals) {
			petal.xRot = Mth.lerp(snap, 0.3F, -0.8F);
		}
	}
}
