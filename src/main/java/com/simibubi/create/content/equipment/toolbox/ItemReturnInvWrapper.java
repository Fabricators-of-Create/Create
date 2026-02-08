package com.simibubi.create.content.equipment.toolbox;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ItemReturnInvWrapper {
	private final Inventory inv;

	public ItemReturnInvWrapper(Inventory inv) {
		this.inv = inv;
	}

	public ItemStack insert(ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;

		ItemStack remaining = stack.copy();
		// Skip hotbar slots so this only fills main inventory rows.
		for (int slot = 9; slot <= 35 && !remaining.isEmpty(); slot++) {
			ItemStack inSlot = inv.getItem(slot);
			int maxSize = remaining.getMaxStackSize();
			if (inSlot.isEmpty()) {
				int moved = Math.min(remaining.getCount(), maxSize);
				if (!simulate)
					inv.setItem(slot, remaining.copyWithCount(moved));
				remaining.shrink(moved);
				continue;
			}
			if (!ItemStack.isSameItemSameComponents(inSlot, remaining))
				continue;
			int space = maxSize - inSlot.getCount();
			if (space <= 0)
				continue;
			int moved = Math.min(space, remaining.getCount());
			if (!simulate)
				inSlot.grow(moved);
			remaining.shrink(moved);
		}

		return remaining;
	}
}
