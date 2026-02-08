package com.simibubi.create.content.processing.basin;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.logistics.funnel.FunnelBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

public class BasinBlock extends Block implements IBE<BasinBlockEntity>, IWrenchable {

	public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;

	public BasinBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> p_206840_1_) {
		super.createBlockStateDefinition(p_206840_1_.add(FACING));
	}

	public static boolean isBasin(LevelReader world, BlockPos pos) {
		return world.getBlockEntity(pos) instanceof BasinBlockEntity;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockEntity blockEntity = world.getBlockEntity(pos.above());
		if (blockEntity instanceof BasinOperatingBlockEntity)
			return false;
		return true;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (!context.getLevel().isClientSide)
			withBlockEntityDo(context.getLevel(), context.getClickedPos(),
				bte -> bte.onWrenched(context.getClickedFace()));
		return InteractionResult.SUCCESS;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
									 return onBlockEntityUseItemOn(level, pos, be -> {
				Direction direction = hitResult.getDirection();
				if (!stack.isEmpty()) {
					if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, be, direction))
						return ItemInteractionResult.SUCCESS;
					if (FluidHelper.tryFillItemFromBE(level, player, hand, stack, be, direction))
						return ItemInteractionResult.SUCCESS;

				if (GenericItemEmptying.canItemBeEmptied(level, stack)
					|| GenericItemFilling.canItemBeFilled(level, stack))
					return ItemInteractionResult.SUCCESS;
				if (stack.getItem().equals(Items.SPONGE)) {
					Storage<FluidVariant> storage = be.getFluidStorage(direction);
					if (storage != null && !TransferUtil.extractAnyFluid(storage, Long.MAX_VALUE).isEmpty()) {
						return ItemInteractionResult.SUCCESS;
					}
				}
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
				}

				Storage<ItemVariant> inv = be.itemCapability;
				if (inv == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
				List<ItemStack> extracted = TransferUtil.extractAllAsStacks(inv);
				if (extracted.size() > 0) {
				extracted.forEach(s -> player.getInventory().placeItemBackInInventory(s));
				level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f,
					1f + level.getRandom().nextFloat());
			}
			be.onEmptied();
			return ItemInteractionResult.SUCCESS;
		});
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);
		if (!worldIn.getBlockState(entityIn.blockPosition())
			.is(this))
			return;
		if (!(entityIn instanceof ItemEntity itemEntity))
			return;
		if (!entityIn.isAlive())
			return;
		withBlockEntityDo(worldIn, entityIn.blockPosition(), be -> {

			// Tossed items bypass the quarter-stack limit
			be.inputInventory.withMaxStackSize(64);
			ItemStack stack = itemEntity.getItem().copy();
			try (Transaction t = Transaction.openOuter()) {
				long inserted = be.inputInventory.insert(ItemVariant.of(stack), stack.getCount(), t);
				t.commit();

				if (inserted == stack.getCount()) {
					itemEntity.discard();

					return;
				}

				stack.setCount((int) (stack.getCount() - inserted));
				itemEntity.setItem(stack);
			}
		});
	}

	@Override
	public VoxelShape getInteractionShape(BlockState p_199600_1_, BlockGetter p_199600_2_, BlockPos p_199600_3_) {
		return AllShapes.BASIN_RAYTRACE_SHAPE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.BASIN_BLOCK_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
		if (ctx instanceof EntityCollisionContext && ((EntityCollisionContext) ctx).getEntity() instanceof ItemEntity)
			return AllShapes.BASIN_COLLISION_SHAPE;
		return getShape(state, reader, pos, ctx);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		IBE.onRemove(state, worldIn, pos, newState);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
		return getBlockEntityOptional(worldIn, pos).map(BasinBlockEntity::getInputInventory)
				.filter(basin -> !Transaction.isOpen()) // fabric: hack fix for comparators updating when they shouldn't
			.map(ItemHelper::calcRedstoneFromInventory)
			.orElse(0);
	}

	@Override
	public Class<BasinBlockEntity> getBlockEntityClass() {
		return BasinBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends BasinBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.BASIN.get();
	}

	public static boolean canOutputTo(BlockGetter world, BlockPos basinPos, Direction direction) {
		BlockPos neighbour = basinPos.relative(direction);
		BlockPos output = neighbour.below();
		BlockState blockState = world.getBlockState(neighbour);

		if (FunnelBlock.isFunnel(blockState)) {
			if (FunnelBlock.getFunnelFacing(blockState) == direction)
				return false;
		} else if (!blockState.getCollisionShape(world, neighbour)
			.isEmpty()) {
			return false;
		} else {
			BlockEntity blockEntity = world.getBlockEntity(output);
			if (blockEntity instanceof BeltBlockEntity belt) {
				return belt.getSpeed() == 0 || belt.getMovementFacing() != direction.getOpposite();
			}
		}

		DirectBeltInputBehaviour directBeltInputBehaviour =
			BlockEntityBehaviour.get(world, output, DirectBeltInputBehaviour.TYPE);
		if (directBeltInputBehaviour != null)
			return directBeltInputBehaviour.canInsertFromSide(direction);
		return false;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

}
