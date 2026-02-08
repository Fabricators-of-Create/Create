package com.simibubi.create.foundation.mixin.fabric.debug;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fabricators_of_create.porting_lib.models.obj.ObjModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

@Mixin(ObjModel.class)
public abstract class ObjModelMakeQuadMixin {
	private static final Direction CREATE_FALLBACK_FACE = Direction.values()[0];

	@WrapOperation(
		method = "makeQuad",
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadView;toBakedQuad(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)Lnet/minecraft/client/renderer/block/model/BakedQuad;"
		),
		require = 0
	)
	private BakedQuad create$handleNullQuadView(QuadView quadView, TextureAtlasSprite sprite, Operation<BakedQuad> original) {
		if (quadView == null) {
			return new BakedQuad(new int[32], -1, CREATE_FALLBACK_FACE, sprite, true);
		}
		return original.call(quadView, sprite);
	}
}
