package com.simibubi.create.foundation.mixin.fabric.debug;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.slf4j.Logger;

import net.minecraft.client.resources.model.ModelBakery;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryBakeWarningMixin {
	@Redirect(
		method = "method_61072",
		at = @At(
			value = "INVOKE",
			target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
		)
	)
	private void create$logBakeWarningWithStack(Logger logger, String message, Object modelId, Object error) {
		if (error instanceof Throwable throwable) {
			logger.warn("Unable to bake model '{}'", modelId, throwable);
			return;
		}
		logger.warn(message, modelId, error);
	}
}
