package com.simibubi.create.content.fluids.pipes;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL_HONEY;

import com.simibubi.create.AllFluids;
import com.simibubi.create.foundation.fluid.FluidHelper;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.CauldronFluidContent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public class VanillaFluidTargets {

	public static boolean canProvideFluidWithoutCapability(BlockState state) {
		if (state.hasProperty(BlockStateProperties.LEVEL_HONEY))
			return true;
		if (CauldronFluidContent.getForBlock(state.getBlock()) != null)
			return true;
		return false;
	}

	public static FluidStack drainBlock(Level level, BlockPos pos, BlockState state, TransactionContext ctx) {
		if (state.hasProperty(BlockStateProperties.LEVEL_HONEY) && state.getValue(LEVEL_HONEY) >= 5) {
			level.updateSnapshots(ctx);
			level.setBlock(pos, state.setValue(LEVEL_HONEY, 0), 3);
			return new FluidStack(FluidHelper.convertToStill(AllFluids.HONEY.get()), FluidConstants.BOTTLE);
		}

		if (state.is(Blocks.LAVA_CAULDRON)) {
			level.updateSnapshots(ctx);
			level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
			return new FluidStack(Fluids.LAVA, FluidConstants.BUCKET);
		}

		Block block = state.getBlock();
		CauldronFluidContent content = CauldronFluidContent.getForBlock(block);
		if (content != null && block instanceof LayeredCauldronBlock lcb) {
			if (!lcb.isFull(state))
				return FluidStack.EMPTY;
			level.updateSnapshots(ctx);
			level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
			return new FluidStack(content.fluid, FluidConstants.BUCKET);
		}

		return FluidStack.EMPTY;
	}

}
