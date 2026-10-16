package dev.mehbur.garipseyler.client.render.demogorgon;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.client.render.ModModelLayers;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Renders every Enderman as a Demogorgon. Behaviour is untouched; only the look changes.
 */
public class DemogorgonRenderer extends MobRenderer<EnderMan, EndermanRenderState, EndermanModel<EndermanRenderState>> {
	private static final Identifier TEXTURE = GaripSeylerMod.id("textures/entity/demogorgon/demogorgon.png");
	private static final float SHADOW_RADIUS = 0.5F;

	private final RandomSource random = RandomSource.create();
	private final BlockModelResolver blockModelResolver;

	public DemogorgonRenderer(EntityRendererProvider.Context context) {
		super(context, new DemogorgonModel(context.bakeLayer(ModModelLayers.DEMOGORGON)), SHADOW_RADIUS);
		this.blockModelResolver = context.getBlockModelResolver();
		this.addLayer(new CarriedBlockLayer(this));
	}

	@Override
	public Vec3 getRenderOffset(EndermanRenderState state) {
		Vec3 offset = super.getRenderOffset(state);
		if (state.isCreepy) {
			double jitter = 0.02 * state.scale;
			return offset.add(this.random.nextGaussian() * jitter, 0.0, this.random.nextGaussian() * jitter);
		}
		return offset;
	}

	@Override
	public Identifier getTextureLocation(EndermanRenderState state) {
		return TEXTURE;
	}

	@Override
	public EndermanRenderState createRenderState() {
		return new EndermanRenderState();
	}

	@Override
	public void extractRenderState(EnderMan entity, EndermanRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
		state.isCreepy = entity.isCreepy();
		BlockState carriedBlock = entity.getCarriedBlock();
		if (carriedBlock != null) {
			this.blockModelResolver.update(state.carriedBlock, carriedBlock, EndermanRenderer.BLOCK_DISPLAY_CONTEXT);
		} else {
			state.carriedBlock.clear();
		}
	}
}
