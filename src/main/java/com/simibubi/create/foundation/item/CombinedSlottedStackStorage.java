package com.simibubi.create.foundation.item;

import java.util.List;

import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import com.simibubi.create.infrastructure.fabric.transfer.item.SlottedStackStorage;

public class CombinedSlottedStackStorage<S extends SlottedStackStorage> extends CombinedSlottedStorage<ItemVariant, S> implements SlottedStackStorage {
	public CombinedSlottedStackStorage(List<S> parts) {
		super(parts);
	}

	@SafeVarargs
	public CombinedSlottedStackStorage(S... parts) {
		this(List.of(parts));
	}

	private <T> T getFromStorage(int slot, SlotFunction<T> function) {
		for (S part : this.parts) {
			if (slot < part.getSlotCount()) {
				return function.apply(part, slot);
			}

			slot -= part.getSlotCount();
		}

		throw new IndexOutOfBoundsException(slot);
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.getFromStorage(slot, SlottedStackStorage::getStackInSlot);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		this.getFromStorage(slot, (storage, index) -> {
			storage.setStackInSlot(index, stack);
			return null;
		});
	}

	@Override
	public int getSlotLimit(int slot) {
		return this.getFromStorage(slot, SlottedStackStorage::getSlotLimit);
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int count) {
		return this.getFromStorage(slot, (storage, index) -> storage.isItemValid(index, resource, count));
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return this.getFromStorage(slot, (storage, index) -> storage.isItemValid(index, stack));
	}

	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.getFromStorage(slot, (storage, index) -> storage.insertSlot(index, resource, maxAmount, transaction));
	}

	@Override
	public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.getFromStorage(slot, (storage, index) -> storage.extractSlot(index, resource, maxAmount, transaction));
	}

	@FunctionalInterface
	private interface SlotFunction<T> {
		T apply(SlottedStackStorage storage, int slot);
	}
}
