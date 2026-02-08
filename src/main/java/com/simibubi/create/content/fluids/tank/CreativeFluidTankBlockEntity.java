package com.simibubi.create.content.fluids.tank;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.foundation.codec.CreateCodecs;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;
import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidTank;

public class CreativeFluidTankBlockEntity extends FluidTankBlockEntity {

	public CreativeFluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
				net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
				AllBlockEntityTypes.CREATIVE_FLUID_TANK.get(),
				(be, context) -> {
					if (be.exposedTank == null)
						be.refreshCapability();
					return be.exposedTank;
				}
		);
	}

	@Override
	protected SmartFluidTank createInventory() {
		return new CreativeSmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return false;
	}

	public static class CreativeSmartFluidTank extends SmartFluidTank {
		public static final Codec<CreativeSmartFluidTank> CODEC = RecordCodecBuilder.create(i -> i.group(
			FluidStack.OPTIONAL_CODEC.fieldOf("fluid").forGetter(FluidTank::getFluid),
			CreateCodecs.NON_NEGATIVE_LONG.fieldOf("capacity").forGetter(FluidTank::getCapacity)
		).apply(i, (fluid, capacity) -> {
			CreativeSmartFluidTank tank = new CreativeSmartFluidTank(capacity, $ -> {});
			tank.setFluid(fluid);
			return tank;
		}));

		public CreativeSmartFluidTank(long capacity, Consumer<FluidStack> updateCallback) {
			super(capacity, updateCallback);
		}

		@Override
		public long getFluidAmount() {
			return getFluid().isEmpty() ? 0 : getCapacity();
		}

		public void setContainedFluid(FluidStack fluidStack) {
			FluidStack fluid = fluidStack.copy();
			if (!fluidStack.isEmpty())
				fluid.setAmount(getCapacity());
			setFluid(fluid);
			onContentsChanged();
		}

		@Override
		public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
			return maxAmount;
		}

		@Override
		public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
			return maxAmount;
		}
	}

}
