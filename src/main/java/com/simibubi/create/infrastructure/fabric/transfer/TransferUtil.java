package com.simibubi.create.infrastructure.fabric.transfer;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantCache;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Predicate;

public class TransferUtil {
	private static Fluid normalizeSourceFluid(Fluid fluid) {
		Objects.requireNonNull(fluid, "Fluid may not be null.");
		return fluid instanceof FlowingFluid flowingFluid ? flowingFluid.getSource() : fluid;
	}

	public static FluidVariant fluidVariantOf(Fluid fluid) {
		Fluid source = normalizeSourceFluid(fluid);
		return ((FluidVariantCache) source).fabric_getCachedFluidVariant();
	}

	public static FluidVariant fluidVariantOf(Fluid fluid, DataComponentPatch components) {
		Objects.requireNonNull(components, "Components may not be null.");
		Fluid source = normalizeSourceFluid(fluid);
		if (components.isEmpty()) {
			return ((FluidVariantCache) source).fabric_getCachedFluidVariant();
		}
		// Avoid FluidVariant.of(...) because fabric-transfer calls a missing MC method in this runtime.
		return new FluidVariantImpl(source, components);
	}

	public static long insert(Storage<FluidVariant> storage, FluidStack stack) {
		try (Transaction t = Transaction.openOuter()) {
			long inserted = insert(storage, stack, t);
			t.commit();
			return inserted;
		}
	}

	public static long insert(Storage<ItemVariant> storage, ItemStack stack) {
		try (Transaction t = Transaction.openOuter()) {
			long inserted = insert(storage, stack, t);
			t.commit();
			return inserted;
		}
	}

	public static long insert(Storage<FluidVariant> storage, FluidStack stack, TransactionContext ctx) {
		return storage.insert(stack.getVariant(), stack.getAmount(), ctx);
	}

	public static long insert(Storage<ItemVariant> storage, ItemStack stack, TransactionContext ctx) {
		return storage.insert(ItemVariant.of(stack), stack.getCount(), ctx);
	}

	@Nullable
	public static <T extends TransferVariant<?>> ResourceAmount<T> extractAny(Storage<T> storage, long maxAmount) {
		return commit(t -> StorageUtil.extractAny(storage, maxAmount, t));
	}

	@Nullable
	public static <T extends TransferVariant<?>> ResourceAmount<T> extractMatching(Storage<T> storage, Predicate<T> predicate, long maxAmount, TransactionContext ctx) {
		T resourceExtracting = null;
		long extracted = 0;

		for (StorageView<T> view : storage.nonEmptyViews()) {
			T resource = view.getResource();

			// see if a resource has already been chosen
			if (resourceExtracting != null && !resourceExtracting.equals(resource))
				continue;

			// if one hasn't, see if this one matches
			if (resourceExtracting == null && predicate.test(resource)) {
				resourceExtracting = resource;
			} else {
				// nope, skip
				continue;
			}

			extracted += view.extract(resource, maxAmount - extracted, ctx);
			if (extracted >= maxAmount) {
				return new ResourceAmount<>(resource, extracted);
			}
		}

		return resourceExtracting != null ? new ResourceAmount<>(resourceExtracting, extracted) : null;
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorage(BlockEntity be) {
		return be == null ? null : getItemStorage(be.getLevel(), be.getBlockPos(), be, null);
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorage(Level level, BlockPos pos) {
		return getItemStorage(level, pos, null);
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorage(Level level, BlockPos pos, @Nullable Direction side) {
		if (level == null || pos == null)
			return null;
		return ItemStorage.SIDED.find(level, pos, side);
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorage(Level level, BlockPos pos, @Nullable BlockEntity be, @Nullable Direction side) {
		if (level == null || pos == null)
			return null;
		return be != null ? ItemStorage.SIDED.find(level, pos, be.getBlockState(), be, side) : ItemStorage.SIDED.find(level, pos, side);
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorage(BlockEntity be) {
		return be == null ? null : getFluidStorage(be.getLevel(), be.getBlockPos(), be, null);
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorage(BlockEntity be, @Nullable Direction side) {
		return be == null ? null : getFluidStorage(be.getLevel(), be.getBlockPos(), be, side);
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorage(Level level, BlockPos pos) {
		return getFluidStorage(level, pos, null);
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorage(Level level, BlockPos pos, @Nullable Direction side) {
		if (level == null || pos == null)
			return null;
		return FluidStorage.SIDED.find(level, pos, side);
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorage(Level level, BlockPos pos, @Nullable BlockEntity be, @Nullable Direction side) {
		if (level == null || pos == null)
			return null;
		return be != null ? FluidStorage.SIDED.find(level, pos, be.getBlockState(), be, side) : FluidStorage.SIDED.find(level, pos, side);
	}

	public static OptionalLong firstCapacity(Storage<?> storage) {
		for (StorageView<?> view : storage) {
			return OptionalLong.of(view.getCapacity());
		}
		return OptionalLong.empty();
	}

	public static <T> void clear(Storage<T> storage) {
		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<T> view : storage.nonEmptyViews()) {
				view.extract(view.getResource(), view.getAmount(), t);
			}
			t.commit();
		}
	}

	public static <T> T commit(Function<TransactionContext, T> function) {
		try (Transaction t = Transaction.openOuter()) {
			T value = function.apply(t);
			t.commit();
			return value;
		}
	}

	public static <T> T simulate(Function<TransactionContext, T> function) {
		try (Transaction t = Transaction.openOuter()) {
			return function.apply(t);
		}
	}

	public static int truncateLong(long amount) {
		return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
	}

	public static long totalCapacity(Storage<?> storage) {
		long total = 0;
		for (StorageView<?> view : storage)
			total += view.getCapacity();
		return total;
	}

	public static List<ItemStack> getAllItems(Storage<ItemVariant> storage) {
		List<ItemStack> result = new ArrayList<>();
		for (StorageView<ItemVariant> view : storage.nonEmptyViews())
			result.add(view.getResource().toStack(truncateLong(view.getAmount())));
		return result;
	}

	public static List<ItemStack> extractAllAsStacks(Storage<ItemVariant> storage) {
		List<ItemStack> extracted = new ArrayList<>();
		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
				long amount = view.extract(view.getResource(), view.getAmount(), t);
				if (amount > 0)
					extracted.add(view.getResource().toStack(truncateLong(amount)));
			}
			t.commit();
		}
		return extracted;
	}

	public static FluidStack firstOrEmpty(Storage<FluidVariant> storage) {
		for (StorageView<FluidVariant> view : storage.nonEmptyViews())
			return new FluidStack(view.getResource(), view.getAmount());
		return FluidStack.EMPTY;
	}

	public static @Nullable FluidStack extractAnyFluid(Storage<FluidVariant> storage, long maxAmount, TransactionContext ctx) {
		ResourceAmount<FluidVariant> extracted = StorageUtil.extractAny(storage, maxAmount, ctx);
		return extracted == null ? FluidStack.EMPTY : new FluidStack(extracted);
	}

	public static @Nullable FluidStack extractAnyFluid(Storage<FluidVariant> storage, long maxAmount) {
		return commit(ctx -> extractAnyFluid(storage, maxAmount, ctx));
	}

	public static ItemStack extractAnyItem(Storage<ItemVariant> storage, long maxAmount, TransactionContext ctx) {
		ResourceAmount<ItemVariant> extracted = StorageUtil.extractAny(storage, maxAmount, ctx);
		return extracted == null ? ItemStack.EMPTY : extracted.resource().toStack(truncateLong(extracted.amount()));
	}

	public static ItemStack extractAnyItem(Storage<ItemVariant> storage, long maxAmount) {
		return commit(ctx -> extractAnyItem(storage, maxAmount, ctx));
	}

	public static long extract(Storage<ItemVariant> storage, ItemVariant resource, long maxAmount) {
		return commit(ctx -> storage.extract(resource, maxAmount, ctx));
	}

	public static Optional<FluidStack> getFluidContained(ItemStack stack) {
		return io.github.fabricators_of_create.porting_lib.transfer.TransferUtil.getFluidContained(stack)
			.map(portingStack -> new FluidStack(portingStack.getVariant(), portingStack.getAmount()));
	}
}
