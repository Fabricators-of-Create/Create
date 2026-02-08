package com.simibubi.create.content.fluids.tank;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;

import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

public class FluidTankItem extends BlockItem {
	// fabric: (#690) because of ordering differences, we need to delay connection by a tick when placing multiblocks with NBT.
	// If the item has NBT, it needs to be applied to a controller. However, ordering is different on fabric.
	// on forge, the block is placed, the data is set, and the tanks connect.
	// on fabric, the block is placed, the tanks connect, and the data is set.
	// However, now that the tank is not a controller, nothing happens.
	// solution: hacky static state storage. If we're placing NBT, delay connection until next tick.
	@Internal
	public static boolean IS_PLACING_NBT = false;

	public FluidTankItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public InteractionResult place(BlockPlaceContext ctx) {
		IS_PLACING_NBT = FluidTankItem.checkPlacingNbt(ctx);
		InteractionResult initialResult = super.place(ctx);
		IS_PLACING_NBT = false;
		if (!initialResult.consumesAction())
			return initialResult;
		tryMultiPlace(ctx);
		return initialResult;
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos blockPos, Level level, Player player,
		ItemStack itemStack, BlockState blockState) {
		MinecraftServer minecraftserver = level.getServer();
		if (minecraftserver == null)
			return false;
		CustomData blockEntityData = itemStack.get(DataComponents.BLOCK_ENTITY_DATA);
		if (blockEntityData != null) {
			CompoundTag nbt = blockEntityData.copyTag();
			nbt.remove("Luminosity");
			nbt.remove("Size");
			nbt.remove("Height");
			nbt.remove("Controller");
			nbt.remove("LastKnownPos");
			if (nbt.contains("TankContent")) {
				FluidStack fluid = FluidStack.parseOptional(minecraftserver.registryAccess(), nbt.getCompound("TankContent"));
				if (!fluid.isEmpty()) {
					fluid.setAmount(Math.min(FluidTankBlockEntity.getCapacityMultiplier(), fluid.getAmount()));
					nbt.put("TankContent", fluid.saveOptional(minecraftserver.registryAccess()));
				}
			}
			BlockEntity.addEntityType(nbt, ((IBE<?>) this.getBlock()).getBlockEntityType());
			itemStack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(nbt));
		}
		return super.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState);
	}

	private void tryMultiPlace(BlockPlaceContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null)
			return;
		if (player.isShiftKeyDown())
			return;
		Direction face = ctx.getClickedFace();
		if (!face.getAxis()
			.isVertical())
			return;
		ItemStack stack = ctx.getItemInHand();
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockPos placedOnPos = pos.relative(face.getOpposite());
		BlockState placedOnState = world.getBlockState(placedOnPos);

		if (!FluidTankBlock.isTank(placedOnState))
			return;
		if (SymmetryWandItem.presentInHotbar(player))
			return;
		boolean creative = getBlock().equals(AllBlocks.CREATIVE_FLUID_TANK.get());
		FluidTankBlockEntity tankAt = ConnectivityHandler.partAt(
			creative ? AllBlockEntityTypes.CREATIVE_FLUID_TANK.get() : AllBlockEntityTypes.FLUID_TANK.get(), world, placedOnPos
		);
		if (tankAt == null)
			return;
		FluidTankBlockEntity controllerBE = tankAt.getControllerBE();
		if (controllerBE == null)
			return;

		int width = controllerBE.width;
		if (width == 1)
			return;

		int tanksToPlace = 0;
		BlockPos startPos = face == Direction.DOWN ? controllerBE.getBlockPos()
			.below()
			: controllerBE.getBlockPos()
				.above(controllerBE.height);

		if (startPos.getY() != pos.getY())
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (FluidTankBlock.isTank(blockState))
					continue;
				if (!blockState.canBeReplaced())
					return;
				tanksToPlace++;
			}
		}

		if (!player.isCreative() && stack.getCount() < tanksToPlace)
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (FluidTankBlock.isTank(blockState))
					continue;
				BlockPlaceContext context = BlockPlaceContext.at(ctx, offsetPos, face);
				player.getCustomData()
					.putBoolean("SilenceTankSound", true);
				IS_PLACING_NBT = checkPlacingNbt(context);
				super.place(context);
				IS_PLACING_NBT = false;
				player.getCustomData()
					.remove("SilenceTankSound");
			}
		}
	}

	public static boolean checkPlacingNbt(BlockPlaceContext ctx) {
		ItemStack item = ctx.getItemInHand();
		return item.has(DataComponents.BLOCK_ENTITY_DATA);
	}
}
