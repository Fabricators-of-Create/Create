package com.simibubi.create;

import com.simibubi.create.content.schematics.SchematicProcessor;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class AllStructureProcessorTypes {
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static final Holder.Reference<StructureProcessorType<?>> SCHEMATIC = Registry.registerForHolder(
		BuiltInRegistries.STRUCTURE_PROCESSOR,
		Create.asResource("schematic"),
		(StructureProcessorType) (() -> SchematicProcessor.CODEC)
	);

	public static void register() {
	}
}
