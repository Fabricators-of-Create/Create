package com.simibubi.create.content.fluids.transfer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.fluid.FluidHelper;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.TransactionSuccessCallback;
import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.BBHelper;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;

public class FluidDrainingBehaviour extends FluidManipulationBehaviour {

	public static final BehaviourType<FluidDrainingBehaviour> TYPE = new BehaviourType<>();

	Fluid fluid;

	// Execution
	Set<BlockPos> validationSet;
	// fabric: we need to save the queue for snapshots, so it must be a copyable type.
	SortedArraySet<BlockPosEntry> queue;
	boolean isValid;

	// Validation
	List<BlockPosEntry> validationFrontier;
	Set<BlockPos> validationVisited;
	Set<BlockPos> newValidationSet;

	SnapshotParticipant<Data> snapshotParticipant = new SnapshotParticipant<>() {
		@Override
		protected Data createSnapshot() {
			FluidDrainingBehaviour b = FluidDrainingBehaviour.this;
			BlockPos rootPos = b.rootPos == null ? null : b.rootPos.immutable();
			BoundingBox box = b.affectedArea == null ? null : new BoundingBox(
					affectedArea.minX(), affectedArea.minY(), affectedArea.minZ(),
					affectedArea.maxX(), affectedArea.maxY(), affectedArea.maxZ()
			);

			return new Data(
					fluid, isValid, rootPos, new ArrayList<>(validationFrontier), new HashSet<>(validationVisited),
					new HashSet<>(newValidationSet), revalidateIn, box, copySet(queue)
			);
		}

		@Override
		protected void readSnapshot(Data snapshot) {
			fluid = snapshot.fluid;
			isValid = snapshot.valid;
			validationFrontier = snapshot.validationFrontier;
			validationVisited = snapshot.validationVisited;
			newValidationSet = snapshot.newValidationSet;
			revalidateIn = snapshot.revalidateIn;
			affectedArea = snapshot.affectedArea;
			rootPos = snapshot.rootPos;
			queue = snapshot.queue;
		}
	};

	@Override
	protected SnapshotParticipant<?> snapshotParticipant() {
		return snapshotParticipant;
	}

	record Data(Fluid fluid, boolean valid, BlockPos rootPos, List<BlockPosEntry> validationFrontier,
				Set<BlockPos> validationVisited, Set<BlockPos> newValidationSet, int revalidateIn,
				BoundingBox affectedArea, SortedArraySet<BlockPosEntry> queue) {
	}

	public FluidDrainingBehaviour(SmartBlockEntity be) {
		super(be);
		validationVisited = new HashSet<>();
		validationFrontier = new ArrayList<>();
		validationSet = new HashSet<>();
		newValidationSet = new HashSet<>();
		queue = SortedArraySet.create(this::comparePositions);
	}

	@Nullable
	public boolean pullNext(BlockPos root, TransactionContext ctx) {
		if (!frontier.isEmpty())
			return false;
		if (!Objects.equals(root, rootPos)) {
			rebuildContext(root, ctx);
			return false;
		}

		if (counterpartActed) {
			counterpartActed = false;
			softReset(root, ctx);
			return false;
		}

		if (affectedArea == null)
			affectedArea = BoundingBox.fromCorners(root, root);

		Level world = getWorld();
		if (!queue.isEmpty() && !isValid) {
			rebuildContext(root, ctx);
			return false;
		}

		snapshotParticipant.updateSnapshots(ctx);
		if (validationFrontier.isEmpty() && !queue.isEmpty() && revalidateIn == 0)
			revalidate(root);

		if (infinite) {
			blockEntity.award(AllAdvancements.HOSE_PULLEY);
			if (FluidHelper.isLava(fluid))
				blockEntity.award(AllAdvancements.HOSE_PULLEY_LAVA);

			playEffect(world, root, fluid, true);
			return true;
		}

		while (!queue.isEmpty()) {
			// Dont dequeue here, so we can decide not to dequeue a valid entry when
			// simulating
			BlockPos currentPos = queue.first()
				.pos();

			BlockState blockState = world.getBlockState(currentPos);
			BlockState emptied = blockState;
			Fluid fluid = Fluids.EMPTY;

			if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)
				&& blockState.getValue(BlockStateProperties.WATERLOGGED)) {
				emptied = blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false));
				fluid = Fluids.WATER;
				} else if (blockState.getBlock() instanceof LiquidBlock flowingFluid) {
					emptied = Blocks.AIR.defaultBlockState();
					if (blockState.getValue(LiquidBlock.LEVEL) == 0)
						fluid = blockState.getFluidState().getType();
					else {
					affectedArea = BBHelper.encapsulate(affectedArea, BoundingBox.fromCorners(currentPos, currentPos));
					if (!blockEntity.isVirtual())
						world.setBlock(currentPos, emptied, 2 | 16);
					dequeue(queue);
					if (queue.isEmpty()) {
						isValid = checkValid(world, rootPos);
						reset(ctx);
					}
					continue;
				}
			} else if (blockState.getFluidState()
				.getType() != Fluids.EMPTY && blockState.getCollisionShape(world, currentPos, CollisionContext.empty())
				.isEmpty()) {
				fluid = blockState.getFluidState()
					.getType();
				emptied = Blocks.AIR.defaultBlockState();
			}

			if (this.fluid == null)
				this.fluid = fluid;

			if (!this.fluid.isSame(fluid)) {
				dequeue(queue);
				if (queue.isEmpty()) {
					isValid = checkValid(world, rootPos);
					reset(ctx);
				}
				continue;
			}

			Fluid finalFluid = fluid;
			TransactionSuccessCallback.register(ctx, () -> {
				playEffect(world, currentPos, finalFluid, true);
				blockEntity.award(AllAdvancements.HOSE_PULLEY);
				if (infinite && FluidHelper.isLava(finalFluid))
					blockEntity.award(AllAdvancements.HOSE_PULLEY_LAVA);
			});

			if (!blockEntity.isVirtual()) {
				world.updateSnapshots(ctx);
				world.setBlock(currentPos, emptied, 2 | 16);

				BlockState stateAbove = world.getBlockState(currentPos.above());
				if (stateAbove.getFluidState()
					.getType() == Fluids.EMPTY && !stateAbove.canSurvive(world, currentPos.above()))
					world.setBlock(currentPos.above(), Blocks.AIR.defaultBlockState(), 2 | 16);
			}
			affectedArea = BBHelper.encapsulate(affectedArea, currentPos);

			dequeue(queue);
			if (queue.isEmpty()) {
				isValid = checkValid(world, rootPos);
				reset(ctx);
			} else if (!validationSet.contains(currentPos)) {
				reset(ctx);
			}
			return true;
		}

		if (rootPos == null)
			return false;

		if (isValid)
			rebuildContext(root, ctx);

		return false;
	}

	protected void softReset(BlockPos root, TransactionContext ctx) {
		queue.clear();
		validationSet.clear();
		newValidationSet.clear();
		validationFrontier.clear();
		validationVisited.clear();
		visited.clear();
		infinite = false;
		setValidationTimer();
		frontier.add(new BlockPosEntry(root, 0));
		TransactionSuccessCallback.register(ctx, blockEntity::sendData);
	}

	protected boolean checkValid(Level world, BlockPos root) {
		BlockPos currentPos = root;
		for (int timeout = 1000; timeout > 0 && !root.equals(blockEntity.getBlockPos()); timeout--) {
			FluidBlockType canPullFluidsFrom = canPullFluidsFrom(world.getBlockState(currentPos), currentPos);
			if (canPullFluidsFrom == FluidBlockType.FLOWING) {
				for (Direction d : Iterate.directions) {
					BlockPos side = currentPos.relative(d);
					if (canPullFluidsFrom(world.getBlockState(side), side) == FluidBlockType.SOURCE)
						return true;
				}
				currentPos = currentPos.above();
				continue;
			}
			if (canPullFluidsFrom == FluidBlockType.SOURCE)
				return true;
			break;
		}
		return false;
	}

	enum FluidBlockType {
		NONE, SOURCE, FLOWING;
	}

	@Override
	public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(nbt, registries, clientPacket);
		if (!clientPacket && affectedArea != null)
			frontier.add(new BlockPosEntry(rootPos, 0));
	}

	protected FluidBlockType canPullFluidsFrom(BlockState blockState, BlockPos pos) {
		if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)
			&& blockState.getValue(BlockStateProperties.WATERLOGGED))
			return FluidBlockType.SOURCE;
		if (blockState.getBlock() instanceof LiquidBlock)
			return blockState.getValue(LiquidBlock.LEVEL) == 0 ? FluidBlockType.SOURCE : FluidBlockType.FLOWING;
		if (blockState.getFluidState()
			.getType() != Fluids.EMPTY && blockState.getCollisionShape(getWorld(), pos, CollisionContext.empty())
			.isEmpty())
			return FluidBlockType.SOURCE;
		return FluidBlockType.NONE;
	}

	@Override
	public void tick() {
		super.tick();
		if (rootPos != null)
			isValid = checkValid(getWorld(), rootPos);
		if (!frontier.isEmpty()) {
			continueSearch();
			return;
		}
		if (!validationFrontier.isEmpty()) {
			continueValidation();
			return;
		}
		if (revalidateIn > 0)
			revalidateIn--;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
	}

	public void rebuildContext(BlockPos root, TransactionContext ctx) {
		reset(ctx);
		rootPos = root;
		affectedArea = BoundingBox.fromCorners(rootPos, rootPos);
		if (isValid)
			frontier.add(new BlockPosEntry(root, 0));
	}

	public void revalidate(BlockPos root) {
		validationFrontier.clear();
		validationVisited.clear();
		newValidationSet.clear();
		validationFrontier.add(new BlockPosEntry(root, 0));
		setValidationTimer();
	}

	private void continueSearch() {
		try {
			fluid = search(fluid, frontier, visited, (e, d) -> {
				BlockPosEntry entry = new BlockPosEntry(e, d);
				queue.add(entry);
				validationSet.add(e);
			}, false);
		} catch (ChunkNotLoadedException e) {
			blockEntity.sendData();
			frontier.clear();
			visited.clear();
		}

		int maxBlocks = maxBlocks();
		if (visited.size() > maxBlocks && canDrainInfinitely(fluid) && !queue.isEmpty()) {
			infinite = true;
			BlockPos firstValid = queue.first()
				.pos();
			frontier.clear();
			visited.clear();
			queue.clear();
			queue.add(new BlockPosEntry(firstValid, 0));
			blockEntity.sendData();
			return;
		}

		if (!frontier.isEmpty())
			return;

		blockEntity.sendData();
		visited.clear();
	}

	private void continueValidation() {
		try {
			search(fluid, validationFrontier, validationVisited, (e, d) -> newValidationSet.add(e), false);
		} catch (ChunkNotLoadedException e) {
			validationFrontier.clear();
			validationVisited.clear();
			setLongValidationTimer();
			return;
		}

		int maxBlocks = maxBlocks();
		if (validationVisited.size() > maxBlocks && canDrainInfinitely(fluid)) {
			if (!infinite)
				reset(null);
			validationFrontier.clear();
			setLongValidationTimer();
			return;
		}

		if (!validationFrontier.isEmpty())
			return;
		if (infinite) {
			reset(null);
			return;
		}

		validationSet = newValidationSet;
		newValidationSet = new HashSet<>();
		validationVisited.clear();
	}

	@Override
	public void reset(@Nullable TransactionContext ctx) {
		super.reset(ctx);

		fluid = null;
		rootPos = null;
		queue.clear();
		validationSet.clear();
		newValidationSet.clear();
		validationFrontier.clear();
		validationVisited.clear();
		if (ctx != null) TransactionSuccessCallback.register(ctx, blockEntity::sendData);
		else blockEntity.sendData();
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	protected boolean isSearching() {
		return !frontier.isEmpty();
	}

	public FluidStack getDrainableFluid(BlockPos rootPos) {
		try (Transaction t = Transaction.openOuter()) { // simulate pullNext
			if (fluid == null || isSearching() || !pullNext(rootPos, t)) {
				return FluidStack.EMPTY;
			}
		}
		return new FluidStack(fluid, FluidConstants.BUCKET);
	}
}
