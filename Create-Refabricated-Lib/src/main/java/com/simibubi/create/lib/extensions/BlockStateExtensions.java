package com.simibubi.create.lib.extensions;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public interface BlockStateExtensions {
	default boolean addRunningEffects(World world, BlockPos pos, Entity entity) {
		return false;
	}

	default boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
		return false;
	}

	default boolean addLandingEffects(ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return false;
	}

	default boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return false;
	}
}