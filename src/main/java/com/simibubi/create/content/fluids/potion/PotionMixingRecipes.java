package com.simibubi.create.content.fluids.potion;

import java.util.List;
import java.util.Map;

import com.simibubi.create.content.kinetics.mixer.MixingRecipe;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

public class PotionMixingRecipes {
	public static final List<MixingRecipe> ALL = List.of();

	private static final List<RecipeHolder<MixingRecipe>> RECIPES = List.of();
	private static final Map<Item, List<MixingRecipe>> RECIPES_BY_ITEM = Map.of();

	private PotionMixingRecipes() {
	}

	public static List<RecipeHolder<MixingRecipe>> createRecipes(Level level) {
		return RECIPES;
	}

	public static Map<Item, List<MixingRecipe>> sortRecipesByItem(Level level) {
		return RECIPES_BY_ITEM;
	}
}
