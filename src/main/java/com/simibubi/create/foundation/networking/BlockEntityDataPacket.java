package com.simibubi.create.foundation.networking;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A server to client version of {@link BlockEntityConfigurationPacket}
 */
public abstract class BlockEntityDataPacket<BE extends SyncedBlockEntity> implements ClientboundPacketPayload {
	protected final BlockPos pos;

	public BlockEntityDataPacket(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(LocalPlayer player) {
		BlockEntity blockEntity = player.clientLevel.getBlockEntity(pos);

		if (blockEntity instanceof SyncedBlockEntity) {
			handlePacket((BE) blockEntity);
		}
	}

	protected abstract void handlePacket(BE blockEntity);
}
