package com.simibubi.create.foundation.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
	static {
		CreateBuiltInRegistries.init();
	}

	@WrapOperation(method = "validate", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;forEach(Ljava/util/function/Consumer;)V"))
	private static <T extends Registry<?>> void create$ourRegistriesAreNotEmpty(Registry<T> instance, Consumer<T> consumer, Operation<Void> original) {
		Consumer<T> callback = (t) -> {
			if (!create$isCreateRegistry(t))
				consumer.accept(t);
		};

		original.call(instance, callback);
	}

	private static boolean create$isCreateRegistry(Registry<?> registry) {
		return registry == CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE
			|| registry == CreateBuiltInRegistries.FAN_PROCESSING_TYPE
			|| registry == CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE
			|| registry == CreateBuiltInRegistries.DISPLAY_SOURCE
			|| registry == CreateBuiltInRegistries.DISPLAY_TARGET
			|| registry == CreateBuiltInRegistries.MOUNTED_ITEM_STORAGE_TYPE
			|| registry == CreateBuiltInRegistries.MOUNTED_FLUID_STORAGE_TYPE
			|| registry == CreateBuiltInRegistries.CONTRAPTION_TYPE
			|| registry == CreateBuiltInRegistries.PACKAGE_PORT_TARGET_TYPE
			|| registry == CreateBuiltInRegistries.POTATO_PROJECTILE_RENDER_MODE
			|| registry == CreateBuiltInRegistries.POTATO_PROJECTILE_ENTITY_HIT_ACTION
			|| registry == CreateBuiltInRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION;
	}
}
