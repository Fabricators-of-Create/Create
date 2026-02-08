package com.simibubi.create.content.kinetics.millstone;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;

import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;
import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;

public class MillstoneBlock extends KineticBlock implements IBE<MillstoneBlockEntity>, ICogWheel {

	public MillstoneBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.MILLSTONE;
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.DOWN;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!stack.isEmpty())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (level.isClientSide)
			return ItemInteractionResult.SUCCESS;

		withBlockEntityDo(level, pos, millstone -> {
			boolean emptyOutput = true;
			ItemStackHandler outputInv = millstone.outputInv;
			for (int slot = 0; slot < outputInv.getSlotCount(); slot++) {
				ItemStack stackInSlot = outputInv.getStackInSlot(slot);
				if (!stackInSlot.isEmpty())
					emptyOutput = false;
				player.getInventory()
					.placeItemBackInInventory(stackInSlot);
				outputInv.setStackInSlot(slot, ItemStack.EMPTY);
			}

			if (emptyOutput) {
				var inputInv = millstone.inputInv;
				for (int slot = 0; slot < inputInv.getSlotCount(); slot++) {
					player.getInventory()
						.placeItemBackInInventory(inputInv.getStackInSlot(slot));
					inputInv.setStackInSlot(slot, ItemStack.EMPTY);
				}
			}

			millstone.setChanged();
			millstone.sendData();
		});

		return ItemInteractionResult.SUCCESS;
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);

		if (entityIn.level().isClientSide)
			return;
		if (!(entityIn instanceof ItemEntity itemEntity))
			return;
		if (!entityIn.isAlive())
			return;

		MillstoneBlockEntity millstone = null;
		for (BlockPos pos : Iterate.hereAndBelow(entityIn.blockPosition()))
			if (millstone == null)
				millstone = getBlockEntity(worldIn, pos);

		if (millstone == null)
			return;

		Storage<ItemVariant> handler = millstone.getItemStorage(null);
		if (handler == null)
			return;

		try (Transaction t = Transaction.openOuter()) {
			ItemStack inEntity = itemEntity.getItem();
				long inserted = handler.insert(ItemVariant.of(inEntity), inEntity.getCount(), t);
				if (inserted == inEntity.getCount())
					itemEntity.discard();
				else itemEntity.setItem(inEntity.copyWithCount((int) (inEntity.getCount() - inserted)));
				t.commit();
			}
		}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public Class<MillstoneBlockEntity> getBlockEntityClass() {
		return MillstoneBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends MillstoneBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.MILLSTONE.get();
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

}
