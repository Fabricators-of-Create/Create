package com.simibubi.create.content.fluids.hosePulley;

import java.util.function.Supplier;

import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;
import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

public class HosePulleyFluidHandler implements SingleSlotStorage<FluidVariant> {

	// The dynamic interface

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		if (!internalTank.isEmpty() && !internalTank.getFluid().getVariant().equals(resource))
			return 0;
		if (resource.isBlank() || !FluidHelper.hasBlockState(resource.getFluid()))
			return 0;

		long diff = maxAmount;
		long totalAmountAfterFill = diff + internalTank.getFluidAmount();
		long remaining = maxAmount;
		boolean deposited = false;

		if (predicate.get() && totalAmountAfterFill >= FluidConstants.BUCKET) {
			if (filler.tryDeposit(resource.getFluid(), rootPosGetter.get(), transaction)) {
				drainer.counterpartActed(transaction);
				remaining -= FluidConstants.BUCKET;
				diff -= FluidConstants.BUCKET;
				deposited = true;
			}
		}

		if (diff <= 0) {
			internalTank.extract(resource, -diff, transaction);
			return maxAmount;
		}

		return internalTank.insert(resource, remaining, transaction) + (deposited ? FluidConstants.BUCKET : 0);
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		if (resource != null && !internalTank.isEmpty() && !internalTank.getFluid().getVariant().equals(resource))
			return 0;
		if (internalTank.getFluidAmount() >= FluidConstants.BUCKET)
			return internalTank.extract(resource, maxAmount, transaction);
		BlockPos pos = rootPosGetter.get();
		FluidStack returned = drainer.getDrainableFluid(pos);
		if (!predicate.get() || !drainer.pullNext(pos, transaction))
			return internalTank.extract(resource, maxAmount, transaction);

		filler.counterpartActed(transaction);
		FluidStack leftover = returned.copy();
		long available = FluidConstants.BUCKET + internalTank.getFluidAmount();
		long drained;

		if ((!internalTank.isEmpty() && !FluidStack.isSameFluidSameComponents(internalTank.getFluid(), returned))
			|| returned.isEmpty())
			return internalTank.extract(resource, maxAmount, transaction);

		if (resource != null && !returned.getVariant().equals(resource))
			return 0;

		drained = Math.min(maxAmount, available);
		returned.setAmount(drained);
		leftover.setAmount(available - drained);
		if (!leftover.isEmpty())
			internalTank.setFluid(leftover, transaction);
		return returned.getAmount();
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public FluidVariant getResource() {
		if (!internalTank.isResourceBlank() || drainer.blockEntity.getLevel() == null) return internalTank.getResource();
		FluidState state = drainer.blockEntity.getLevel().getFluidState(rootPosGetter.get());
		Fluid f = state.getType();
		if (f instanceof FlowingFluid flowing) f = flowing.getSource();
		if (!f.isSource(state)) return FluidVariant.blank();
		return TransferUtil.fluidVariantOf(f);
	}

	@Override
	public long getAmount() {
		return isResourceBlank() ? 0 : Long.MAX_VALUE;
	}

	@Override
	public long getCapacity() {
		return Long.MAX_VALUE;
	}

	//

	private SmartFluidTank internalTank;
	private FluidFillingBehaviour filler;
	private FluidDrainingBehaviour drainer;
	private Supplier<BlockPos> rootPosGetter;
	private Supplier<Boolean> predicate;

	public HosePulleyFluidHandler(SmartFluidTank internalTank, FluidFillingBehaviour filler,
		FluidDrainingBehaviour drainer, Supplier<BlockPos> rootPosGetter, Supplier<Boolean> predicate) {
		this.internalTank = internalTank;
		this.filler = filler;
		this.drainer = drainer;
		this.rootPosGetter = rootPosGetter;
		this.predicate = predicate;
	}

	public SmartFluidTank getInternalTank() {
		return internalTank;
	}
}
