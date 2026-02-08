package com.simibubi.create.content.logistics.chute;

import com.simibubi.create.foundation.item.ItemHelper;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ChuteItemHandler extends SingleVariantStorage<ItemVariant> {

	private ChuteBlockEntity blockEntity;

	public ChuteItemHandler(ChuteBlockEntity be) {
		this.blockEntity = be;
		update();
	}

	public void update() {
		this.variant = ItemVariant.of(blockEntity.item);
		this.amount = blockEntity.item.getCount();
	}

	@Override
	public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
		if (!blockEntity.canAcceptItem(insertedVariant.toStack()))
			return 0;
		return super.insert(insertedVariant, maxAmount, transaction);
	}

	@Override
	protected void onFinalCommit() {
		blockEntity.setItem(variant.toStack(ItemHelper.truncateLong(amount)));
	}

	@Override
	protected long getCapacity(ItemVariant variant) {
		return Math.min(64, variant.getItem().getDefaultMaxStackSize());
	}

	@Override
	protected ItemVariant getBlankVariant() {
		return ItemVariant.blank();
	}
}
