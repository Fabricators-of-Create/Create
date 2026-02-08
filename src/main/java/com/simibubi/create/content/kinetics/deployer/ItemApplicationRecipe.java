package com.simibubi.create.content.kinetics.deployer;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class ItemApplicationRecipe extends ProcessingRecipe<RecipeInput> {
	public static <T extends ProcessingRecipe<?>> MapCodec<T> codec(AllRecipeTypes recipeTypes) {
		return RecordCodecBuilder.mapCodec(i -> i.group(
			ProcessingRecipeSerializer.<T>codec(recipeTypes).forGetter(Function.identity()),
			Codec.BOOL.optionalFieldOf("keep_held_item", false)
				.forGetter(r -> r instanceof ItemApplicationRecipe iar && iar.keepHeldItem)
		).apply(i, (parent, keepHeldItem) -> {
			if (parent instanceof ItemApplicationRecipe iar)
				iar.keepHeldItem = keepHeldItem;
			return parent;
		}));
	}

	private boolean keepHeldItem;

	public ItemApplicationRecipe(AllRecipeTypes type, ProcessingRecipeParams params) {
		super(type, params);
		keepHeldItem = params.keepHeldItem;
	}

	@Override
	public boolean matches(RecipeInput inv, Level p_77569_2_) {
		return getProcessedItem().test(inv.getItem(0)) && getRequiredHeldItem().test(inv.getItem(1));
	}

	@Override
	protected int getMaxInputCount() {
		return 2;
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

	public boolean shouldKeepHeldItem() {
		return keepHeldItem;
	}

	public Ingredient getRequiredHeldItem() {
		if (ingredients.size() < 2)
			throw new IllegalStateException("Item Application Recipe: " + id.toString() + " has no tool!");
		return ingredients.get(1);
	}

	public Ingredient getProcessedItem() {
		if (ingredients.isEmpty())
			throw new IllegalStateException("Item Application Recipe: " + id.toString() + " has no ingredient!");
		return ingredients.get(0);
	}

	@Override
	public void readAdditional(FriendlyByteBuf buffer) {
		super.readAdditional(buffer);
		keepHeldItem = buffer.readBoolean();
	}

	@Override
	public void writeAdditional(FriendlyByteBuf buffer) {
		super.writeAdditional(buffer);
		buffer.writeBoolean(keepHeldItem);
	}
}
