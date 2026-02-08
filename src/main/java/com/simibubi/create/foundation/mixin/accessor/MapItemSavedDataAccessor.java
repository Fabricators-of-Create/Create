package com.simibubi.create.foundation.mixin.accessor;

import java.util.Map;

import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapItemSavedData.class)
public interface MapItemSavedDataAccessor {
	@Accessor("centerX")
	int create$getCenterX();

	@Accessor("centerZ")
	int create$getCenterZ();

	@Accessor("scale")
	byte create$getScale();

	@Accessor("decorations")
	Map<String, MapDecoration> create$getDecorations();

	@Accessor("trackedDecorationCount")
	int create$getTrackedDecorationCount();

	@Accessor("trackedDecorationCount")
	void create$setTrackedDecorationCount(int trackedDecorationCount);
}
