package com.simibubi.create.foundation.mixin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Maps;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.content.trains.station.StationMapData;
import com.simibubi.create.content.trains.station.StationMarker;
import com.simibubi.create.foundation.mixin.accessor.MapItemSavedDataAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin implements StationMapData {
	@Unique
	private static final String STATION_MARKERS_KEY = "create:stations";

	@Unique
	private final Map<String, StationMarker> create$stationMarkers = Maps.newHashMap();

	@Unique
	private MapItemSavedDataAccessor create$accessor() {
		return (MapItemSavedDataAccessor) (Object) this;
	}

	@Inject(
			method = "load",
			at = @At("RETURN")
	)
	private static void create$onLoad(CompoundTag tag, HolderLookup.Provider levelRegistry, CallbackInfoReturnable<MapItemSavedData> cir) {
		MapItemSavedData mapData = cir.getReturnValue();
		StationMapData stationMapData = (StationMapData) mapData;

		ListTag listTag = tag.getList(STATION_MARKERS_KEY, Tag.TAG_COMPOUND);
		for (int i = 0; i < listTag.size(); ++i) {
			StationMarker stationMarker = StationMarker.load(listTag.getCompound(i), levelRegistry);
			stationMapData.addStationMarker(stationMarker);
		}
	}

	@Inject(
			method = "save",
			at = @At("RETURN")
	)
	public void create$onSave(CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
		ListTag listTag = new ListTag();
		for (StationMarker stationMarker : create$stationMarkers.values()) {
			listTag.add(stationMarker.save(registries));
		}
		tag.put(STATION_MARKERS_KEY, listTag);
	}

	@Override
	public void addStationMarker(StationMarker marker) {
		create$stationMarkers.put(marker.getId(), marker);

		MapItemSavedDataAccessor accessor = create$accessor();
		int scaleMultiplier = 1 << accessor.create$getScale();
		float localX = (marker.getTarget().getX() - accessor.create$getCenterX()) / (float) scaleMultiplier;
		float localZ = (marker.getTarget().getZ() - accessor.create$getCenterZ()) / (float) scaleMultiplier;

		if (localX < -63.0F || localX > 63.0F || localZ < -63.0F || localZ > 63.0F) {
			create$removeDecoration(marker.getId());
			return;
		}

		byte localXByte = (byte) (int) (localX * 2.0F + 0.5F);
		byte localZByte = (byte) (int) (localZ * 2.0F + 0.5F);

		MapDecoration decoration = StationMarker.createStationDecoration(localXByte, localZByte, Optional.of(marker.getName()));
		Map<String, MapDecoration> decorations = accessor.create$getDecorations();
		MapDecoration oldDecoration = decorations.put(marker.getId(), decoration);
		if (!decoration.equals(oldDecoration)) {
			int trackedDecorationCount = accessor.create$getTrackedDecorationCount();
			if (oldDecoration != null && oldDecoration.type().value().trackCount()) {
				trackedDecorationCount--;
			}

			if (decoration.type().value().trackCount()) {
				trackedDecorationCount++;
			}

			accessor.create$setTrackedDecorationCount(trackedDecorationCount);
			create$markDecorationsDirty();
		}
	}

	@Unique
	private void create$markDecorationsDirty() {
		((SavedData) (Object) this).setDirty();
	}

	@Unique
	private void create$removeDecoration(String identifier) {
		MapItemSavedDataAccessor accessor = create$accessor();
		Map<String, MapDecoration> decorations = accessor.create$getDecorations();
		MapDecoration removed = decorations.remove(identifier);
		int trackedDecorationCount = accessor.create$getTrackedDecorationCount();
		if (removed != null && removed.type().value().trackCount()) {
			trackedDecorationCount--;
		}
		accessor.create$setTrackedDecorationCount(trackedDecorationCount);
		create$markDecorationsDirty();
	}

	@Override
	public boolean toggleStation(LevelAccessor level, BlockPos pos, StationBlockEntity stationBlockEntity) {
		double xCenter = pos.getX() + 0.5D;
		double zCenter = pos.getZ() + 0.5D;
		MapItemSavedDataAccessor accessor = create$accessor();
		int scaleMultiplier = 1 << accessor.create$getScale();

		double localX = (xCenter - (double) accessor.create$getCenterX()) / (double) scaleMultiplier;
		double localZ = (zCenter - (double) accessor.create$getCenterZ()) / (double) scaleMultiplier;

		if (localX < -63.0D || localX > 63.0D || localZ < -63.0D || localZ > 63.0D)
			return false;

		StationMarker marker = StationMarker.fromWorld(level, pos);
		if (marker == null)
			return false;

		if (create$stationMarkers.remove(marker.getId(), marker)) {
			create$removeDecoration(marker.getId());
			return true;
		}

		if (accessor.create$getTrackedDecorationCount() < 256) {
			addStationMarker(marker);
			return true;
		}

		return false;
	}

	@Inject(
			method = "checkBanners(Lnet/minecraft/world/level/BlockGetter;II)V",
			at = @At("RETURN")
	)
	public void create$onCheckBanners(BlockGetter blockGetter, int x, int z, CallbackInfo ci) {
		create$checkStations(blockGetter, x, z);
	}

	@Unique
	private void create$checkStations(BlockGetter blockGetter, int x, int z) {
		Iterator<StationMarker> iterator = create$stationMarkers.values().iterator();
		List<StationMarker> newMarkers = new ArrayList<>();

		while (iterator.hasNext()) {
			StationMarker marker = iterator.next();
			if (marker.getTarget().getX() == x && marker.getTarget().getZ() == z) {
				StationMarker other = StationMarker.fromWorld(blockGetter, marker.getSource());
				if (!marker.equals(other)) {
					iterator.remove();
					create$removeDecoration(marker.getId());

					if (other != null && marker.getTarget().equals(other.getTarget())) {
						newMarkers.add(other);
					}
				}
			}
		}

		for (StationMarker marker : newMarkers) {
			addStationMarker(marker);
		}
	}
}
