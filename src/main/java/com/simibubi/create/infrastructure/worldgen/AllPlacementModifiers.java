package com.simibubi.create.infrastructure.worldgen;

import com.simibubi.create.Create;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class AllPlacementModifiers {
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static final Holder.Reference<PlacementModifierType<?>> CONFIG_FILTER = Registry.registerForHolder(
		BuiltInRegistries.PLACEMENT_MODIFIER_TYPE,
		Create.asResource("config_filter"),
		(PlacementModifierType) (() -> ConfigPlacementFilter.CODEC)
	);

	public static void register() {
	}
}
