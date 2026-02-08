package com.simibubi.create.content.kinetics.deployer;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerContainer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public class DeployerRecipeSearchEvent {
	private boolean canceled;
	private final DeployerBlockEntity blockEntity;
	private final ItemStackHandlerContainer inventory;
	@Nullable
	private RecipeHolder<? extends Recipe<? extends RecipeInput>> recipe;
	private int maxPriority;

	public static final Event<DeployerRecipeSearchCallback> EVENT =
		EventFactory.createArrayBacked(DeployerRecipeSearchCallback.class, callbacks -> event -> {
			for (DeployerRecipeSearchCallback callback : callbacks) {
				callback.handle(event);
			}
		});

	@FunctionalInterface
	public interface DeployerRecipeSearchCallback {
		void handle(DeployerRecipeSearchEvent event);
	}

	public DeployerRecipeSearchEvent(DeployerBlockEntity blockEntity, ItemStackHandlerContainer inventory) {
		this.blockEntity = blockEntity;
		this.inventory = inventory;
	}

	public DeployerBlockEntity getBlockEntity() {
		return blockEntity;
	}

	public ItemStackHandlerContainer getInventory() {
		return inventory;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void cancel() {
		this.canceled = true;
	}

	public boolean shouldAddRecipeWithPriority(int priority) {
		return !canceled && priority > maxPriority;
	}

	@Nullable
	public RecipeHolder<? extends Recipe<? extends RecipeInput>> getRecipe() {
		if (isCanceled())
			return null;
		return recipe;
	}

	public void addRecipe(
		Supplier<Optional<? extends RecipeHolder<? extends Recipe<? extends RecipeInput>>>> recipeSupplier,
		int priority
	) {
		if (!shouldAddRecipeWithPriority(priority))
			return;
		recipeSupplier.get().ifPresent(newRecipe -> {
			this.recipe = newRecipe;
			this.maxPriority = priority;
		});
	}
}
