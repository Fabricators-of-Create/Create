package com.simibubi.create.foundation.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.AllDamageTypes;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

@Mixin(EnchantedCountIncreaseFunction.class)
public abstract class EnchantedCountIncreaseFunctionMixin {
	@Unique
	private static final Field create$valueField = create$findNumberProviderField();
	@Unique
	private static final Field create$limitField = create$findLimitField();
	@Unique
	private static final Method create$hasLimitMethod = create$findHasLimitMethod();

	@Unique
	private static Field create$findNumberProviderField() {
		for (Field field : EnchantedCountIncreaseFunction.class.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()))
				continue;
			if (!NumberProvider.class.isAssignableFrom(field.getType()))
				continue;
			field.setAccessible(true);
			return field;
		}
		return null;
	}

	@Unique
	private static Field create$findLimitField() {
		Field fallback = null;
		for (Field field : EnchantedCountIncreaseFunction.class.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()) || field.getType() != int.class)
				continue;
			if (field.getName().toLowerCase().contains("limit")) {
				field.setAccessible(true);
				return field;
			}
			if (fallback == null)
				fallback = field;
		}
		if (fallback != null)
			fallback.setAccessible(true);
		return fallback;
	}

	@Unique
	private static Method create$findHasLimitMethod() {
		for (Method method : EnchantedCountIncreaseFunction.class.getDeclaredMethods()) {
			if (Modifier.isStatic(method.getModifiers()))
				continue;
			if (method.getParameterCount() != 0 || method.getReturnType() != boolean.class)
				continue;
			if (!method.getName().toLowerCase().contains("limit"))
				continue;
			method.setAccessible(true);
			return method;
		}
		return null;
	}

	@Unique
	private static NumberProvider create$getValue(EnchantedCountIncreaseFunction self) {
		if (create$valueField == null)
			return null;
		try {
			return (NumberProvider) create$valueField.get(self);
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}

	@Unique
	private static int create$getLimit(EnchantedCountIncreaseFunction self) {
		boolean hasLimit = false;
		if (create$hasLimitMethod != null) {
			try {
				hasLimit = (boolean) create$hasLimitMethod.invoke(self);
			} catch (ReflectiveOperationException ignored) {
				hasLimit = false;
			}
		}

		if (create$limitField == null)
			return hasLimit ? Integer.MAX_VALUE : 0;

		try {
			int limit = (int) create$limitField.get(self);
			if (!hasLimit)
				return Math.max(limit, 0);
			return limit;
		} catch (ReflectiveOperationException ignored) {
			return hasLimit ? Integer.MAX_VALUE : 0;
		}
	}

	@Inject(method = "run", at = @At("TAIL"))
	private void fireWithoutAttackingEntity(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
		DamageSource damageSource = context.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
		if (damageSource != null && damageSource.is(AllDamageTypes.CRUSH)) {
			int lootingLevel = 2;
			EnchantedCountIncreaseFunction self = (EnchantedCountIncreaseFunction) (Object) this;
			NumberProvider valueProvider = create$getValue(self);
			if (valueProvider == null)
				return;

			float f = (float) lootingLevel * valueProvider.getFloat(context);
			stack.grow(Math.round(f));
			int limit = create$getLimit(self);
			if (limit > 0)
				stack.limitSize(limit);
		}
	}
}
