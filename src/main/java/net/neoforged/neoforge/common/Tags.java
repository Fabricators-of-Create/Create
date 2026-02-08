package net.neoforged.neoforge.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class Tags {
	private Tags() {
	}

	public static final class Items {
		public static final TagKey<Item> NUGGETS = create("nuggets");
		public static final TagKey<Item> ENCHANTABLES = create("enchantables");

		private Items() {
		}

		private static TagKey<Item> create(String path) {
			return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
		}
	}
}
