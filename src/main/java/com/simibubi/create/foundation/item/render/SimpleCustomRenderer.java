package com.simibubi.create.foundation.item.render;

import java.util.function.Supplier;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class SimpleCustomRenderer {
	private SimpleCustomRenderer() {}

	public static IClientItemExtensions create(Object item, Supplier<?> renderer) {
		return new IClientItemExtensions() {
		};
	}
}
