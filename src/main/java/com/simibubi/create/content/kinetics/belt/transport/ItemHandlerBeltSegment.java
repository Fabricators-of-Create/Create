package com.simibubi.create.content.kinetics.belt.transport;

import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ItemHandlerBeltSegment implements SingleSlotStorage<ItemVariant> {
	private final BeltInventory beltInventory;
	int offset;

	public ItemHandlerBeltSegment(BeltInventory beltInventory, int offset) {
		this.beltInventory = beltInventory;
		this.offset = offset;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (this.beltInventory.canInsertAt(offset)) {
			int toInsert = Math.min((int) maxAmount, resource.getItem().getDefaultMaxStackSize());
			TransportedItemStack newStack = new TransportedItemStack(resource.toStack(toInsert));
			newStack.insertedAt = offset;
			newStack.beltPosition = offset + .5f + (beltInventory.beltMovementPositive ? -1 : 1) / 16f;
			newStack.prevBeltPosition = newStack.beltPosition;
			this.beltInventory.toInsertSnapshotParticipant.updateSnapshots(transaction);
			this.beltInventory.addItem(newStack);
			return toInsert;
		}
		return 0;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		TransportedItemStack transported = this.beltInventory.getStackAtOffset(offset);
		// since actual removal occurs a tick later, it's possible to extract a removed item
		if (transported == null || beltInventory.toRemove.contains(transported))
			return 0;

		int toExtract = (int) Math.min(maxAmount, transported.stack.getCount());
		this.beltInventory.itemsSnapshotParticipant.updateSnapshots(transaction);
		transported.stack.shrink(toExtract);
		return toExtract;
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public ItemVariant getResource() {
		return ItemVariant.of(getStack());
	}

	@Override
	public long getAmount() {
		ItemStack stack = getStack();
		return stack.isEmpty() ? 0 : stack.getCount();
	}

	@Override
	public long getCapacity() {
		return getStack().getMaxStackSize();
	}

	public ItemStack getStack() {
		TransportedItemStack transported = this.beltInventory.getStackAtOffset(offset);
		if (transported == null)
			return ItemStack.EMPTY;
		return transported.stack.isEmpty() ? ItemStack.EMPTY : transported.stack;
	}
}
