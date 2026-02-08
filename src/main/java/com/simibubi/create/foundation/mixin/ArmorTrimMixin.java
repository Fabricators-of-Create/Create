package com.simibubi.create.foundation.mixin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.armor.AllArmorMaterials;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;

@Mixin(ArmorTrim.class)
public abstract class ArmorTrimMixin {
	@Unique
	private static final Method create$colorPaletteSuffixMethod = create$findColorPaletteSuffixMethod();
	@Unique
	private static final Method create$trimPatternGetter = create$findHolderGetter(TrimPattern.class);
	@Unique
	private static final Method create$trimMaterialGetter = create$findHolderGetter(TrimMaterial.class);

	@Unique
	private static Method create$findColorPaletteSuffixMethod() {
		for (Method method : ArmorTrim.class.getDeclaredMethods()) {
			if (!Modifier.isStatic(method.getModifiers()) || method.getReturnType() != String.class)
				continue;
			Class<?>[] params = method.getParameterTypes();
			if (params.length != 2)
				continue;
			if (!Holder.class.isAssignableFrom(params[0]) || !Holder.class.isAssignableFrom(params[1]))
				continue;
			method.setAccessible(true);
			return method;
		}
		return null;
	}

	@Unique
	private static Method create$findHolderGetter(Class<?> heldType) {
		for (Method method : ArmorTrim.class.getDeclaredMethods()) {
			if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0)
				continue;
			if (!Holder.class.isAssignableFrom(method.getReturnType()))
				continue;
			Type genericType = method.getGenericReturnType();
			if (!(genericType instanceof ParameterizedType parameterized))
				continue;
			Type[] args = parameterized.getActualTypeArguments();
			if (args.length != 1 || args[0] != heldType)
				continue;
			method.setAccessible(true);
			return method;
		}
		return null;
	}

	@Unique
	@SuppressWarnings("unchecked")
	private static Holder<TrimPattern> create$getTrimPatternCompat(ArmorTrim trim) {
		if (create$trimPatternGetter != null) {
			try {
				return (Holder<TrimPattern>) create$trimPatternGetter.invoke(trim);
			} catch (ReflectiveOperationException ignored) {
			}
		}
		throw new IllegalStateException("Unable to resolve ArmorTrim trim pattern getter");
	}

	@Unique
	@SuppressWarnings("unchecked")
	private static Holder<TrimMaterial> create$getTrimMaterialCompat(ArmorTrim trim) {
		if (create$trimMaterialGetter != null) {
			try {
				return (Holder<TrimMaterial>) create$trimMaterialGetter.invoke(trim);
			} catch (ReflectiveOperationException ignored) {
			}
		}
		throw new IllegalStateException("Unable to resolve ArmorTrim trim material getter");
	}

	@Unique
	private static String create$getColorPaletteSuffixCompat(Holder<TrimMaterial> trimMaterial, Holder<ArmorMaterial> armorMaterial) {
		if (create$colorPaletteSuffixMethod != null) {
			try {
				return (String) create$colorPaletteSuffixMethod.invoke(null, trimMaterial, armorMaterial);
			} catch (ReflectiveOperationException ignored) {
			}
		}
		return trimMaterial.unwrapKey()
			.map(key -> key.location().getPath())
			.orElse("quartz");
	}

	@Unique
	private final BiFunction<Boolean, Holder<ArmorMaterial>, ResourceLocation> create$textureCardboard = Util.memoize((inner, armorMaterial) -> {
		ArmorTrim self = (ArmorTrim) (Object) this;
		Holder<TrimPattern> trimPattern = create$getTrimPatternCompat(self);
		Holder<TrimMaterial> trimMaterial = create$getTrimMaterialCompat(self);
		String assetPath = trimPattern.value().assetId().getPath();
		String colorSuffix = create$getColorPaletteSuffixCompat(trimMaterial, armorMaterial);
		return Create.asResource("trims/models/armor/card_" + assetPath + (inner ? "_leggings_" : "_") + colorSuffix);
	});

	@Inject(method = "innerTexture", at = @At("HEAD"), cancellable = true)
	private void create$swapTexturesForCardboardTrimsInner(Holder<ArmorMaterial> armorMaterial, CallbackInfoReturnable<ResourceLocation> cir) {
		if (armorMaterial == AllArmorMaterials.CARDBOARD) {
			cir.setReturnValue(create$textureCardboard.apply(true, armorMaterial));
		}
	}

	@Inject(method = "outerTexture", at = @At("HEAD"), cancellable = true)
	private void create$swapTexturesForCardboardTrimsOuter(Holder<ArmorMaterial> armorMaterial, CallbackInfoReturnable<ResourceLocation> cir) {
		if (armorMaterial == AllArmorMaterials.CARDBOARD) {
			cir.setReturnValue(create$textureCardboard.apply(false, armorMaterial));
		}
	}
}
