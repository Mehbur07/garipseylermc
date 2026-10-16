package dev.mehbur.garipseyler.client.render.demobat;

import net.minecraft.client.renderer.entity.state.PhantomRenderState;

/**
 * Render state of a Ghast drawn as a Giant Demobat. Reuses the Phantom state so the Demobat model can
 * animate it; {@link #charging} opens the petal mouth while a fireball is being charged.
 */
public class GiantDemobatRenderState extends PhantomRenderState {
	public boolean charging;
}
