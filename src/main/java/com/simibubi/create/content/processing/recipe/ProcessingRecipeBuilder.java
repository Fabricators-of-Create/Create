package com.simibubi.create.content.processing.recipe;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.tterrag.registrate.util.DataIngredient;

import net.createmod.catnip.data.Pair;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;

import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

public class ProcessingRecipeBuilder<T extends ProcessingRecipe<?>> {
	protected ResourceLocation recipeId;
	protected ProcessingRecipeFactory<T> factory;
	protected ProcessingRecipeParams params;
	protected List<Object> recipeConditions;

	public ProcessingRecipeBuilder(ProcessingRecipeFactory<T> factory, ResourceLocation recipeId) {
		this.recipeId = recipeId;
		params = new ProcessingRecipeParams(recipeId);
		recipeConditions = new ArrayList<>();
		this.factory = factory;
	}

	public ProcessingRecipeBuilder<T> withItemIngredients(Ingredient... ingredients) {
		return withItemIngredients(NonNullList.of(Ingredient.EMPTY, ingredients));
	}

	public ProcessingRecipeBuilder<T> withItemIngredients(NonNullList<Ingredient> ingredients) {
		params.ingredients = ingredients;
		return this;
	}

	public ProcessingRecipeBuilder<T> withSingleItemOutput(ItemStack output) {
		return withItemOutputs(new ProcessingOutput(output, 1));
	}

	public ProcessingRecipeBuilder<T> withItemOutputs(ProcessingOutput... outputs) {
		return withItemOutputs(NonNullList.of(ProcessingOutput.EMPTY, outputs));
	}

	public ProcessingRecipeBuilder<T> withItemOutputs(NonNullList<ProcessingOutput> outputs) {
		params.results = outputs;
		return this;
	}

	public ProcessingRecipeBuilder<T> withFluidIngredients(FluidIngredient... ingredients) {
		return withFluidIngredients(NonNullList.of(FluidIngredient.EMPTY, ingredients));
	}

	public ProcessingRecipeBuilder<T> withFluidIngredients(NonNullList<FluidIngredient> ingredients) {
		params.fluidIngredients = ingredients;
		return this;
	}

	public ProcessingRecipeBuilder<T> withFluidOutputs(FluidStack... outputs) {
		return withFluidOutputs(NonNullList.of(FluidStack.EMPTY, outputs));
	}

	public ProcessingRecipeBuilder<T> withFluidOutputs(NonNullList<FluidStack> outputs) {
		params.fluidResults = outputs;
		return this;
	}

	public ProcessingRecipeBuilder<T> duration(int ticks) {
		params.processingDuration = ticks;
		return this;
	}

	public ProcessingRecipeBuilder<T> averageProcessingDuration() {
		return duration(100);
	}

	public ProcessingRecipeBuilder<T> requiresHeat(HeatCondition condition) {
		params.requiredHeat = condition;
		return this;
	}

	public T build() {
		validateFluidAmounts();
		return factory.create(params);
	}

	public void build(RecipeOutput consumer) {
		T recipe = build();
		IRecipeTypeInfo recipeType = recipe.getTypeInfo();
		ResourceLocation typeId = recipeType.getId();

		if (!(recipeType.getSerializer() instanceof ProcessingRecipeSerializer))
			throw new IllegalStateException("Cannot datagen ProcessingRecipe of type: " + typeId);

		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(recipe.id.getNamespace(),
				typeId.getPath() + "/" + recipe.id.getPath());

		consumer.accept(id, recipe, null);
	}

	public static final long[] SUS_AMOUNTS = { 10, 250, 500, 1000 };

	private void validateFluidAmounts() {
		for (FluidIngredient ingredient : params.fluidIngredients) {
			for (long amount : SUS_AMOUNTS) {
				if (ingredient.getRequiredAmount() == amount) {
					Create.LOGGER.warn("Suspicious fluid amount in recipe [{}]: {}", params.id, amount);
				}
			}
		}
	}

	// Datagen shortcuts

	public ProcessingRecipeBuilder<T> require(TagKey<Item> tag) {
		return require(Ingredient.of(tag));
	}

	public ProcessingRecipeBuilder<T> require(ItemLike item) {
		return require(Ingredient.of(item));
	}

	public ProcessingRecipeBuilder<T> require(Ingredient ingredient) {
		params.ingredients.add(ingredient);
		return this;
	}

	// fabric: custom ingredient support
	public ProcessingRecipeBuilder<T> require(CustomIngredient ingredient) {
		return require(ingredient.toVanilla());
	}

	public ProcessingRecipeBuilder<T> require(Mods mod, String id) {
		params.ingredients.add(DataIngredient.ingredient(null, mod.asResource(id)).toVanilla());
		return this;
	}

	public ProcessingRecipeBuilder<T> require(ResourceLocation ingredient) {
		params.ingredients.add(DataIngredient.ingredient(null, ingredient).toVanilla());
		return this;
	}

	public ProcessingRecipeBuilder<T> require(Fluid fluid, long amount) {
		return require(FluidIngredient.fromFluid(fluid, amount));
	}

	public ProcessingRecipeBuilder<T> require(TagKey<Fluid> fluidTag, long amount) {
		return require(FluidIngredient.fromTag(fluidTag, amount));
	}

	public ProcessingRecipeBuilder<T> require(FluidIngredient ingredient) {
		params.fluidIngredients.add(ingredient);
		return this;
	}

	public ProcessingRecipeBuilder<T> output(ItemLike item) {
		return output(item, 1);
	}

	public ProcessingRecipeBuilder<T> output(float chance, ItemLike item) {
		return output(chance, item, 1);
	}

	public ProcessingRecipeBuilder<T> output(ItemLike item, int amount) {
		return output(1, item, amount);
	}

	public ProcessingRecipeBuilder<T> output(float chance, ItemLike item, int amount) {
		return output(chance, new ItemStack(item, amount));
	}

	public ProcessingRecipeBuilder<T> output(ItemStack output) {
		return output(1, output);
	}

	public ProcessingRecipeBuilder<T> output(float chance, ItemStack output) {
		return output(new ProcessingOutput(output, chance));
	}

	public ProcessingRecipeBuilder<T> output(float chance, Mods mod, String id, int amount) {
		return output(new ProcessingOutput(Pair.of(mod.asResource(id), amount), chance));
	}

	public ProcessingRecipeBuilder<T> output(ResourceLocation id) {
		return output(1, id, 1);
	}

	public ProcessingRecipeBuilder<T> output(Mods mod, String id) {
		return output(1, mod.asResource(id), 1);
	}
	public ProcessingRecipeBuilder<T> output(float chance, ResourceLocation registryName, int amount) {
		return output(new ProcessingOutput(Pair.of(registryName, amount), chance));
	}

	public ProcessingRecipeBuilder<T> output(ProcessingOutput output) {
		params.results.add(output);
		return this;
	}

	public ProcessingRecipeBuilder<T> output(Fluid fluid, long amount) {
		fluid = FluidHelper.convertToStill(fluid);
		return output(new FluidStack(fluid, amount));
	}

	public ProcessingRecipeBuilder<T> output(FluidStack fluidStack) {
		params.fluidResults.add(fluidStack);
		return this;
	}

	public ProcessingRecipeBuilder<T> toolNotConsumed() {
		params.keepHeldItem = true;
		return this;
	}

	//

	public ProcessingRecipeBuilder<T> whenModLoaded(String modid) {
		return this;
	}

	public ProcessingRecipeBuilder<T> whenModMissing(String modid) {
		return this;
	}

	public ProcessingRecipeBuilder<T> withCondition(Object condition) {
		recipeConditions.add(condition);
		return this;
	}

	@FunctionalInterface
	public interface ProcessingRecipeFactory<T extends ProcessingRecipe<?>> {
		T create(ProcessingRecipeParams params);
	}

	public static class ProcessingRecipeParams {

		protected ResourceLocation id;
		protected NonNullList<Ingredient> ingredients;
		protected NonNullList<ProcessingOutput> results;
		protected NonNullList<FluidIngredient> fluidIngredients;
		protected NonNullList<FluidStack> fluidResults;
		protected int processingDuration;
		protected HeatCondition requiredHeat;

		public boolean keepHeldItem;

		protected ProcessingRecipeParams(ResourceLocation id) {
			this.id = id;
			ingredients = NonNullList.create();
			results = NonNullList.create();
			fluidIngredients = NonNullList.create();
			fluidResults = NonNullList.create();
			processingDuration = 0;
			requiredHeat = HeatCondition.NONE;
			keepHeldItem = false;
		}

	}
}
