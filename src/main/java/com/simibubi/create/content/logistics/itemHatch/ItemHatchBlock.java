package com.simibubi.create.content.logistics.itemHatch;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.fabric.block.SecondaryUseBypassingBlock;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.fabric.api.entity.FakePlayer;

public class ItemHatchBlock extends HorizontalDirectionalBlock
	implements IBE<ItemHatchBlockEntity>, IWrenchable, ProperWaterloggedBlock, SecondaryUseBypassingBlock {
	public static final MapCodec<ItemHatchBlock> CODEC = simpleCodec(ItemHatchBlock::new);

	public static final BooleanProperty OPEN = BooleanProperty.create("open");

	public ItemHatchBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(OPEN, false)
			.setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(OPEN, FACING, WATERLOGGED));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState state = super.getStateForPlacement(pContext);
		if (state == null)
			return state;
		if (pContext.getClickedFace()
			.getAxis()
			.isVertical())
			return null;

		return withWater(state.setValue(FACING, pContext.getClickedFace()
			.getOpposite())
			.setValue(OPEN, false), pContext);
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
		updateWater(pLevel, pState, pPos);
		return pState;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (level.isClientSide())
			return ItemInteractionResult.SUCCESS;
		if (player instanceof FakePlayer)
			return ItemInteractionResult.SUCCESS;

		Direction facing = state.getValue(FACING);
		BlockPos targetPos = pos.relative(facing);
		Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, targetPos, facing.getOpposite());
		if (storage == null)
			return ItemInteractionResult.FAIL;

		FilteringBehaviour filter = BlockEntityBehaviour.get(level, pos, FilteringBehaviour.TYPE);
		if (filter == null)
			return ItemInteractionResult.FAIL;

		Inventory inventory = player.getInventory();
		boolean anyInserted = false;
		boolean depositItemInHand = !player.isShiftKeyDown();

		if (!depositItemInHand && AllItemTags.WRENCH.matches(stack))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		for (int i = 0; i < inventory.items.size(); i++) {
			if (Inventory.isHotbarSlot(i) != depositItemInHand)
				continue;
			if (depositItemInHand && i != inventory.selected)
				continue;
			ItemStack item = inventory.getItem(i);
			if (item.isEmpty())
				continue;
			if (!item.getItem()
				.canFitInsideContainerItems() && !PackageItem.isPackage(item))
				continue;
			if (!filter.getFilter()
				.isEmpty() && !filter.test(item))
				continue;

			long inserted = TransferUtil.insert(storage, item);
			if (inserted <= 0)
				continue;

			anyInserted = true;
			int newSize = TransferUtil.truncateLong(item.getCount() - inserted);
			ItemStack newStack = newSize <= 0 ? ItemStack.EMPTY : item.copyWithCount(newSize);
			inventory.setItem(i, newStack);
		}

		if (!anyInserted)
			return ItemInteractionResult.SUCCESS;

		AllSoundEvents.ITEM_HATCH.playOnServer(level, pos);
		level.setBlockAndUpdate(pos, state.setValue(OPEN, true));
		level.scheduleTick(pos, this, 10);

		CreateLang.translate(depositItemInHand ? "item_hatch.deposit_item" : "item_hatch.deposit_inventory")
			.sendStatus(player);
		return ItemInteractionResult.SUCCESS;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.ITEM_HATCH.get(pState.getValue(FACING)
			.getOpposite());
	}

	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		if (pState.getValue(OPEN))
			pLevel.setBlockAndUpdate(pPos, pState.setValue(OPEN, false));
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		IBE.onRemove(state, level, pos, newState);
	}

	@Override
	public Class<ItemHatchBlockEntity> getBlockEntityClass() {
		return ItemHatchBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ItemHatchBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.ITEM_HATCH.get();
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	public boolean shouldBypassSecondaryUse(Player player, InteractionHand hand, BlockState state) {
		return true;
	}
}
