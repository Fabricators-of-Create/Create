package com.simibubi.create.infrastructure.worldgen;

import com.simibubi.create.Create;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class AllFeatures {
	public static final Holder.Reference<Feature<?>> LAYERED_ORE = Registry.registerForHolder(
		BuiltInRegistries.FEATURE,
		Create.asResource("layered_ore"),
		new LayeredOreFeature()
	);

	public static void register() {
	}
}
