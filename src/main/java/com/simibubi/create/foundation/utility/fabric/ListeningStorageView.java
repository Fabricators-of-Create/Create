package com.simibubi.create.foundation.utility.fabric;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ListeningStorageView<T> implements StorageView<T> {
	private final StorageView<T> delegate;
	private final Runnable listener;

	public ListeningStorageView(StorageView<T> delegate, Runnable listener) {
		this.delegate = delegate;
		this.listener = listener;
	}

	@Override
	public boolean isResourceBlank() {
		return delegate.isResourceBlank();
	}

	@Override
	public T getResource() {
		return delegate.getResource();
	}

	@Override
	public long getAmount() {
		return delegate.getAmount();
	}

	@Override
	public long getCapacity() {
		return delegate.getCapacity();
	}

	@Override
	public long extract(T resource, long maxAmount, TransactionContext transaction) {
		long extracted = delegate.extract(resource, maxAmount, transaction);
		if (extracted > 0) {
			listener.run();
		}
		return extracted;
	}
}
