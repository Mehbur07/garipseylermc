package dev.mehbur.garipseyler.client.mixin;

import dev.mehbur.garipseyler.GaripSeylerMod;
import dev.mehbur.garipseyler.config.ConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the neon "Garip Şeyler" logo (2:1) instead of the Minecraft logo and edition text wherever the game uses
 * its logo renderer (title screen, credits). Off with config {@code menu.titleScreenLogo}.
 */
@Mixin(LogoRenderer.class)
public abstract class LogoRendererMixin {
	private static final Identifier GARIPSEYLERMC$LOGO = GaripSeylerMod.id("textures/gui/title_logo.png");
	private static final int GARIPSEYLERMC$WIDTH = 160;
	private static final int GARIPSEYLERMC$HEIGHT = 80;
	/** Vanilla logo + edition text span about 51 pixels from the height offset; ours starts higher to fit. */
	private static final int GARIPSEYLERMC$RAISE = 14;

	@Shadow
	@Final
	private boolean keepLogoThroughFade;

	@Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IFI)V", at = @At("HEAD"), cancellable = true)
	private void garipseylermc$neonLogo(GuiGraphicsExtractor graphics, int width, float alpha, int heightOffset, CallbackInfo ci) {
		if (!ConfigManager.get().menu.titleScreenLogo) {
			return;
		}
		ci.cancel();
		int color = ARGB.white(this.keepLogoThroughFade ? 1.0F : alpha);
		int x = width / 2 - GARIPSEYLERMC$WIDTH / 2;
		int y = Math.max(2, heightOffset - GARIPSEYLERMC$RAISE);
		graphics.blit(RenderPipelines.GUI_TEXTURED, GARIPSEYLERMC$LOGO, x, y, 0.0F, 0.0F,
				GARIPSEYLERMC$WIDTH, GARIPSEYLERMC$HEIGHT, GARIPSEYLERMC$WIDTH, GARIPSEYLERMC$HEIGHT, color);
	}
}
