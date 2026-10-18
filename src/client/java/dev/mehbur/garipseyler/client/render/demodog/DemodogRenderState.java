package dev.mehbur.garipseyler.client.render.demodog;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class DemodogRenderState extends LivingEntityRenderState {
	public boolean aggressive;
	/** True while a melee/head-butt attack animation is playing. */
	public boolean attacking;
	/** Piglins and hoglins shake while zombifying in the Overworld. */
	public boolean converting;
	/** Saddled Striders show a saddle on the back. */
	public boolean saddled;
	/** Striders out of lava are cold: grey-purple and shivering. */
	public boolean cold;
	/** Cube size (1, 2, 4) of a Magma Cube, 0 for every other mob. */
	public int cubeSize;
	/** Magma Cube hop squish: positive while launching, negative after landing. */
	public float squish;
}
