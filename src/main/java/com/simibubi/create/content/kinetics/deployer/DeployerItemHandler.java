package com.simibubi.create.content.kinetics.deployer;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.core.component.DataComponents;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;

public class DeployerItemHandler extends SnapshotParticipant<Unit> implements Storage<ItemVariant> {

	private DeployerBlockEntity be;
	private DeployerFakePlayer player;

	public DeployerItemHandler(DeployerBlockEntity be) {
		this.be = be;
		this.player = be.player;
	}

	public ItemStack getHeld() {
		if (player == null)
			return ItemStack.EMPTY;
		return player.getMainHandItem();
	}

	public void set(ItemStack stack) {
		if (player == null)
			return;
		if (be.getLevel().isClientSide)
			return;
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		int maxInsert = Math.min((int) maxAmount, resource.getItem().getDefaultMaxStackSize());
		ItemStack stack = resource.toStack(maxInsert);
		if (!isItemValid(stack))
			return 0;

		ItemStack held = getHeld();
		if (held.isEmpty()) {
			be.snapshotParticipant.updateSnapshots(transaction);
			set(stack);
			return maxInsert;
		}

		if (!ItemStack.isSameItemSameComponents(held, stack))
			return 0;

		int space = held.getMaxStackSize() - held.getCount();
		if (space == 0)
			return 0;

		int toInsert = Math.min(maxInsert, space);
		ItemStack newStack = held.copy();
		newStack.grow(toInsert);
		be.snapshotParticipant.updateSnapshots(transaction);
		set(newStack);
		return toInsert;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long extracted = extractFromOverflow(resource, maxAmount, transaction);
		if (extracted == maxAmount)
			return extracted;
		extracted += extractFromHeld(resource, maxAmount - extracted, transaction);
		return extracted;
	}

	/**
	 * @return number of items extracted from the TE's overflow slots
	 */
	public long extractFromOverflow(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long extracted = 0;
		for (int i = 0; i < be.overflowItems.size(); i++) {
			ItemStack itemStack = be.overflowItems.get(i);
			if (itemStack.isEmpty())
				continue;
			if (!resource.matches(itemStack))
				continue;
			int toExtract = (int) Math.min(maxAmount - extracted, itemStack.getCount());
			if (extracted == 0)
				be.snapshotParticipant.updateSnapshots(transaction);
			extracted += toExtract;
			ItemStack newStack = itemStack.copy();
			newStack.shrink(toExtract);
			be.overflowItems.set(i, newStack);
		}
		return extracted;
	}

	/**
	 * @return the number of items extracted from the TE's held stack
	 */
	public long extractFromHeld(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		ItemStack held = getHeld();
		if (held.isEmpty() || !resource.matches(held))
			return 0;
		if (!be.filtering.getFilter().isEmpty() && be.filtering.test(held))
			return 0;
		int toExtract = (int) Math.min(maxAmount, held.getCount());
		be.snapshotParticipant.updateSnapshots(transaction);
		ItemStack newStack = held.copy();
		newStack.shrink(toExtract);
		set(newStack);
		return toExtract;
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
		be.setChanged();
		be.sendData();
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return new DeployerItemHandlerIterator();
	}

	public boolean isItemValid(ItemStack stack) {
		FilteringBehaviour filteringBehaviour = be.getBehaviour(FilteringBehaviour.TYPE);
		return filteringBehaviour == null || filteringBehaviour.test(stack);
	}

	public class DeployerItemHandlerIterator implements Iterator<StorageView<ItemVariant>> {
		private int index; // -1 means held

		public DeployerItemHandlerIterator() {
			this.index = be.overflowItems.size() != 0 ? 0 : -1;
		}

		@Override
		public boolean hasNext() {
			return index < be.overflowItems.size();
		}

		@Override
		public StorageView<ItemVariant> next() {
			final int indexFinal = this.index;
			Supplier<ItemStack> heldGetter = () -> be.overflowItems.get(indexFinal);
			Consumer<ItemStack> heldSetter = (stack) -> be.overflowItems.set(indexFinal, stack);
			Predicate<ItemStack> mayExtract = stack -> true;
			if (index == -1) {
				heldGetter = player::getMainHandItem;
				heldSetter = s -> player.setItemInHand(InteractionHand.MAIN_HAND, s);
				mayExtract = s -> be.filtering.getFilter().isEmpty() || !be.filtering.test(s);
				index = be.overflowItems.size(); // hasNext will be false now
			}
			index++;
			if (index == be.overflowItems.size())
				index = -1;
			return new DeployerSlotView(heldGetter, heldSetter, mayExtract);
		}

	}

	public class DeployerSlotView extends SnapshotParticipant<Unit> implements StorageView<ItemVariant> {
		public final Supplier<ItemStack> heldGetter;
		public final Consumer<ItemStack> heldSetter;
		public final Predicate<ItemStack> mayExtract;

		public DeployerSlotView(Supplier<ItemStack> heldGetter, Consumer<ItemStack> heldSetter, Predicate<ItemStack> mayExtract) {
			this.heldGetter = heldGetter;
			this.heldSetter = heldSetter;
			this.mayExtract = mayExtract;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			ItemStack stack = getStack();
			if (stack.isEmpty() || !resource.matches(stack) || !mayExtract.test(stack))
				return 0;
			int toExtract = (int) Math.min(maxAmount, stack.getCount());
			updateSnapshots(transaction);
			ItemStack newStack = stack.copyWithCount(stack.getCount() - toExtract);
			heldSetter.accept(newStack);
			return toExtract;
		}

		@Override
		public boolean isResourceBlank() {
			return getResource().isBlank();
		}

		@Override
		public ItemVariant getResource() {
			ItemStack stack = getStack();
			return stack.isEmpty() ? ItemVariant.blank() : ItemVariant.of(stack);
		}

		@Override
		public long getAmount() {
			return getStack().getCount();
		}

		@Override
		public long getCapacity() {
			return getStack().getMaxStackSize();
		}

		public ItemStack getStack() {
			return heldGetter.get();
		}

		@Override
		public void updateSnapshots(TransactionContext transaction) {
			super.updateSnapshots(transaction);
			be.snapshotParticipant.updateSnapshots(transaction);
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
			DeployerItemHandler.this.onFinalCommit();
		}
	}
}
