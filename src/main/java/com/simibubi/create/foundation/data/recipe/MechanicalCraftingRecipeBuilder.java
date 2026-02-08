package com.simibubi.create.foundation.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;

import net.createmod.catnip.registry.RegisteredObjectsHelper;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

public class MechanicalCraftingRecipeBuilder {

	private final Item result;
	private final int count;
	private final List<String> pattern = Lists.newArrayList();
	private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
	private boolean acceptMirrored;
	private List<Object> recipeConditions;

	public MechanicalCraftingRecipeBuilder(ItemLike p_i48261_1_, int p_i48261_2_) {
		result = p_i48261_1_.asItem();
		count = p_i48261_2_;
		acceptMirrored = true;
		recipeConditions = new ArrayList<>();
	}

	/**
	 * Creates a new builder for a shaped recipe.
	 */
	public static MechanicalCraftingRecipeBuilder shapedRecipe(ItemLike p_200470_0_) {
		return shapedRecipe(p_200470_0_, 1);
	}

	/**
	 * Creates a new builder for a shaped recipe.
	 */
	public static MechanicalCraftingRecipeBuilder shapedRecipe(ItemLike p_200468_0_, int p_200468_1_) {
		return new MechanicalCraftingRecipeBuilder(p_200468_0_, p_200468_1_);
	}

	/**
	 * Adds a key to the recipe pattern.
	 */
	public MechanicalCraftingRecipeBuilder key(Character p_200469_1_, TagKey<Item> p_200469_2_) {
		return this.key(p_200469_1_, Ingredient.of(p_200469_2_));
	}

	/**
	 * Adds a key to the recipe pattern.
	 */
	public MechanicalCraftingRecipeBuilder key(Character p_200462_1_, ItemLike p_200462_2_) {
		return this.key(p_200462_1_, Ingredient.of(p_200462_2_));
	}

	/**
	 * Adds a key to the recipe pattern.
	 */
	public MechanicalCraftingRecipeBuilder key(Character p_200471_1_, Ingredient p_200471_2_) {
		if (this.key.containsKey(p_200471_1_)) {
			throw new IllegalArgumentException("Symbol '" + p_200471_1_ + "' is already defined!");
		} else if (p_200471_1_ == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			this.key.put(p_200471_1_, p_200471_2_);
			return this;
		}
	}

	/**
	 * Adds a new entry to the patterns for this recipe.
	 */
	public MechanicalCraftingRecipeBuilder patternLine(String p_200472_1_) {
		if (!this.pattern.isEmpty() && p_200472_1_.length() != this.pattern.get(0)
			.length()) {
			throw new IllegalArgumentException("Pattern must be the same width on every line!");
		} else {
			this.pattern.add(p_200472_1_);
			return this;
		}
	}

	/**
	 * Prevents the crafters from matching a vertically flipped version of the recipe
	 */
	public MechanicalCraftingRecipeBuilder disallowMirrored() {
		acceptMirrored = false;
		return this;
	}

	/**
	 * Builds this recipe into a {@link RecipeOutput}.
	 */
	public void build(RecipeOutput output) {
		this.build(output, RegisteredObjectsHelper.getKeyOrThrow(this.result));
	}

	/**
	 * Builds this recipe into a {@link RecipeOutput}. Use
	 * {@link #build(RecipeOutput)} if save is the same as the ID for the result.
	 */
	public void build(RecipeOutput output, String id) {
		ResourceLocation resourcelocation = RegisteredObjectsHelper.getKeyOrThrow(this.result);
		if ((ResourceLocation.parse(id)).equals(resourcelocation)) {
			throw new IllegalStateException("Shaped Recipe " + id + " should remove its 'save' argument");
		} else {
			this.build(output, ResourceLocation.parse(id));
		}
	}

	/**
	 * Builds this recipe into a {@link RecipeOutput}.
	 */
	public void build(RecipeOutput output, ResourceLocation id) {
		validate(id);
		MechanicalCraftingRecipe recipe = new MechanicalCraftingRecipe(
				"",
				CraftingBookCategory.MISC,
				ShapedRecipePattern.of(key, pattern),
				new ItemStack(result, count),
				acceptMirrored
		);
		output.accept(id, recipe, null);
		//output
		//	.accept(new MechanicalCraftingRecipeBuilder.Result(id, result, count, pattern, key, acceptMirrored, recipeConditions));
	}

	/**
	 * Makes sure that this recipe is valid.
	 */
	private void validate(ResourceLocation id) {
		if (pattern.isEmpty()) {
			throw new IllegalStateException("No pattern is defined for shaped recipe " + id + "!");
		} else {
			Set<Character> set = Sets.newHashSet(key.keySet());
			set.remove(' ');

			for (String s : pattern) {
				for (int i = 0; i < s.length(); ++i) {
					char c0 = s.charAt(i);
					if (!key.containsKey(c0) && c0 != ' ')
						throw new IllegalStateException(
							"Pattern in recipe " + id + " uses undefined symbol '" + c0 + "'");
					set.remove(c0);
				}
			}

			if (!set.isEmpty())
				throw new IllegalStateException(
					"Ingredients are defined but not used in pattern for recipe " + id);
		}
	}

	public MechanicalCraftingRecipeBuilder whenModLoaded(String modid) {
		return this;
	}

	public MechanicalCraftingRecipeBuilder whenModMissing(String modid) {
		return this;
	}

	public MechanicalCraftingRecipeBuilder withCondition(Object condition) {
		recipeConditions.add(condition);
		return this;
	}

}
