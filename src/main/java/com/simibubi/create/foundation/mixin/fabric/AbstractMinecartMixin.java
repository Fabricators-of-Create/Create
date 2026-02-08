package com.simibubi.create.foundation.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.minecart.capability.MinecartController;
import com.simibubi.create.foundation.utility.fabric.AbstractMinecartExtensions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin implements AbstractMinecartExtensions {
	@Unique
	private MinecartController controller;

	@Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
	private void initController(EntityType<?> entityType, Level level, CallbackInfo ci) {
		this.controller = new MinecartController((AbstractMinecart) (Object) this);
		if (level != null) { // don't trust modders
			CapabilityMinecartController.attach((AbstractMinecart) (Object) this);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
	private void loadController(CompoundTag compound, CallbackInfo ci) {
		if (compound.contains(CAP_KEY, Tag.TAG_COMPOUND))
			controller.deserializeNBT(((AbstractMinecart) (Object) this).registryAccess(), compound.getCompound(CAP_KEY));
	}

	@Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
	private void saveController(CompoundTag compound, CallbackInfo ci) {
		compound.put(CAP_KEY, controller.serializeNBT(((AbstractMinecart) (Object) this).registryAccess()));
	}

	@Override
	public MinecartController create$getController() {
		return controller;
	}
}
