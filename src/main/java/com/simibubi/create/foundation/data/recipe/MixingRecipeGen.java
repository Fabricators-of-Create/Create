package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

import io.github.fabricators_of_create.porting_lib.tags.Tags;

import java.util.concurrent.CompletableFuture;

public class MixingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	TEMP_LAVA = create("lava_from_cobble", b -> b.require(Tags.Items.COBBLESTONES)
		.output(Fluids.LAVA, FluidConstants.BUCKET / 20)
		.requiresHeat(HeatCondition.SUPERHEATED)),

		TEA = create("tea", b -> b.require(Fluids.WATER, FluidConstants.BOTTLE)
			.require(Tags.Fluids.MILK, FluidConstants.BOTTLE)
			.require(ItemTags.LEAVES)
			.output(AllFluids.TEA.get(), FluidConstants.BUCKET / 2)
			.requiresHeat(HeatCondition.HEATED)),

		CHOCOLATE = create("chocolate", b -> b.require(Tags.Fluids.MILK, FluidConstants.BOTTLE)
			.require(Items.SUGAR)
			.require(Items.COCOA_BEANS)
			.output(AllFluids.CHOCOLATE.get(), FluidConstants.BOTTLE)
			.requiresHeat(HeatCondition.HEATED)),

		CHOCOLATE_MELTING = create("chocolate_melting", b -> b.require(AllItems.BAR_OF_CHOCOLATE.get())
			.output(AllFluids.CHOCOLATE.get(), FluidConstants.BOTTLE)
			.requiresHeat(HeatCondition.HEATED)),

		HONEY = create("honey", b -> b.require(Items.HONEY_BLOCK)
			.output(AllFluids.HONEY.get(), FluidConstants.BUCKET)
			.requiresHeat(HeatCondition.HEATED)),

		DOUGH = create("dough_by_mixing", b -> b.require(I.wheatFlour())
			.require(Fluids.WATER, FluidConstants.BUCKET)
			.output(AllItems.DOUGH.get(), 1)),

		BRASS_INGOT = create("brass_ingot", b -> b.require(I.copper())
			.require(I.zinc())
			.output(AllItems.BRASS_INGOT.get(), 2)
			.requiresHeat(HeatCondition.HEATED)),

		ANDESITE_ALLOY = create("andesite_alloy", b -> b.require(Blocks.ANDESITE)
			.require(I.ironNugget())
			.output(I.andesiteAlloy(), 1)),

		ANDESITE_ALLOY_FROM_ZINC = create("andesite_alloy_from_zinc", b -> b.require(Blocks.ANDESITE)
			.require(I.zincNugget())
			.output(I.andesiteAlloy(), 1)),

		MUD = create("mud_by_mixing", b -> b.require(new BlockTagIngredient(BlockTags.CONVERTABLE_TO_MUD).toVanilla())
			.require(Fluids.WATER, FluidConstants.BOTTLE)
			.output(Blocks.MUD, 1)),

		PULP = create("cardboard_pulp", b -> b
			.require(AllItemTags.PULPIFIABLE.tag)
			.require(AllItemTags.PULPIFIABLE.tag)
			.require(AllItemTags.PULPIFIABLE.tag)
			.require(AllItemTags.PULPIFIABLE.tag)
			.require(Fluids.WATER, FluidConstants.BOTTLE)
			.output(AllItems.PULP, 1)),

		// AE2

		AE2_FLUIX = create(Mods.AE2.recipeId("fluix_crystal"), b -> b.require(Tags.Items.DUSTS_REDSTONE)
				.require(Fluids.WATER, FluidConstants.BOTTLE)
				.require(Mods.AE2, "charged_certus_quartz_crystal")
				.require(Tags.Items.GEMS_QUARTZ)
				.output(1f, Mods.AE2, "fluix_crystal", 2)
				.whenModLoaded(Mods.AE2.getId())),

		// Regions Unexplored

		RU_PEAT_MUD = moddedMud(Mods.RU, "peat"),
		RU_SILT_MUD = moddedMud(Mods.RU, "silt")

	;

	public GeneratedRecipe moddedMud(Mods mod, String name) {
		String mud = name + "_mud";
		return create(mod.recipeId(mud), b -> b.require(Fluids.WATER, FluidConstants.BOTTLE)
				.require(mod, name + "_dirt")
				.output(mod, mud)
				.whenModLoaded(mod.getId()));
	}

	public MixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MIXING;
	}

}
