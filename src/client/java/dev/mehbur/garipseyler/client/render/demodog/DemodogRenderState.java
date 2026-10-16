package dev.mehbur.garipseyler.client.render.demodog;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class DemodogRenderState extends LivingEntityRenderState {
	public boolean aggressive;
	/** True while a melee/head-butt attack animation is playing. */
	public boolean attacking;
	/** Piglins and hoglins shake while zombifying in the Overworld. */
	public boolean converting;
}
