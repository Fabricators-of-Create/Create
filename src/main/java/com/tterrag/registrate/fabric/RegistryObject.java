package com.tterrag.registrate.fabric;

import java.util.function.Supplier;

public class RegistryObject<T> implements Supplier<T> {
	private final Supplier<T> delegate;

	public RegistryObject(Supplier<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T get() {
		return delegate.get();
	}
}
