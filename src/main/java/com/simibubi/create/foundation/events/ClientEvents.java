package com.simibubi.create.foundation.events;

import com.simibubi.create.CreateClient;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ClientEvents {

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> CreateClient.checkGraphicsFanciness());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CreateClient.RAILWAYS.cleanUp());
	}
}
