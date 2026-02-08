package com.simibubi.create.foundation.render;

import com.simibubi.create.Create;

import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.LightShader;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.EmptyModel;
import dev.engine_room.flywheel.lib.material.LightShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel;
import net.minecraft.client.resources.model.BakedModel;

public class SpecialModels {
	private static final RendererReloadCache<Key, Model> FLAT = new RendererReloadCache<>(SpecialModels::buildSafe);

	private static Model buildSafe(Key key) {
		try {
			BakedModel bakedModel = key.partial.get();
			if (bakedModel == null) {
				Create.LOGGER.error("Partial model '{}' did not bake; using empty fallback", key.partial.modelLocation());
				return EmptyModel.INSTANCE;
			}
			if (hasNullWrappedDelegate(bakedModel)) {
				Create.LOGGER.error("Partial model '{}' has a null wrapped delegate; using empty fallback", key.partial.modelLocation());
				return EmptyModel.INSTANCE;
			}
			return new BakedModelBuilder(bakedModel)
				.materialFunc((renderType, aBoolean) -> {
					var material = ModelUtil.getMaterial(renderType, aBoolean);
					if (material == null) {
						return null;
					}
					return SimpleMaterial.builderOf(material)
						.light(key.light)
						.cardinalLightingMode(key.cardinalLightingMode)
						.build();
				})
				.build();
		} catch (Throwable t) {
			Create.LOGGER.error("Failed to build Flywheel special model for '{}'; using empty fallback", key.partial.modelLocation(), t);
			return EmptyModel.INSTANCE;
		}
	}

	private static boolean hasNullWrappedDelegate(BakedModel model) {
		BakedModel current = model;
		for (int depth = 0; depth < 32 && current instanceof WrapperBakedModel wrapper; depth++) {
			BakedModel wrapped = wrapper.getWrappedModel();
			if (wrapped == null) {
				return true;
			}
			if (wrapped == current) {
				return false;
			}
			current = wrapped;
		}
		return false;
	}

	public static Model flatLit(PartialModel partial) {
		return FLAT.get(new Key(partial, LightShaders.FLAT, CardinalLightingMode.ENTITY));
	}

	public static Model flatChunk(PartialModel partial) {
		return FLAT.get(new Key(partial, LightShaders.FLAT, CardinalLightingMode.CHUNK));
	}

	public static Model chunkDiffuse(PartialModel partial) {
		return FLAT.get(new Key(partial, LightShaders.SMOOTH_WHEN_EMBEDDED, CardinalLightingMode.CHUNK));
	}

	private record Key(PartialModel partial, LightShader light, CardinalLightingMode cardinalLightingMode) {}
}
