package com.simibubi.create.content.processing.sequenced;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeFactory;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public class SequencedAssemblyRecipeBuilder {

	private ResourceLocation id;
	private SequencedAssemblyRecipe recipe;
	protected List<Object> recipeConditions;

	public SequencedAssemblyRecipeBuilder(ResourceLocation id) {
		this.id = id;
		recipeConditions = new ArrayList<>();
		this.recipe = new SequencedAssemblyRecipe(AllRecipeTypes.SEQUENCED_ASSEMBLY.getSerializer());
	}

	public <T extends ProcessingRecipe<?>> SequencedAssemblyRecipeBuilder addStep(ProcessingRecipeFactory<T> factory,
		UnaryOperator<ProcessingRecipeBuilder<T>> builder) {
		ProcessingRecipeBuilder<T> recipeBuilder =
			new ProcessingRecipeBuilder<>(factory, ResourceLocation.withDefaultNamespace("dummy"));
		Item placeHolder = recipe.getTransitionalItem()
			.getItem();
		recipe.getSequence()
			.add(new SequencedRecipe<>(builder.apply(recipeBuilder.require(placeHolder)
				.output(placeHolder))
				.build()));
		return this;
	}

	public SequencedAssemblyRecipeBuilder require(ItemLike ingredient) {
		return require(Ingredient.of(ingredient));
	}

	public SequencedAssemblyRecipeBuilder require(TagKey<Item> tag) {
		return require(Ingredient.of(tag));
	}

	public SequencedAssemblyRecipeBuilder require(Ingredient ingredient) {
		recipe.ingredient = ingredient;
		return this;
	}

	public SequencedAssemblyRecipeBuilder transitionTo(ItemLike item) {
		recipe.transitionalItem = new ProcessingOutput(new ItemStack(item), 1);
		return this;
	}

	public SequencedAssemblyRecipeBuilder loops(int loops) {
		recipe.loops = loops;
		return this;
	}

	public SequencedAssemblyRecipeBuilder addOutput(ItemLike item, float weight) {
		return addOutput(new ItemStack(item), weight);
	}

	public SequencedAssemblyRecipeBuilder addOutput(ItemStack item, float weight) {
		recipe.resultPool.add(new ProcessingOutput(item, weight));
		return this;
	}

	public RecipeHolder<SequencedAssemblyRecipe> build() {
		return new RecipeHolder<>(id, recipe);
	}

	public void build(RecipeOutput consumer) {
		RecipeHolder<SequencedAssemblyRecipe> holder = build();

		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(holder.id().getNamespace(),
				AllRecipeTypes.SEQUENCED_ASSEMBLY.getId().getPath() + "/" + holder.id().getPath());

		consumer.accept(id, holder.value(), null);
	}
}
