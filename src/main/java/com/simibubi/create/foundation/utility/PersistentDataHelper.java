package com.simibubi.create.foundation.utility;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class PersistentDataHelper {
	private static final Map<UUID, CompoundTag> DATA = new ConcurrentHashMap<>();

	private PersistentDataHelper() {
	}

	public static CompoundTag get(Entity entity) {
		return DATA.computeIfAbsent(entity.getUUID(), id -> new CompoundTag());
	}

	public static void clear(Entity entity) {
		DATA.remove(entity.getUUID());
	}
}
