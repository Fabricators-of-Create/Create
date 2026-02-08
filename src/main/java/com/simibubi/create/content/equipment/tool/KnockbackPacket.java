package com.simibubi.create.content.equipment.tool;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public record KnockbackPacket(float yRot, float strength) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, KnockbackPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, KnockbackPacket::yRot,
	    ByteBufCodecs.FLOAT, KnockbackPacket::strength,
	    KnockbackPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.KNOCKBACK;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(LocalPlayer player) {
		if (player != null)
			player.knockback(strength, Mth.sin(yRot * ((float) Math.PI / 180F)), -Mth.cos(yRot * ((float) Math.PI / 180F)));
	}
}
