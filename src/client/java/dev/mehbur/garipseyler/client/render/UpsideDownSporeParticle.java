package dev.mehbur.garipseyler.client.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Slowly drifting, softly fading spore flakes of the Upside Down.
 */
public class UpsideDownSporeParticle extends SingleQuadParticle {
	private static final int FADE_IN_TICKS = 20;
	private static final int FADE_OUT_TICKS = 30;
	private static final float MAX_ALPHA = 0.85F;

	private final float swayPhase;

	protected UpsideDownSporeParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
		super(level, x, y, z, sprite);
		this.lifetime = 100 + this.random.nextInt(100);
		this.quadSize *= 0.5F + this.random.nextFloat() * 0.6F;
		this.gravity = 0.0F;
		this.friction = 1.0F;
		this.hasPhysics = false;
		this.xd = this.random.nextGaussian() * 0.004;
		this.yd = (this.random.nextFloat() - 0.3F) * 0.006;
		this.zd = this.random.nextGaussian() * 0.004;
		this.roll = this.oRoll = this.random.nextFloat() * Mth.TWO_PI;
		this.swayPhase = this.random.nextFloat() * Mth.TWO_PI;

		float shade = 0.75F + this.random.nextFloat() * 0.2F;
		this.setColor(shade, shade * 0.92F, shade * 0.92F);
		this.setAlpha(0.0F);
	}

	@Override
	public void tick() {
		super.tick();
		this.oRoll = this.roll;
		this.roll += 0.01F;
		this.xd += Mth.sin(this.age * 0.05F + this.swayPhase) * 0.0002;

		float fadeIn = Math.min(1.0F, this.age / (float) FADE_IN_TICKS);
		float fadeOut = Math.min(1.0F, (this.lifetime - this.age) / (float) FADE_OUT_TICKS);
		this.setAlpha(MAX_ALPHA * Math.min(fadeIn, fadeOut));
	}

	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z,
				double xAux, double yAux, double zAux, RandomSource random) {
			return new UpsideDownSporeParticle(level, x, y, z, this.sprites.get(random));
		}
	}
}
