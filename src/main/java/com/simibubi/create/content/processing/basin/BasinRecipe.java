package com.simibubi.create.content.processing.basin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import com.simibubi.create.infrastructure.fabric.transfer.TransactionSuccessCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

public class BasinRecipe extends ProcessingRecipe<RecipeInput> {

	public static boolean match(BasinBlockEntity basin, Recipe<?> recipe) {
		FilteringBehaviour filter = basin.getFilter();
		if (filter == null)
			return false;

		boolean filterTest = filter.test(recipe.getResultItem(basin.getLevel()
			.registryAccess()));
		if (recipe instanceof BasinRecipe basinRecipe) {
			if (basinRecipe.getRollableResults()
				.isEmpty()
				&& !basinRecipe.getFluidResults()
				.isEmpty())
				filterTest = filter.test(basinRecipe.getFluidResults()
					.get(0));
		}

		if (!filterTest)
			return false;

		return apply(basin, recipe, true);
	}

	public static boolean apply(BasinBlockEntity basin, Recipe<?> recipe) {
		return apply(basin, recipe, false);
	}

	private static boolean apply(BasinBlockEntity basin, Recipe<?> recipe, boolean test) {
		boolean isBasinRecipe = recipe instanceof BasinRecipe;
		Storage<ItemVariant> availableItems = basin.getItemStorage(null);
		Storage<FluidVariant> availableFluids = basin.getFluidStorage(null);

		if (availableItems == null || availableFluids == null)
			return false;

		HeatLevel heat = BasinBlockEntity.getHeatLevelOf(basin.getLevel()
				.getBlockState(basin.getBlockPos()
						.below(1)));
		if (isBasinRecipe && !((BasinRecipe) recipe).getRequiredHeat()
				.testBlazeBurner(heat))
			return false;

		List<ItemStack> recipeOutputItems = new ArrayList<>();
		List<FluidStack> recipeOutputFluids = new ArrayList<>();

		List<Ingredient> ingredients = new LinkedList<>(recipe.getIngredients());
		List<FluidIngredient> fluidIngredients =
				isBasinRecipe ? ((BasinRecipe) recipe).getFluidIngredients() : Collections.emptyList();

		NonNullList<ItemStack> consumedItems = NonNullList.create();

		try (Transaction t = Transaction.openOuter()) {
			Ingredients:
			for (Ingredient ingredient : ingredients) {
				for (StorageView<ItemVariant> view : availableItems.nonEmptyViews()) {
					ItemVariant var = view.getResource();
					ItemStack stack = var.toStack();
					if (!ingredient.test(stack)) continue;
					// Catalyst items are never consumed
					ItemStack remainder = stack.getRecipeRemainder();
					if (!remainder.isEmpty() && ItemStack.isSameItem(remainder, stack))
						continue Ingredients;
					long extracted = view.extract(var, 1, t);
					if (extracted == 0) continue;
					consumedItems.add(stack);
					continue Ingredients;
				}
				// something wasn't found
				return false;
			}

			boolean fluidsAffected = false;
			FluidIngredients:
			for (FluidIngredient fluidIngredient : fluidIngredients) {
				long amountRequired = fluidIngredient.getRequiredAmount();
				for (StorageView<FluidVariant> view : availableFluids.nonEmptyViews()) {
					FluidStack fluidStack = new FluidStack(view);
					if (!fluidIngredient.test(fluidStack)) continue;
					long drainedAmount = Math.min(amountRequired, fluidStack.getAmount());
					if (view.extract(fluidStack.getVariant(), drainedAmount, t) == drainedAmount) {
						fluidsAffected = true;
						amountRequired -= drainedAmount;
						if (amountRequired != 0) continue;
						continue FluidIngredients;
					}
				}
				// something wasn't found
				return false;
			}

			if (fluidsAffected) {
				TransactionSuccessCallback.register(t, () -> {
					basin.getBehaviour(SmartFluidTankBehaviour.INPUT)
							.forEach(TankSegment::onFluidStackChanged);
					basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT)
							.forEach(TankSegment::onFluidStackChanged);
				});
			}

				NonNullList<ItemStack> craftingRemainderStacks = NonNullList.withSize(9, ItemStack.EMPTY);
				for (int i = 0; i < consumedItems.size() && i < 9; i++)
					craftingRemainderStacks.set(i, consumedItems.get(i).copyWithCount(1));
				CraftingInput remainderInput = CraftingInput.of(3, 3, craftingRemainderStacks);

			if (recipe instanceof BasinRecipe basinRecipe) {
				recipeOutputItems.addAll(basinRecipe.rollResults());

					for (FluidStack fluidStack : basinRecipe.getFluidResults())
						if (!fluidStack.isEmpty())
							recipeOutputFluids.add(fluidStack);
					for (ItemStack stack : basinRecipe.getRemainingItems(remainderInput))
						if (!stack.isEmpty())
							recipeOutputItems.add(stack);

			} else {
				recipeOutputItems.add(recipe.getResultItem(basin.getLevel()
					.registryAccess()));

					if (recipe instanceof CraftingRecipe craftingRecipe) {
						for (ItemStack stack : craftingRecipe.getRemainingItems(remainderInput))
							if (!stack.isEmpty())
								recipeOutputItems.add(stack);
					}
//				}
			}

			// fabric: bad
			recipeOutputItems.removeIf(ItemStack::isEmpty);

			if (!basin.acceptOutputs(recipeOutputItems, recipeOutputFluids, t))
				return false;

			if (!test)
				t.commit();
			return true;
		}
	}

	public static RecipeHolder<BasinRecipe> convertShapeless(RecipeHolder<?> recipe) {
		BasinRecipe basinRecipe =
			new ProcessingRecipeBuilder<>(BasinRecipe::new, recipe.id()).withItemIngredients(recipe.value().getIngredients())
				.withSingleItemOutput(recipe.value().getResultItem(Minecraft.getInstance().level.registryAccess()))
				.build();
		return new RecipeHolder<>(recipe.id(), basinRecipe);
	}

	protected BasinRecipe(IRecipeTypeInfo type, ProcessingRecipeParams params) {
		super(type, params);
	}

	public BasinRecipe(ProcessingRecipeParams params) {
		this(AllRecipeTypes.BASIN, params);
	}

	@Override
	protected int getMaxInputCount() {
		return 9;
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

	@Override
	protected int getMaxFluidInputCount() {
		return 2;
	}

	@Override
	protected int getMaxFluidOutputCount() {
		return 2;
	}

	@Override
	protected boolean canRequireHeat() {
		return true;
	}

	@Override
	protected boolean canSpecifyDuration() {
		return true;
	}

	@Override
	public boolean matches(RecipeInput input, @Nonnull Level worldIn) {
		return false;
	}

}
