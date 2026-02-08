package com.simibubi.create.content.logistics.tunnel;

import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class BrassTunnelItemHandler implements SingleSlotStorage<ItemVariant> {

	private BrassTunnelBlockEntity blockEntity;

	public BrassTunnelItemHandler(BrassTunnelBlockEntity be) {
		this.blockEntity = be;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!blockEntity.hasDistributionBehaviour()) {
			Storage<ItemVariant> beltCapability = blockEntity.getBeltCapability();
			if (beltCapability == null)
				return 0;
			return beltCapability.insert(resource, maxAmount, transaction);
		}

		if (!blockEntity.canTakeItems())
			return 0;
		int toInsert = Math.min(ItemHelper.truncateLong(maxAmount), resource.getItem().getDefaultMaxStackSize());

		blockEntity.setStackToDistribute(resource.toStack(toInsert), null, transaction);
		return toInsert;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		Storage<ItemVariant> beltCapability = blockEntity.getBeltCapability();
		if (beltCapability == null)
			return 0;
		return beltCapability.extract(resource, maxAmount, transaction);
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
		ItemStack stack = blockEntity.stackToDistribute;
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		return stack;
	}
}
