package com.simibubi.create.content.equipment.toolbox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.item.ItemSlots;
import com.simibubi.create.infrastructure.fabric.transfer.TransactionSuccessCallback;
import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ToolboxInventory extends ItemStackHandler {
	public static final Codec<ToolboxInventory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ItemSlots.maxSizeCodec(8).fieldOf("items").forGetter(ItemSlots::fromHandler),
		ItemStack.CODEC.listOf().fieldOf("filters").forGetter(toolbox -> toolbox.filters)
	).apply(instance, ToolboxInventory::deserialize));

	public static final int STACKS_PER_COMPARTMENT = 4;
	List<ItemStack> filters;
	boolean settling;
	private ToolboxBlockEntity blockEntity;

	private boolean limitedMode;

	public ToolboxInventory(ToolboxBlockEntity be) {
		super(8 * STACKS_PER_COMPARTMENT);
		this.blockEntity = be;
		limitedMode = false;
		filters = new ArrayList<>();
		settling = false;
		for (int i = 0; i < 8; i++)
			filters.add(ItemStack.EMPTY);
	}

	public void inLimitedMode(Consumer<ToolboxInventory> action) {
		limitedMode = true;
		action.accept(this);
		limitedMode = false;
	}

	public void settle(int compartment) {
		int totalCount = 0;
		boolean valid = true;
		boolean shouldBeEmpty = false;
		ItemStack sample = ItemStack.EMPTY;

		for (int i = 0; i < STACKS_PER_COMPARTMENT; i++) {
			ItemStack stackInSlot = getStackInSlot(compartment * STACKS_PER_COMPARTMENT + i);
			totalCount += stackInSlot.getCount();
			if (!shouldBeEmpty)
				shouldBeEmpty = stackInSlot.isEmpty() || stackInSlot.getCount() != stackInSlot.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
			else if (!stackInSlot.isEmpty()) {
				valid = false;
				sample = stackInSlot;
			}
		}

		if (valid)
			return;

		settling = true;
		if (!sample.isStackable()) {
			for (int i = 0; i < STACKS_PER_COMPARTMENT; i++) {
				if (!getStackInSlot(compartment * STACKS_PER_COMPARTMENT + i).isEmpty())
					continue;
				for (int j = i + 1; j < STACKS_PER_COMPARTMENT; j++) {
					ItemStack stackInSlot = getStackInSlot(compartment * STACKS_PER_COMPARTMENT + j);
					if (stackInSlot.isEmpty())
						continue;
					setStackInSlot(compartment * STACKS_PER_COMPARTMENT + i, stackInSlot);
					setStackInSlot(compartment * STACKS_PER_COMPARTMENT + j, ItemStack.EMPTY);
					break;
				}
			}
		} else {
			for (int i = 0; i < STACKS_PER_COMPARTMENT; i++) {
				ItemStack copy = totalCount <= 0 ? ItemStack.EMPTY
					: sample.copyWithCount(Math.min(totalCount, sample.getOrDefault(DataComponents.MAX_STACK_SIZE, 64)));
				setStackInSlot(compartment * STACKS_PER_COMPARTMENT + i, copy);
				totalCount -= copy.getCount();
			}
		}
		settling = false;
		notifyUpdate();
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant var, int count) {
		ItemStack stack = var.toStack();
		if (!stack.getItem().canFitInsideContainerItems())
			return false;

		if (slot < 0 || slot >= getSlotCount())
			return false;
		int compartment = slot / STACKS_PER_COMPARTMENT;
		ItemStack filter = filters.get(compartment);
		if (limitedMode && filter.isEmpty())
			return false;
		if (filter.isEmpty() || ToolboxInventory.canItemsShareCompartment(filter, stack))
			return super.isItemValid(slot, var, count);
		return false;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		super.setStackInSlot(slot, stack);
		updateCompartmentFilters(slot, stack, null);
	}

	private void updateCompartmentFilters(int slot, ItemStack stack, @Nullable TransactionContext ctx) {
		int compartment = slot / STACKS_PER_COMPARTMENT;
		if (!stack.isEmpty() && filters.get(compartment)
				.isEmpty()) {
			filters.set(compartment, stack.copyWithCount(1));
			if (ctx != null) TransactionSuccessCallback.register(ctx, blockEntity::notifyUpdate);
			else notifyUpdate();
		}
	}

	@Override
	public @NotNull CompoundTag serializeNBT(@NotNull HolderLookup.Provider registries) {
		CompoundTag compound = super.serializeNBT(registries);
		compound.put("Compartments", NBTHelper.writeItemList(filters, registries));
		return compound;
	}

	@Override
	protected void onContentsChanged(int slot) {
		if (!settling && (blockEntity == null || !blockEntity.getLevel().isClientSide))
			settle(slot / STACKS_PER_COMPARTMENT);
		notifyUpdate();
		super.onContentsChanged(slot);
		// fabric: since slots bypass setStackInSlot, call this here too
		ItemStack stack = this.getStackInSlot(slot);
		updateCompartmentFilters(slot, stack, null);
	}

	@Override
	public void deserializeNBT(@NotNull HolderLookup.Provider registries, CompoundTag nbt) {
		filters = NBTHelper.readItemList(nbt.getList("Compartments", Tag.TAG_COMPOUND), registries);
		if (filters.size() != 8) {
			filters.clear();
			for (int i = 0; i < 8; i++)
				filters.add(ItemStack.EMPTY);
		}
		super.deserializeNBT(registries, nbt);
	}

	public ItemStack distributeToCompartment(@Nonnull ItemStack stack, int compartment, TransactionContext ctx) {
		if (stack.isEmpty())
			return stack;
		if (filters.get(compartment)
			.isEmpty())
			return stack;

		int toInsert = stack.getCount();
		int inserted = 0;
		ItemVariant variant = ItemVariant.of(stack);
		for (int i = STACKS_PER_COMPARTMENT - 1; i >= 0; i--) {
			int slot = compartment * STACKS_PER_COMPARTMENT + i;
			inserted += getSlot(slot).insert(variant, toInsert - inserted, ctx);
			if (inserted >= toInsert)
				break;
		}

		return ItemHandlerHelper.copyStackWithSize(stack, toInsert - inserted);
	}

	public ItemStack takeFromCompartment(int amount, int compartment, TransactionContext ctx) {
		if (amount == 0)
			return ItemStack.EMPTY;

		ItemVariant toExtract = null;
		int extracted = 0;
		for (int i = STACKS_PER_COMPARTMENT - 1; i >= 0; i--) {
			int slot = compartment * STACKS_PER_COMPARTMENT + i;
			SingleSlotStorage<ItemVariant> handlerSlot = getSlot(slot);
			if (handlerSlot.isResourceBlank())
				continue;
			if (toExtract == null)
				toExtract = handlerSlot.getResource();
			extracted += handlerSlot.extract(toExtract, amount - extracted, ctx);
			if (extracted >= amount)
				break;
		}

		return toExtract == null || extracted == 0 ? ItemStack.EMPTY : toExtract.toStack(extracted);
	}

	public static ItemStack cleanItemNBT(ItemStack stack) {
		if (AllItems.BELT_CONNECTOR.isIn(stack))
			stack.remove(AllDataComponents.BELT_FIRST_SHAFT);
		return stack;
	}

	public static boolean canItemsShareCompartment(ItemStack stack1, ItemStack stack2) {
		if (!stack1.isStackable() && !stack2.isStackable() && stack1.isDamageableItem() && stack2.isDamageableItem())
			return stack1.getItem() == stack2.getItem();
		if (AllItems.BELT_CONNECTOR.isIn(stack1) && AllItems.BELT_CONNECTOR.isIn(stack2))
			return true;
		return ItemStack.isSameItemSameComponents(stack1, stack2);
	}

	private void notifyUpdate() {
		if (blockEntity != null)
			// change to sendData if this doesn't exist
			blockEntity.notifyUpdate();
	}

	private static ToolboxInventory deserialize(ItemSlots slots, List<ItemStack> filters) {
		ToolboxInventory inventory = new ToolboxInventory(null);
		slots.forEach(inventory::setStackInSlot);
		inventory.filters = filters;
		return inventory;
	}

}
