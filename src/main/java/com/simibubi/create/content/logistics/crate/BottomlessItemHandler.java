package com.simibubi.create.content.logistics.crate;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottomlessItemHandler implements SingleSlotStorage<ItemVariant> {

	private final Supplier<ItemStack> suppliedItemStack;

	public BottomlessItemHandler(Supplier<ItemStack> suppliedItemStack) {
		this.suppliedItemStack = suppliedItemStack;
	}

	public ItemStack getStackInSlot(int slot) {
		return slot == 0 ? getStack() : ItemStack.EMPTY;
	}

	protected ItemStack getStack() {
		ItemStack stack = suppliedItemStack.get();
		return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return maxAmount;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		ItemStack stack = getStack();
		if (!resource.matches(stack))
			return 0;
		if (stack.isEmpty())
			return 0;
		return Math.min(stack.getMaxStackSize(), maxAmount);
	}

	@Override
	public boolean isResourceBlank() {
		return getStack().isEmpty();
	}

	@Override
	public ItemVariant getResource() {
		return ItemVariant.of(getStack());
	}

	@Override
	public long getAmount() {
		return Long.MAX_VALUE;
	}

	@Override
	public long getCapacity() {
		return Long.MAX_VALUE;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return SingleSlotStorage.super.iterator();
	}

	@Override
	public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
		return this::nonEmptyIterator;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
		return isResourceBlank() ? Collections.emptyIterator() : iterator();
	}
}
