package com.simibubi.create.foundation.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.tterrag.registrate.builders.MenuBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@Mixin(MenuBuilder.class)
public class MenuBuilderMixin {

	private static final StreamCodec<RegistryFriendlyByteBuf, RegistryFriendlyByteBuf> CREATE_PACKET_CODEC =
		new StreamCodec<>() {
			@Override
			public void encode(RegistryFriendlyByteBuf target, RegistryFriendlyByteBuf source) {
				int length = source.readableBytes();
				ByteBufCodecs.VAR_INT.encode(target, length);
				target.writeBytes(source.slice(source.readerIndex(), length));
			}

			@Override
			public RegistryFriendlyByteBuf decode(RegistryFriendlyByteBuf source) {
				int length = ByteBufCodecs.VAR_INT.decode(source);
				return new RegistryFriendlyByteBuf(source.readBytes(length), source.registryAccess());
			}
		};

	@ModifyArg(
		method = "createEntry",
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/fabric/api/screenhandler/v1/ExtendedScreenHandlerType;<init>(Lnet/fabricmc/fabric/api/screenhandler/v1/ExtendedScreenHandlerType$ExtendedFactory;Lnet/minecraft/network/codec/StreamCodec;)V"
		),
		index = 1
	)
	private StreamCodec<RegistryFriendlyByteBuf, RegistryFriendlyByteBuf> create$ensureCodec(
		StreamCodec<RegistryFriendlyByteBuf, RegistryFriendlyByteBuf> codec
	) {
		return codec != null ? codec : CREATE_PACKET_CODEC;
	}
}
