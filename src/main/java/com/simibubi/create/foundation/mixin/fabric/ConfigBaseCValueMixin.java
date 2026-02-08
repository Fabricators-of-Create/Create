package com.simibubi.create.foundation.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.createmod.catnip.config.ConfigBase.CValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

@Mixin(CValue.class)
public abstract class ConfigBaseCValueMixin {

	@Shadow(remap = false)
	protected ConfigValue<?> value;

	@Inject(method = "get", at = @At("HEAD"), cancellable = true, remap = false)
	private void create$returnDefaultWhenConfigIsNotLoaded(CallbackInfoReturnable<Object> cir) {
		if (value == null) {
			return;
		}

		try {
			cir.setReturnValue(value.get());
		} catch (IllegalStateException e) {
			cir.setReturnValue(value.getDefault());
		}
	}
}
