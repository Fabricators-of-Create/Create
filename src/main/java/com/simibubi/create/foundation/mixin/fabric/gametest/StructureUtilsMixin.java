package com.simibubi.create.foundation.mixin.fabric.gametest;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.simibubi.create.foundation.utility.fabric.StructureBlockEntityExtensions;

import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

@Mixin(value = StructureUtils.class, priority = 900) // apply before FAPI, run earlier
public class StructureUtilsMixin {
	/**
	 * this is what vanilla and forge do, but FAPI forces a different system
	 * @see StructureTestUtilMixin
 	 */
	@Inject(method = "getStructureTemplate", at = @At("HEAD"), cancellable = true)
	private static void useStructureManager(String name, ServerLevel level, CallbackInfoReturnable<StructureTemplate> cir) {
		ResourceLocation id = ResourceLocation.parse(name);
		level.getStructureManager().get(id).ifPresent(cir::setReturnValue);
	}

	@ModifyReceiver(
		method = "createStructureBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/entity/StructureBlockEntity;setIgnoreEntities(Z)V"
		)
	)
	private static StructureBlockEntity markGameTest(StructureBlockEntity instance, boolean ignoreEntities) {
		((StructureBlockEntityExtensions) instance).create$markGameTest();
		return instance;
	}
}
