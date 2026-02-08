package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
	@ModifyExpressionValue(
		method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;getRootVehicle()Lnet/minecraft/world/entity/Entity;",
			ordinal = 1
		)
	)
	private static Entity create$interactWithEntitiesOnContraptions(Entity root) {
		// return null when root is a contraption, so the interaction isn't blocked
		return root instanceof AbstractContraptionEntity ? null : root;
	}

	@WrapOperation(
		method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;canRiderInteract()Z"
		),
		require = 0
	)
	private static boolean create$canRiderInteractNullSafe(Entity entity, Operation<Boolean> original) {
		// Porting Lib can evaluate this call with a null root vehicle; treat it as non-blocking.
		if (entity == null || entity instanceof AbstractContraptionEntity) {
			return true;
		}
		return original.call(entity);
	}
}
