package com.simibubi.create.content.trains.entity;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public class CarriageDataUpdatePacket implements ClientboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, CarriageDataUpdatePacket> STREAM_CODEC = StreamCodec.of(
		(buf, packet) -> packet.write(buf),
		CarriageDataUpdatePacket::new
	);

	private final int entityId;
	private final CarriageSyncData data;

	public CarriageDataUpdatePacket(CarriageContraptionEntity entity) {
		this.entityId = entity.getId();
		this.data = entity.carriageData;
	}

	private CarriageDataUpdatePacket(RegistryFriendlyByteBuf buf) {
		this.entityId = buf.readVarInt();
		this.data = new CarriageSyncData();
		this.data.read(buf);
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		data.write(buf);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(LocalPlayer player) {
		Entity entity = player.clientLevel.getEntity(entityId);
		if (entity instanceof CarriageContraptionEntity carriage) {
			carriage.onCarriageDataUpdate(data);
		} else {
			Create.LOGGER.error("Invalid CarriageDataUpdatePacket for non-carriage entity: {}", entity);
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CARRIAGE_DATA_UPDATE;
	}
}
