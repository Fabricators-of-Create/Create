package com.simibubi.create.foundation.events;

import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.zapper.ZapperInteractionHandler;
import com.simibubi.create.content.equipment.zapper.ZapperItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class CommonEvents {

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Create.SCHEMATIC_RECEIVER.tick();
			Create.LAGGER.tick();
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> Create.SCHEMATIC_RECEIVER.shutdown());
	}

	public static void leftClickEmpty(ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof ZapperItem) {
			ZapperInteractionHandler.trySelect(stack, player);
		}
	}
}
