package com.simibubi.create.foundation.blockEntity.behaviour.inventory;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;
import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.StorageProvider;

public class TankManipulationBehaviour extends CapManipulationBehaviourBase<FluidVariant, TankManipulationBehaviour> {

	public static final BehaviourType<TankManipulationBehaviour> OBSERVE = new BehaviourType<>();
	private BehaviourType<TankManipulationBehaviour> behaviourType;

	public TankManipulationBehaviour(SmartBlockEntity be, InterfaceProvider target) {
		this(OBSERVE, be, target);
	}

	private TankManipulationBehaviour(BehaviourType<TankManipulationBehaviour> type, SmartBlockEntity be,
		InterfaceProvider target) {
		super(be, target);
		behaviourType = type;
	}

	public FluidStack extractAny() {
		if (!hasInventory())
			return FluidStack.EMPTY;
		Storage<FluidVariant> inventory = getInventory();
		Predicate<FluidStack> filterTest = getFilterTest(Predicates.alwaysTrue());

		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<FluidVariant> view : inventory) {
				if (!view.isResourceBlank()) {
					FluidStack stack = new FluidStack(view);
					if (!filterTest.test(stack))
						continue;
						long extracted = view.extract(view.getResource(), view.getAmount(), t);
						if (extracted != 0) {
							if (!simulateNext) t.commit();
							stack.setAmount(extracted);
							return stack;
						}
					}
				}
		}

		return FluidStack.EMPTY;
	}

	protected Predicate<FluidStack> getFilterTest(Predicate<FluidStack> customFilter) {
		Predicate<FluidStack> test = customFilter;
		FilteringBehaviour filter = blockEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null)
			test = customFilter.and(filter::test);
		return test;
	}

	@Override
	protected StorageProvider<FluidVariant> getProvider(BlockPos pos, boolean bypassSided) {
		return bypassSided
				? new UnsidedFluidStorageProvider(getWorld(), pos)
				: StorageProvider.createForFluids(getWorld(), pos);
	}

	@Override
	public BehaviourType<?> getType() {
		return behaviourType;
	}

	public static class UnsidedFluidStorageProvider extends UnsidedStorageProvider<FluidVariant> {
		protected UnsidedFluidStorageProvider(Level level, BlockPos pos) {
			super(FluidStorage.SIDED, level, pos);
		}

		@Nullable
		@Override
		public Storage<FluidVariant> get() {
			return TransferUtil.getFluidStorage(level, pos);
		}
	}
}
