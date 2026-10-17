package dev.mehbur.garipseyler.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla caps max health at 1024; Vecna (2000) and the Mind Flayer (1500) need more. Raises only the
 * max health ceiling, on both sides, so synced values are not clamped on the client either.
 */
@Mixin(RangedAttribute.class)
public abstract class RangedAttributeMixin extends Attribute {
	@Unique
	private static final String MAX_HEALTH_ID = "attribute.name.max_health";
	@Unique
	private static final double RAISED_MAX_HEALTH = 1_000_000.0;

	@Shadow
	@Final
	private double minValue;

	protected RangedAttributeMixin(String descriptionId, double defaultValue) {
		super(descriptionId, defaultValue);
	}

	@Inject(method = "sanitizeValue", at = @At("HEAD"), cancellable = true)
	private void garipseylermc$raiseMaxHealth(double value, CallbackInfoReturnable<Double> cir) {
		if (MAX_HEALTH_ID.equals(this.getDescriptionId())) {
			cir.setReturnValue(Double.isNaN(value) ? this.minValue : Mth.clamp(value, this.minValue, RAISED_MAX_HEALTH));
		}
	}
}
