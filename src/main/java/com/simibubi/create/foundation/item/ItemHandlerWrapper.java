package com.simibubi.create.foundation.item;

import java.util.Iterator;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ItemHandlerWrapper implements Storage<ItemVariant> {

	protected Storage<ItemVariant> wrapped;

	public ItemHandlerWrapper(Storage<ItemVariant> wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean supportsInsertion() {
		return wrapped.supportsInsertion();
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return wrapped.insert(resource, maxAmount, transaction);
	}

	@Override
	public boolean supportsExtraction() {
		return wrapped.supportsExtraction();
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return wrapped.extract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return wrapped.iterator();
	}

	@Override
	public long getVersion() {
		return wrapped.getVersion();
	}
}
