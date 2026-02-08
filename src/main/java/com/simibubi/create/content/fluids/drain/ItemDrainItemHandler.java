package com.simibubi.create.content.fluids.drain;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;

import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public class ItemDrainItemHandler extends SnapshotParticipant<Unit> implements SingleSlotStorage<ItemVariant> {

	private ItemDrainBlockEntity blockEntity;
	private Direction side;

	public ItemDrainItemHandler(ItemDrainBlockEntity be, Direction side) {
		this.blockEntity = be;
		this.side = side;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!blockEntity.getHeldItemStack().isEmpty())
			return 0;

		ItemStack stack = resource.toStack();
		int toInsert = GenericItemEmptying.canItemBeEmptied(blockEntity.getLevel(), stack)
				? 1
				: Math.min((int) maxAmount, resource.getItem().getDefaultMaxStackSize());
		stack.setCount(toInsert);
		TransportedItemStack heldItem = new TransportedItemStack(stack);
		heldItem.prevBeltPosition = 0;
		blockEntity.snapshotParticipant.updateSnapshots(transaction);
		blockEntity.setHeldItem(heldItem, side.getOpposite());
		return toInsert;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		TransportedItemStack held = blockEntity.heldItem;
		if (held == null)
			return 0;

		int toExtract = Math.min((int) maxAmount, held.stack.getCount());
		ItemStack stack = held.stack.copy();
		stack.shrink(toExtract);
		blockEntity.snapshotParticipant.updateSnapshots(transaction);
		blockEntity.heldItem.stack = stack;
		if (stack.isEmpty())
			blockEntity.heldItem = null;
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
		TransportedItemStack held = blockEntity.heldItem;
		if (held == null || held.stack == null || held.stack.isEmpty())
			return ItemStack.EMPTY;
		return held.stack;
	}

	@Override
	protected Unit createSnapshot() {
		return Unit.INSTANCE;
	}

	@Override
	protected void readSnapshot(Unit snapshot) {
	}

	@Override
	protected void onFinalCommit() {
		super.onFinalCommit();
		blockEntity.notifyUpdate();
	}
}
