package com.simibubi.create.foundation.utility.fabric;

import net.minecraft.world.entity.player.Player;

public class ReachUtil {
	private ReachUtil() {}

	public static double reach(Player player) {
		return player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE);
	}
}
