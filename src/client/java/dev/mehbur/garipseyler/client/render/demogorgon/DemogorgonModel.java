package dev.mehbur.garipseyler.client.render.demogorgon;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.util.Mth;

/**
 * Demogorgon: a gaunt humanoid whose head is a five-petal flower mouth. It keeps the part names of the
 * vanilla Enderman model, so Enderman walking/attack animations and the carried-block layer still work.
 * The petals are closed while calm and open wide when the Enderman is angry ("creepy").
 */
public class DemogorgonModel extends EndermanModel<EndermanRenderState> {
	public static final int PETAL_COUNT = 5;
	private static final float PETAL_CLOSED = -0.35F;
	private static final float PETAL_OPEN = 1.25F;
	private static final float ENDERMAN_CREEPY_HEAD_LIFT = 5.0F;

	private final ModelPart[] petals = new ModelPart[PETAL_COUNT];

	public DemogorgonModel(ModelPart root) {
		super(root);
		for (int i = 0; i < PETAL_COUNT; i++) {
			this.petals[i] = this.head.getChild("petal_holder_" + i).getChild("petal");
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		// Head = short neck stalk; the petals form the actual "face".
		PartDefinition head = root.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F),
				PartPose.offset(0.0F, -13.0F, 0.0F));
		head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		for (int i = 0; i < PETAL_COUNT; i++) {
			PartDefinition holder = head.addOrReplaceChild("petal_holder_" + i, CubeListBuilder.create(),
					PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, i * Mth.TWO_PI / PETAL_COUNT, 0.0F));
			holder.addOrReplaceChild("petal",
					CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -7.0F, 0.0F, 4.0F, 7.0F, 1.0F),
					PartPose.offsetAndRotation(0.0F, 0.0F, -1.5F, PETAL_CLOSED, 0.0F, 0.0F));
		}

		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(16, 0).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 13.0F, 5.0F),
				PartPose.offset(0.0F, -13.0F, 0.0F));

		CubeListBuilder arm = CubeListBuilder.create()
				.texOffs(0, 20).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 24.0F, 3.0F)
				.texOffs(12, 20).addBox(-1.5F, 22.0F, -2.0F, 3.0F, 4.0F, 1.0F);
		CubeListBuilder mirroredArm = CubeListBuilder.create().mirror()
				.texOffs(0, 20).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 24.0F, 3.0F)
				.texOffs(12, 20).addBox(-1.5F, 22.0F, -2.0F, 3.0F, 4.0F, 1.0F);
		root.addOrReplaceChild("right_arm", arm, PartPose.offset(-5.5F, -11.0F, 0.0F));
		root.addOrReplaceChild("left_arm", mirroredArm, PartPose.offset(5.5F, -11.0F, 0.0F));

		CubeListBuilder leg = CubeListBuilder.create()
				.texOffs(24, 20).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 24.0F, 3.0F)
				.texOffs(36, 20).addBox(-2.0F, 22.0F, -3.5F, 4.0F, 2.0F, 5.0F);
		CubeListBuilder mirroredLeg = CubeListBuilder.create().mirror()
				.texOffs(24, 20).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 24.0F, 3.0F)
				.texOffs(36, 20).addBox(-2.0F, 22.0F, -3.5F, 4.0F, 2.0F, 5.0F);
		root.addOrReplaceChild("right_leg", leg, PartPose.offset(-2.0F, 0.0F, 0.0F));
		root.addOrReplaceChild("left_leg", mirroredLeg, PartPose.offset(2.0F, 0.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(EndermanRenderState state) {
		super.setupAnim(state);
		if (state.isCreepy) {
			// The Enderman pose lifts the head 5px when angry; keep the Demogorgon's neck attached instead.
			this.head.y += ENDERMAN_CREEPY_HEAD_LIFT;
		}
		// Calm: petals breathe slightly. Angry: they open wide and quiver.
		float open = state.isCreepy
				? PETAL_OPEN + Mth.sin(state.ageInTicks * 0.8F) * 0.08F
				: PETAL_CLOSED + Mth.sin(state.ageInTicks * 0.05F) * 0.05F;
		for (ModelPart petal : this.petals) {
			petal.xRot = open;
		}
	}
}
