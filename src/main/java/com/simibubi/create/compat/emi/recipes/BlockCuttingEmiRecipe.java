package com.simibubi.create.compat.emi.recipes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.recipes.BlockCuttingEmiRecipe.CondensedBlockCuttingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

public class BlockCuttingEmiRecipe extends CreateEmiRecipe<CondensedBlockCuttingRecipe> {

	public BlockCuttingEmiRecipe(EmiRecipeCategory category, CondensedBlockCuttingRecipe recipe) {
		super(category, recipe, 177, 75);
		id = null;
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 31, 6);
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 16, 50);

		addSlot(widgets, input.get(0), 4, 8);

		List<List<ItemStack>> results = recipe.getCondensedOutputs();
		for (int i = 0; i < results.size(); i++) {
			int x = (i % 5) * 19;
			int y = (i / 5) * -19;
			addSlot(widgets, EmiStack.of(results.get(i).get(0)), 77 + x, 47 + y).recipeContext(this);
		}

		CreateEmiAnimations.addSaw(widgets, 33, 37);
	}

	public static class CondensedBlockCuttingRecipe extends StonecutterRecipe {
		private List<ItemStack> outputs = Lists.newArrayList();

		public CondensedBlockCuttingRecipe(Ingredient ingredient) {
			super(ResourceLocation.fromNamespaceAndPath("create", "synthetic/emi/block_cutting"), "", ingredient, ItemStack.EMPTY);
		}

		public void addOutput(ItemStack stack) {
			outputs.add(stack);
		}

		public List<ItemStack> getOutputs() {
			return outputs;
		}

		public List<List<ItemStack>> getCondensedOutputs() {
			List<List<ItemStack>> result = Lists.newArrayList();
			int index = 0;
			boolean firstPass = true;
			for (ItemStack stack : outputs) {
				if (firstPass) {
					result.add(new ArrayList<>());
				}
				result.get(index).add(stack);
				index++;
				if (index >= 15) {
					index = 0;
					firstPass = false;
				}
			}
			return result;
		}

		@Override
		public boolean isSpecial() {
			return true;
		}

		@SuppressWarnings("rawtypes")
		public static List<CondensedBlockCuttingRecipe> condenseRecipes(List<? extends Recipe> recipes, String name) {
			List<CondensedBlockCuttingRecipe> condensed = Lists.newArrayList();
			outer:
			for (Recipe<?> recipe : recipes) {
				Ingredient ingredient = recipe.getIngredients().get(0);
				if (ingredient.isEmpty()) {
					continue;
				}
				for (CondensedBlockCuttingRecipe condensedRecipe : condensed) {
					if (ItemHelper.matchIngredients(ingredient, condensedRecipe.getIngredients().get(0))) {
						condensedRecipe.addOutput(CreateEmiRecipe.getResultItem(recipe));
						continue outer;
					}
				}
				CondensedBlockCuttingRecipe cr = new CondensedBlockCuttingRecipe(ingredient);
				cr.addOutput(CreateEmiRecipe.getResultItem(recipe));
				condensed.add(cr);
			}
			return condensed;
		}
	}
}
