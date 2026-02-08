package com.simibubi.create.compat.emi.recipes.fan;

import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.CreateEmiPlugin;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Blocks;

public class FanSmokingEmiRecipe extends FanEmiRecipe<SmokingRecipe> {

	public FanSmokingEmiRecipe(SmokingRecipe recipe) {
		super(CreateEmiPlugin.FAN_SMOKING, recipe);
		ResourceLocation rid = recipe.getId();
		this.id = ResourceLocation.fromNamespaceAndPath("emi", "create/fan_smoking/" + rid.getNamespace() + "/" + rid.getPath());
	}

	@Override
	protected void renderAttachedBlock(GuiGraphics graphics) {
		GuiGameElement.of(Blocks.FIRE.defaultBlockState())
			.scale(SCALE)
			.atLocal(0, 0, 2)
			.lighting(CreateEmiAnimations.DEFAULT_LIGHTING)
			.render(graphics);
	}
}
