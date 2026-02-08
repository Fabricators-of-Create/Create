package com.simibubi.create.compat.emi.recipes;

import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.CreateEmiPlugin;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;

public class CrushingEmiRecipe extends CreateEmiRecipe<AbstractCrushingRecipe> {

	public CrushingEmiRecipe(AbstractCrushingRecipe recipe) {
		super(CreateEmiPlugin.CRUSHING, recipe, 134, 110);
		ResourceLocation rid = recipe.getId();
		this.id = ResourceLocation.fromNamespaceAndPath("emi", "create/crushing/" + rid.getNamespace() + "/" + rid.getPath());
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 51, 11);

		addSlot(widgets, input.get(0), 29, 6);

		int xOff = -output.size() * 19 / 2;
		for (int i = 0; i < output.size(); i++) {
			addSlot(widgets, output.get(i), 67 + xOff + 19 * i, 82).recipeContext(this);
		}

		CreateEmiAnimations.addCrushingWheels(widgets, 41, 63);
	}
}
