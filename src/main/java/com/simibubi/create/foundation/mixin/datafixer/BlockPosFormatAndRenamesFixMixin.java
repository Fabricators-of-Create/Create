package com.simibubi.create.foundation.mixin.datafixer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.simibubi.create.foundation.utility.DataFixerHelper;

import net.minecraft.util.datafix.fixes.BlockPosFormatAndRenamesFix;

@Mixin(BlockPosFormatAndRenamesFix.class)
public abstract class BlockPosFormatAndRenamesFixMixin extends DataFix {
	private static final Method CREATE_ENTITY_FIXER = create$findEntityFixerMethod();
	private static boolean create$warnedMissingEntityFixer;

	public BlockPosFormatAndRenamesFixMixin(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Inject(method = "makeRule", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
	private void create$addFixers(CallbackInfoReturnable<TypeRewriteRule> cir, @Local List<TypeRewriteRule> output) {
		for (DataFixerHelper.BlockPosFixer fixer : DataFixerHelper.BLOCK_POS_FIXERS_VIEW) {
			DSL.TypeReference ref = fixer.reference();
			String id = fixer.id();

			TypeRewriteRule rule;
			if (fixer.customFixer() != null) {
				OpticFinder<?> opticfinder = DSL.namedChoice(id, this.getInputSchema().getChoiceType(ref, id));
				rule = fixTypeEverywhereTyped("BlockPos format for " + id + " (" + ref.typeName() + ")",
					getInputSchema().getType(ref),
					typed -> typed.updateTyped(opticfinder, data ->
						data.update(DSL.remainderFinder(), dynamic ->
							fixer.customFixer().apply(dynamic)
						)
					)
				);
			} else {
				rule = create$invokeEntityFixer(ref, id, fixer.renames());
				if (rule == null) {
					if (!create$warnedMissingEntityFixer) {
						create$warnedMissingEntityFixer = true;
						System.err.println("[Create] Skipping legacy BlockPos datafix injection: BlockPosFormatAndRenamesFix#createEntityFixer signature was not found.");
					}
					continue;
				}
			}

			output.add(rule);
		}
	}

	private static Method create$findEntityFixerMethod() {
		for (Method method : BlockPosFormatAndRenamesFix.class.getDeclaredMethods()) {
			if (!TypeRewriteRule.class.isAssignableFrom(method.getReturnType())) {
				continue;
			}

			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 3) {
				continue;
			}

			if (!DSL.TypeReference.class.isAssignableFrom(parameterTypes[0])) {
				continue;
			}

			if (parameterTypes[1] != String.class) {
				continue;
			}

			if (!Map.class.isAssignableFrom(parameterTypes[2])) {
				continue;
			}

			method.setAccessible(true);
			return method;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private TypeRewriteRule create$invokeEntityFixer(DSL.TypeReference reference, String entityId, Map<String, String> renames) {
		if (CREATE_ENTITY_FIXER == null) {
			return null;
		}

		try {
			return (TypeRewriteRule) CREATE_ENTITY_FIXER.invoke(this, reference, entityId, renames);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}
}
