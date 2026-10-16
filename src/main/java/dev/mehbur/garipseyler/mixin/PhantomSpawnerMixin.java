package dev.mehbur.garipseyler.mixin;

import dev.mehbur.garipseyler.config.ConfigManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Turns off insomnia Phantom spawning so Demobats never appear in the Overworld.
 */
@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void garipseylermc$disableInsomnia(ServerLevel level, boolean spawnEnemies, CallbackInfo ci) {
		if (ConfigManager.get().demobat.disableOverworldPhantoms) {
			ci.cancel();
		}
	}
}
