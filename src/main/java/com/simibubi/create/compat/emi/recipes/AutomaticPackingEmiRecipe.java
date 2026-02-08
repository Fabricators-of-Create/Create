package com.simibubi.create.compat.emi.recipes;

import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.CreateEmiPlugin;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

public class AutomaticPackingEmiRecipe extends CreateEmiRecipe<BasinRecipe> {

	public AutomaticPackingEmiRecipe(BasinRecipe recipe) {
		super(CreateEmiPlugin.AUTOMATIC_PACKING, recipe, 177, 108);
		if (recipe.getRequiredHeat() == HeatCondition.NONE) {
			height = 90;
		}
		ResourceLocation id = recipe.getId();
		this.id = ResourceLocation.fromNamespaceAndPath("emi", "create/automatic_packing/" + id.getNamespace() + "/" + id.getPath());
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 136, 32);
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 81, 74);

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		int size = ingredients.size();
		int rows = size == 4 ? 2 : 3;
		for (int i = 0; i < size; i++) {
			int x = (rows == 2 ? 26 : 17) + (i % rows) * 19;
			int y = 50 - (i / rows) * 19;
			EmiIngredient ingredient = EmiIngredient.of(ingredients.get(i));
			addSlot(widgets, ingredient, x, y);
		}

		addSlot(widgets, output.get(0), 140, 50).recipeContext(this);

		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (requiredHeat != HeatCondition.NONE) {
			CreateEmiAnimations.addBlazeBurner(widgets, widgets.getWidth() / 2 + 3, 55, requiredHeat.visualizeAsBlazeBurner());
		}
		CreateEmiAnimations.addPress(widgets, widgets.getWidth() / 2 + 3, 40, true);
	}
}
