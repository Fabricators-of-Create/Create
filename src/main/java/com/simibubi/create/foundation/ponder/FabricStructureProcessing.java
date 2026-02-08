package com.simibubi.create.foundation.ponder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.Create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class FabricStructureProcessing {
	public static final MapCodec<Processor> PROCESSOR_CODEC = ResourceLocation.CODEC
		.fieldOf("structureId")
		.xmap(Processor::new, processor -> processor.structureId)
		;

	public static final StructureProcessorType<Processor> PROCESSOR_TYPE = Registry.register(
		BuiltInRegistries.STRUCTURE_PROCESSOR,
		Create.asResource("fabric_structure_processor"),
		() -> PROCESSOR_CODEC
	);

	public static void init() {
		// Intentionally left blank for the temporary Fabric 1.21.1 port.
	}

	public static class Processor extends StructureProcessor {
		public final ResourceLocation structureId;

		public Processor(ResourceLocation structureId) {
			this.structureId = structureId;
		}

		@Nullable
		@Override
		public StructureBlockInfo processBlock(
			@NotNull LevelReader level,
			@NotNull BlockPos pos,
			@NotNull BlockPos pivot,
			@NotNull StructureBlockInfo blockInfo,
			@NotNull StructureBlockInfo relativeBlockInfo,
			@NotNull StructurePlaceSettings settings
		) {
			return relativeBlockInfo;
		}

		@Override
		protected @NotNull StructureProcessorType<?> getType() {
			return PROCESSOR_TYPE;
		}
	}
}
