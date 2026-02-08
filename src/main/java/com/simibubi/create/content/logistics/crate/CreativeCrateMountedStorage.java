package com.simibubi.create.content.logistics.crate;

import java.util.Iterator;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Iterator;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class CreativeCrateMountedStorage extends MountedItemStorage implements SingleSlotStorage<ItemVariant> {
	public static final MapCodec<CreativeCrateMountedStorage> CODEC = ItemStack.OPTIONAL_CODEC.xmap(
		CreativeCrateMountedStorage::new, storage -> storage.suppliedStack
	).fieldOf("value");

	private final ItemStack suppliedStack;
	private final ItemStack cachedStackInSlot;

	protected CreativeCrateMountedStorage(MountedItemStorageType<?> type, ItemStack suppliedStack) {
		super(type);
		this.suppliedStack = suppliedStack;
		this.cachedStackInSlot = suppliedStack.copyWithCount(suppliedStack.getMaxStackSize());
	}

	public CreativeCrateMountedStorage(ItemStack suppliedStack) {
		this(AllMountedStorageTypes.CREATIVE_CRATE.get(), suppliedStack);
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		// no need to do anything here, the supplied item can't change while mounted
	}

	@Override
	@NotNull
	public ItemStack getStackInSlot(int slot) {
		return slot == 0 ? this.cachedStackInSlot : ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return maxAmount;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		if (resource.matches(this.suppliedStack)) {
			return Math.min(this.suppliedStack.getMaxStackSize(), maxAmount);
		} else {
			return 0;
		}
	}

	@Override
	public boolean isResourceBlank() {
		return this.suppliedStack.isEmpty();
	}

	@Override
	public ItemVariant getResource() {
		return ItemVariant.of(this.suppliedStack);
	}

	@Override
	public long getAmount() {
		return Long.MAX_VALUE;
	}

	@Override
	public long getCapacity() {
		return Long.MAX_VALUE;
	}

	@Override
	public int getSlotLimit(int i) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return slot == 0 && (this.suppliedStack.isEmpty() || ItemStack.isSameItemSameComponents(this.suppliedStack, stack));
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return SingleSlotStorage.super.iterator();
	}
}
