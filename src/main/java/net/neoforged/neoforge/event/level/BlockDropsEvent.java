package net.neoforged.neoforge.event.level;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockDropsEvent {
	private boolean canceled;
	private int droppedExperience;

	public BlockDropsEvent(ServerLevel level, BlockPos pos, BlockState state, BlockEntity blockEntity, List<ItemStack> drops,
		Player player, ItemStack usedTool) {
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public int getDroppedExperience() {
		return droppedExperience;
	}

	public void setDroppedExperience(int droppedExperience) {
		this.droppedExperience = droppedExperience;
	}
}
