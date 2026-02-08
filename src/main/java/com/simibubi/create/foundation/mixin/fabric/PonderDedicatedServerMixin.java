package com.simibubi.create.foundation.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

@Mixin(targets = "net.createmod.ponder.FabricPonder", remap = false)
public class PonderDedicatedServerMixin {

	@Inject(method = "onInitialize", at = @At("HEAD"), cancellable = true, remap = false)
	private void create$skipServerPonderInit(CallbackInfo ci) {
		// Ponder's Fabric entrypoint currently touches client networking classes on server startup.
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			ci.cancel();
		}
	}
}
