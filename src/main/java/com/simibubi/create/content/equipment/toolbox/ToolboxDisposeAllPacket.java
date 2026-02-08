package com.simibubi.create.content.equipment.toolbox;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.utility.PersistentDataHelper;

import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

public record ToolboxDisposeAllPacket(BlockPos toolboxPos) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, com.simibubi.create.content.equipment.toolbox.ToolboxDisposeAllPacket> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
			com.simibubi.create.content.equipment.toolbox.ToolboxDisposeAllPacket::new, com.simibubi.create.content.equipment.toolbox.ToolboxDisposeAllPacket::toolboxPos
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.TOOLBOX_DISPOSE_ALL;
	}

	@Override
	public void handle(ServerPlayer player) {
		Level world = player.level();
		BlockEntity blockEntity = world.getBlockEntity(toolboxPos);

		double maxRange = ToolboxHandler.getMaxRange(player);
		if (player.distanceToSqr(toolboxPos.getX() + 0.5, toolboxPos.getY(), toolboxPos.getZ() + 0.5) > maxRange
				* maxRange)
			return;
		if (!(blockEntity instanceof ToolboxBlockEntity toolbox))
			return;

		CompoundTag compound = PersistentDataHelper.get(player)
				.getCompound("CreateToolboxData");
		MutableBoolean sendData = new MutableBoolean(false);

			toolbox.inventory.inLimitedMode(inventory -> {
				try (Transaction t = Transaction.openOuter()) {
					PlayerInventoryStorage playerInv = PlayerInventoryStorage.of(player);
					for (int i = 0; i < 36; i++) {
						String key = String.valueOf(i);
						if (compound.contains(key) && NBTHelper.readBlockPos(compound.getCompound(key), "Pos")
							.equals(toolboxPos)) {
							ToolboxHandler.unequip(player, i, true);
							sendData.setTrue();
						}

						SingleSlotStorage<ItemVariant> slot = playerInv.getSlot(i);
						if (slot.isResourceBlank())
							continue;
						long amount = slot.getAmount();
						ItemVariant resource = slot.getResource();

						long inserted = inventory.insert(resource, amount, t);
						if (inserted == 0)
							continue;
						slot.extract(resource, inserted, t);
					}
					t.commit();
				}
			});

		if (sendData.booleanValue())
			ToolboxHandler.syncData(player);
	}
}
