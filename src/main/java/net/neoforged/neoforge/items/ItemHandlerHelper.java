package net.neoforged.neoforge.items;

import com.simibubi.create.content.equipment.toolbox.ItemReturnInvWrapper;
import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.item.SlottedStackStorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

public final class ItemHandlerHelper {
	private ItemHandlerHelper() {
	}

	public static ItemStack copyStackWithSize(ItemStack stack, int size) {
		if (size <= 0 || stack.isEmpty())
			return ItemStack.EMPTY;
		return stack.copyWithCount(size);
	}

	public static boolean canItemStacksStack(ItemStack first, ItemStack second) {
		return ItemStack.isSameItemSameComponents(first, second);
	}

	public static ItemStack insertItemStacked(SlottedStackStorage handler, ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		try (Transaction t = Transaction.openOuter()) {
			long inserted = handler.insert(ItemVariant.of(stack), stack.getCount(), t);
			if (!simulate)
				t.commit();
			return copyStackWithSize(stack, TransferUtil.truncateLong(stack.getCount() - inserted));
		}
	}

	public static ItemStack insertItemStacked(ItemReturnInvWrapper handler, ItemStack stack, boolean simulate) {
		return handler.insert(stack, simulate);
	}
}
