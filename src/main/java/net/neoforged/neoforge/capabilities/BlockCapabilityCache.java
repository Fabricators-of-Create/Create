package net.neoforged.neoforge.capabilities;

import java.util.function.Supplier;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class BlockCapabilityCache<T, C> {
	private final Supplier<T> supplier;

	private BlockCapabilityCache(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	public static <T, C> BlockCapabilityCache<T, C> create(Object capability, ServerLevel level, BlockPos pos, C context) {
		return new BlockCapabilityCache<>(() -> resolve(capability, level, pos, context));
	}

	@SuppressWarnings("unchecked")
	private static <T, C> T resolve(Object capability, ServerLevel level, BlockPos pos, C context) {
		Direction side = context instanceof Direction d ? d : null;
		if (capability == net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK)
			return (T) TransferUtil.getItemStorage(level, pos, side);
		if (capability == net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK)
			return (T) TransferUtil.getFluidStorage(level, pos, side);
		return null;
	}

	public T getCapability() {
		return supplier.get();
	}
}
