package com.simibubi.create.infrastructure.gametest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovingInteraction;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity.SelectionMode;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.mixin.accessor.GameTestHelperAccessor;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;
import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;

/**
 * A helper class expanding the functionality of {@link GameTestHelper}.
 * This class may replace the default helper parameter if a test is registered through {@link CreateTestFunction}.
 */
public class CreateGameTestHelper extends GameTestHelper {
	public static final int TICKS_PER_SECOND = 20;
	public static final int TEN_SECONDS = 10 * TICKS_PER_SECOND;
	public static final int FIFTEEN_SECONDS = 15 * TICKS_PER_SECOND;
	public static final int TWENTY_SECONDS = 20 * TICKS_PER_SECOND;

	private CreateGameTestHelper(GameTestInfo testInfo) {
		super(testInfo);
	}

	public static CreateGameTestHelper of(GameTestHelper original) {
		GameTestHelperAccessor access = (GameTestHelperAccessor) original;
		CreateGameTestHelper helper = new CreateGameTestHelper(access.getTestInfo());
		//noinspection DataFlowIssue // accessor applied at runtime
		GameTestHelperAccessor newAccess = (GameTestHelperAccessor) helper;
		newAccess.setFinalCheckAdded(access.getFinalCheckAdded());
		return helper;
	}

	// blocks

	/**
	 * Flip the direction of any block with the {@link BlockStateProperties#FACING} property.
	 */
	public void flipBlock(BlockPos pos) {
		BlockState original = getBlockState(pos);
		if (!original.hasProperty(BlockStateProperties.FACING))
			fail("FACING property not in block: " + BuiltInRegistries.BLOCK.getId(original.getBlock()));
		Direction facing = original.getValue(BlockStateProperties.FACING);
		BlockState reversed = original.setValue(BlockStateProperties.FACING, facing.getOpposite());
		setBlock(pos, reversed);
	}

	public void assertNixiePower(BlockPos pos, int strength) {
		NixieTubeBlockEntity nixie = getBlockEntity(AllBlockEntityTypes.NIXIE_TUBE.get(), pos);
		int actualStrength = nixie.getRedstoneStrength();
		if (actualStrength != strength)
			fail("Expected nixie tube at %s to have power of %s, got %s".formatted(pos, strength, actualStrength));
	}

	/**
	 * Turn off a lever.
	 */
	public void powerLever(BlockPos pos) {
		assertBlockPresent(Blocks.LEVER, pos);
		if (!getBlockState(pos).getValue(LeverBlock.POWERED)) {
			pullLever(pos);
		}
	}

	/**
	 * Turn on a lever.
	 */
	public void unpowerLever(BlockPos pos) {
		assertBlockPresent(Blocks.LEVER, pos);
		if (getBlockState(pos).getValue(LeverBlock.POWERED)) {
			pullLever(pos);
		}
	}

	/**
	 * Set the {@link SelectionMode} of a belt tunnel at the given position.
	 * @param pos
	 * @param mode
	 */
	public void setTunnelMode(BlockPos pos, SelectionMode mode) {
		ScrollValueBehaviour behavior = getBehavior(pos, ScrollOptionBehaviour.TYPE);
		behavior.setValue(mode.ordinal());
	}

	public void assertSpeedometerSpeed(BlockPos speedometer, float value) {
		SpeedGaugeBlockEntity be = getBlockEntity(AllBlockEntityTypes.SPEEDOMETER.get(), speedometer);
		assertInRange(be.getSpeed(), value - 0.01, value + 0.01);
	}

	public void assertStressometerCapacity(BlockPos stressometer, float value) {
		StressGaugeBlockEntity be = getBlockEntity(AllBlockEntityTypes.STRESSOMETER.get(), stressometer);
		assertInRange(be.getNetworkCapacity(), value - 0.01, value + 0.01);
	}

	public void toggleActorsOfType(Contraption contraption, ItemLike item) {
		AtomicBoolean toggled = new AtomicBoolean(false);
		contraption.getInteractors().forEach((localPos, behavior) -> {
			if (toggled.get() || !(behavior instanceof ContraptionControlsMovingInteraction controls))
				return;
			MutablePair<StructureBlockInfo, MovementContext> actor = contraption.getActorAt(localPos);
			if (actor == null)
				return;
			ItemStack filter = ContraptionControlsMovement.getFilter(actor.right);
			if (filter != null && filter.is(item.asItem())) {
				controls.handlePlayerInteraction(
						makeMockPlayer(GameType.CREATIVE), InteractionHand.MAIN_HAND, localPos, contraption.entity
				);
				toggled.set(true);
			}
		});
	}

	// block entities

	/**
	 * Get the block entity of the expected type. If the type does not match, this fails the test.
	 */
	public <T extends BlockEntity> T getBlockEntity(BlockEntityType<T> type, BlockPos pos) {
		BlockEntity be = getBlockEntity(pos);
		BlockEntityType<?> actualType = be == null ? null : be.getType();
		if (actualType != type) {
			String actualId = actualType == null ? "null" : RegisteredObjectsHelper.getKeyOrThrow(actualType).toString();
			String error = "Expected block entity at pos [%s] with type [%s], got [%s]".formatted(
					pos, RegisteredObjectsHelper.getKeyOrThrow(type), actualId
			);
			fail(error);
		}
		return (T) be;
	}

	/**
	 * Given any segment of an {@link IMultiBlockEntityContainer}, get the controller for it.
	 */
	public <T extends BlockEntity & IMultiBlockEntityContainer> T getControllerBlockEntity(BlockEntityType<T> type, BlockPos anySegment) {
		T be = getBlockEntity(type, anySegment).getControllerBE();
		if (be == null)
			fail("Could not get block entity controller with type [%s] from pos [%s]".formatted(RegisteredObjectsHelper.getKeyOrThrow(type), anySegment));
		return be;
	}

	/**
	 * Get the expected {@link BlockEntityBehaviour} from the given position, failing if not present.
	 */
	public <T extends BlockEntityBehaviour> T getBehavior(BlockPos pos, BehaviourType<T> type) {
		T behavior = BlockEntityBehaviour.get(getLevel(), absolutePos(pos), type);
		if (behavior == null)
			fail("Behavior at " + pos + " missing, expected " + type.getName());
		return behavior;
	}

	// entities

	/**
	 * Spawn an item entity at the given position with no velocity.
	 */
	public ItemEntity spawnItem(BlockPos pos, ItemStack stack) {
		Vec3 spawn = Vec3.atCenterOf(absolutePos(pos));
		ServerLevel level = getLevel();
		ItemEntity item = new ItemEntity(level, spawn.x, spawn.y, spawn.z, stack, 0, 0, 0);
		level.addFreshEntity(item);
		return item;
	}

	public <T extends Entity> T spawnSeated(EntityType<T> type, BlockPos pos) {
		T entity = this.spawn(type, pos);
		SeatBlock.sitDown(this.getLevel(), this.absolutePos(pos), entity);
		return entity;
	}

	/**
	 * Spawn item entities given an item and amount. The amount will be split into multiple entities if
	 * larger than the item's max stack size.
	 */
	public void spawnItems(BlockPos pos, Item item, int amount) {
		while (amount > 0) {
			int toSpawn = Math.min(amount, new ItemStack(item).getMaxStackSize());
			amount -= toSpawn;
			ItemStack stack = new ItemStack(item, toSpawn);
			spawnItem(pos, stack);
		}
	}

	/**
	 * Get the first entity found at the given position.
	 */
	public <T extends Entity> T getFirstEntity(EntityType<T> type, BlockPos pos) {
		List<T> list = getEntitiesBetween(type, pos.north().east().above(), pos.south().west().below());
		if (list.isEmpty())
			fail("No entities at pos: " + pos);
		return list.get(0);
	}

	/**
	 * Get a list of all entities between two positions, inclusive.
	 */
	public <T extends Entity> List<T> getEntitiesBetween(EntityType<T> type, BlockPos pos1, BlockPos pos2) {
		BoundingBox box = BoundingBox.fromCorners(absolutePos(pos1), absolutePos(pos2));
		List<? extends T> entities = getLevel().getEntities(type, e -> box.isInside(e.blockPosition()));
		return (List<T>) entities;
	}


	// transfer - fluids

	public Storage<FluidVariant> fluidStorageAt(BlockPos pos) {
		Storage<FluidVariant> storage = TransferUtil.getFluidStorage(getLevel(), absolutePos(pos));
		if (storage == null)
			fail("storage not present");
		return storage;
	}

	/**
	 * Get the content of the tank at the pos.
	 * content is determined by what the tank allows to be extracted.
	 */
	public FluidStack getTankContents(BlockPos tank) {
		Storage<FluidVariant> storage = fluidStorageAt(tank);
		return TransferUtil.firstOrEmpty(storage);
	}

	/**
	 * Get the total capacity of a tank at the given position.
	 */
	public long getTankCapacity(BlockPos pos) {
		Storage<FluidVariant> storage = fluidStorageAt(pos);
		return TransferUtil.totalCapacity(storage);
	}

	/**
	 * Get the total fluid amount across all fluid tanks at the given positions.
	 */
	public long getFluidInTanks(BlockPos... tanks) {
		long total = 0;
		for (BlockPos tank : tanks) {
			total += getTankContents(tank).getAmount();
		}
		return total;
	}

	/**
	 * Assert that the given fluid stack is present in the given tank. The tank might also hold more than the fluid.
	 */
	public void assertFluidPresent(FluidStack fluid, BlockPos pos) {
		FluidStack contained = getTankContents(pos);
		if (!FluidStack.isSameFluidSameComponents(fluid, contained))
			fail("Different fluids");
		if (fluid.getAmount() != contained.getAmount())
			fail("Different amounts");
	}

	/**
	 * Assert that the given tank holds no fluid.
	 */
	public void assertTankEmpty(BlockPos pos) {
		assertFluidPresent(FluidStack.EMPTY, pos);
	}

	public void assertTanksEmpty(BlockPos... tanks) {
		for (BlockPos tank : tanks) {
			assertTankEmpty(tank);
		}
	}

	// transfer - items

	public Storage<ItemVariant> itemStorageAt(BlockPos pos) {
		Storage<ItemVariant> storage = TransferUtil.getItemStorage(getLevel(), absolutePos(pos));
		if (storage == null)
			fail("storage not present");
		return storage;
	}

	/**
	 * Get a map of contained items to their amounts. This is not safe for NBT!
	 */
	public Object2LongMap<Item> getItemContent(BlockPos pos) {
		Storage<ItemVariant> storage = itemStorageAt(pos);
		Object2LongMap<Item> map = new Object2LongArrayMap<>();
		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
				ItemVariant resource = view.getResource();
				Item item = resource.getItem();
				long amount = map.getLong(item);
				amount += view.extract(resource, Long.MAX_VALUE, t);
				map.put(item, amount);
			}
		}
		return map;
	}

	/**
	 * Get the combined total of all ItemStacks inside the inventory.
	 */
	public long getTotalItems(BlockPos pos) {
		Storage<ItemVariant> storage = itemStorageAt(pos);
		try (Transaction t = Transaction.openOuter()) {
			return TransferUtil.extractAllAsStacks(storage).stream().mapToLong(ItemStack::getCount).sum();
		}
	}

	/**
	 * Of the provided items, assert that at least one is present in the given inventory.
	 */
	public void assertAnyContained(BlockPos pos, Item... items) {
		Storage<ItemVariant> storage = itemStorageAt(pos);
		boolean noneFound = true;
		try (Transaction t = Transaction.openOuter()) {
			for (Item item : items) {
				if (storage.extract(ItemVariant.of(item), 1, t) > 0) {
					noneFound = false;
					break;
				}
			}
		}
		if (noneFound)
			fail("No matching items " + Arrays.toString(items) + " found in handler at pos: " + pos);
	}

	/**
	 * Assert that the inventory contains all the provided content.
	 */
	public void assertContentPresent(Object2LongMap<Item> content, BlockPos pos) {
		Storage<ItemVariant> storage = itemStorageAt(pos);
		Object2LongMap<Item> missing = new Object2LongArrayMap<>();
		try (Transaction t = Transaction.openOuter()) {
			for (Entry<Item> entry : content.object2LongEntrySet()) {
				Item item = entry.getKey();
				long amount = entry.getLongValue();
				long extracted = storage.extract(ItemVariant.of(item), amount, t);
				long remaining = amount - extracted;
				if (remaining > 0) {
					missing.put(item, remaining);
				}
			}
		}
		if (!missing.isEmpty())
			fail("Storage missing content: " + missing + ", expected: " + content);
	}

	/**
	 * Assert that all the given inventories hold no items.
	 */
	public void assertContainersEmpty(List<BlockPos> positions) {
		for (BlockPos pos : positions) {
			assertContainerEmpty(pos);
		}
	}

	/**
	 * Assert that the given inventory holds no items.
	 */
	@Override
	public void assertContainerEmpty(@NotNull BlockPos pos) {
		Storage<ItemVariant> storage = itemStorageAt(pos);
		storage.nonEmptyViews().forEach(view -> fail("Storage not empty"));
	}

	/** @see CreateGameTestHelper#assertContainerContains(BlockPos, ItemStack) */
	public void assertContainerContains(BlockPos pos, ItemLike item) {
		assertContainerContains(pos, item.asItem());
	}

	/** @see CreateGameTestHelper#assertContainerContains(BlockPos, ItemStack) */
	@Override
	public void assertContainerContains(@NotNull BlockPos pos, @NotNull Item item) {
		assertContainerContains(pos, new ItemStack(item));
	}

	/**
	 * Assert that the inventory holds at least the given ItemStack. It may also hold more than the stack.
	 */
	public void assertContainerContains(BlockPos pos, ItemStack item) {
		Storage<ItemVariant> storage = itemStorageAt(pos);
		ItemStack extracted = ItemHelper.extract(storage, stack -> ItemStack.isSameItemSameComponents(stack, item), item.getCount(), true);
		if (extracted.isEmpty())
			fail("item not present: " + item);
	}

	// time

	/**
	 * Fail unless the desired number seconds have passed since test start.
	 */
	public void assertSecondsPassed(int seconds) {
		if (getTick() < (long) seconds * TICKS_PER_SECOND)
			fail("Waiting for %s seconds to pass".formatted(seconds));
	}

	/**
	 * Get the total number of seconds that have passed since test start.
	 */
	public long secondsPassed() {
		return getTick() % 20;
	}

	/**
	 * Run an action later, once enough time has passed.
	 */
	public void whenSecondsPassed(int seconds, Runnable run) {
		runAfterDelay((long) seconds * TICKS_PER_SECOND, run);
	}

	// numbers

	/**
	 * Assert that a number is less than 1 away from its expected value
	 */
	public void assertCloseEnoughTo(double value, double expected) {
		assertInRange(value, expected - 1, expected + 1);
	}

	public void assertInRange(double value, double min, double max) {
		if (value < min)
			fail("Value %s below expected min of %s".formatted(value, min));
		if (value > max)
			fail("Value %s greater than expected max of %s".formatted(value, max));
	}

	// misc

	@Contract("_->fail") // make IDEA happier
	@Override
	public void fail(@NotNull String exceptionMessage) {
		super.fail(exceptionMessage);
	}
}
