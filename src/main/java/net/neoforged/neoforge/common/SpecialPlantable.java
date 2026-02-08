package net.neoforged.neoforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface SpecialPlantable {
	boolean canPlacePlantAtPosition(ItemStack stack, Level level, BlockPos pos, Player player);

	void spawnPlantAtPosition(ItemStack stack, Level level, BlockPos pos, Player player);
}
