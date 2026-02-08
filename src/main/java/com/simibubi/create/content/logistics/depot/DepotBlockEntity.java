package com.simibubi.create.content.logistics.depot;

import java.util.List;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;

public class DepotBlockEntity extends SmartBlockEntity implements SidedStorageBlockEntity {

	DepotBehaviour depotBehaviour;

	public DepotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
				net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
				AllBlockEntityTypes.DEPOT.get(),
				(be, context) -> be.depotBehaviour.itemHandler
		);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(depotBehaviour = new DepotBehaviour(this));
		depotBehaviour.addSubBehaviours(behaviours);
	}

	@Nullable
	@Override
	public Storage<ItemVariant> getItemStorage(@Nullable Direction direction) {
		return depotBehaviour.itemHandler;
	}

	public ItemStack getHeldItem() {
		return depotBehaviour.getHeldItemStack();
	}

	public void setHeldItem(ItemStack item) {
		TransportedItemStack newStack = new TransportedItemStack(item);
		if (depotBehaviour.heldItem != null)
			newStack.angle = depotBehaviour.heldItem.angle;
		depotBehaviour.setHeldItem(newStack);
	}

}
