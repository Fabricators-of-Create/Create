package io.github.fabricators_of_create.porting_lib.util;

import net.minecraft.server.MinecraftServer;

public class ServerLifecycleHooks {
	private static MinecraftServer currentServer;

	private ServerLifecycleHooks() {}

	public static MinecraftServer getCurrentServer() {
		return currentServer;
	}

	public static void setCurrentServer(MinecraftServer server) {
		currentServer = server;
	}
}
