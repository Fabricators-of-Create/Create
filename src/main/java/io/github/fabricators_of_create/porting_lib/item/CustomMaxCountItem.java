package io.github.fabricators_of_create.porting_lib.item;

import net.minecraft.world.item.ItemStack;

public interface CustomMaxCountItem {
	default int getItemStackLimit(ItemStack stack) {
		return stack.getMaxStackSize();
	}
}
