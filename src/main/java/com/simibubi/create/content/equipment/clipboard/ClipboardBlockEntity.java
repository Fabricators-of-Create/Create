package com.simibubi.create.content.equipment.clipboard;

import java.util.List;
import java.util.UUID;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.AddressEditBoxHelper;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.platform.CatnipServices;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import io.github.fabricators_of_create.porting_lib.common.util.EnvExecutor;


public class ClipboardBlockEntity extends SmartBlockEntity {

	public ItemStack dataContainer;
	private UUID lastEdit;

	public ClipboardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		dataContainer = AllBlocks.CLIPBOARD.asStack();
	}

	@Override
	public void initialize() {
		super.initialize();
		updateWrittenState();
	}

	public void onEditedBy(Player player) {
		lastEdit = player.getUUID();
		notifyUpdate();
		updateWrittenState();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (level.isClientSide())
			CatnipServices.PLATFORM.executeOnClientOnly(() -> this::advertiseToAddressHelper);
	}

	public void updateWrittenState() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.CLIPBOARD.has(blockState))
			return;
		if (level.isClientSide())
			return;
		boolean isWritten = blockState.getValue(ClipboardBlock.WRITTEN);
		boolean shouldBeWritten = !dataContainer.getComponentsPatch().isEmpty();
		if (isWritten == shouldBeWritten)
			return;
		level.setBlockAndUpdate(worldPosition, blockState.setValue(ClipboardBlock.WRITTEN, shouldBeWritten));
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.put("Item", dataContainer.saveOptional(registries));
		if (clientPacket && lastEdit != null)
			tag.putUUID("LastEdit", lastEdit);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		dataContainer = ItemStack.parseOptional(registries, tag.getCompound("Item"));
		if (!AllBlocks.CLIPBOARD.isIn(dataContainer))
			dataContainer = AllBlocks.CLIPBOARD.asStack();

		if (clientPacket)
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> readClientSide(tag));
	}

	@Environment(EnvType.CLIENT)
	private void readClientSide(CompoundTag tag) {
		Minecraft mc = Minecraft.getInstance();
		if (!(mc.screen instanceof ClipboardScreen cs))
			return;
		if (tag.contains("LastEdit") && tag.getUUID("LastEdit")
			.equals(mc.player.getUUID()))
			return;
		if (!worldPosition.equals(cs.targetedBlock))
			return;
		cs.reopenWith(dataContainer);
	}

	@Environment(EnvType.CLIENT)
	private void advertiseToAddressHelper() {
		AddressEditBoxHelper.advertiseClipboard(this);
	}

}
