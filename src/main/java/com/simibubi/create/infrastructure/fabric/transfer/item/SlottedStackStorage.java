package com.simibubi.create.infrastructure.fabric.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.world.item.ItemStack;

public interface SlottedStackStorage extends io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage {
	ItemStack getStackInSlot(int slot);

	void setStackInSlot(int slot, ItemStack stack);

	int getSlotLimit(int slot);

	boolean isItemValid(int slot, ItemStack stack);

	default boolean isItemValid(int slot, ItemVariant variant, int count) {
		return isItemValid(slot, variant.toStack(count));
	}

	default long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return getSlot(slot).insert(resource, maxAmount, transaction);
	}

	default long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return getSlot(slot).extract(resource, maxAmount, transaction);
	}
}
