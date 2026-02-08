package com.simibubi.create.foundation.mixin.fabric.debug;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(targets = "io.github.fabricators_of_create.porting_lib.models.CompositeModel$Baked", remap = false)
public class CompositeModelGetQuadsWarningMixin {
	private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

	@WrapOperation(
		method = "getQuads",
		at = @At(
			value = "INVOKE",
			target = "Ljava/lang/Thread;getStackTrace()[Ljava/lang/StackTraceElement;"
		),
		require = 0
	)
	private StackTraceElement[] create$skipCompositeModelStackTrace(Thread thread, Operation<StackTraceElement[]> original) {
		return EMPTY_STACK_TRACE;
	}

	@WrapOperation(
		method = "getQuads",
		at = @At(
			value = "INVOKE",
			target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;)V"
		),
		require = 0
	)
	private void create$suppressCompositeModelWarning(Logger logger, String message, Operation<Void> original) {
		// Suppress known Porting Lib warning spam in legacy getQuads path.
	}
}
