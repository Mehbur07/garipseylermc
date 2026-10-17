package dev.mehbur.garipseyler.entity.mindflayer;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

/**
 * Added to the vanilla {@code EnderDragonFight} by {@code EnderDragonFightMixin}, so the fight (boss bar,
 * exit portal, gateways, egg) runs for the Mind Flayer the same way it does for the dragon.
 */
public interface MindFlayerFight {
	/** Called every tick by the fight's Mind Flayer: boss bar progress and "still alive" bookkeeping. */
	void garipseylermc$updateMindFlayer(MindFlayer flayer);

	/** End of the death animation: opens the exit portal, adds a gateway and places the egg on the first kill. */
	void garipseylermc$setMindFlayerKilled(MindFlayer flayer);

	BlockPos garipseylermc$origin();

	boolean garipseylermc$isBossKilled();

	@Nullable BlockPos garipseylermc$exitPortal();
}
