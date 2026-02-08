package com.simibubi.create.foundation.utility;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.api.schematic.nbt.PartialSafeNBT;
import com.simibubi.create.api.schematic.nbt.SafeNbtWriterRegistry;
import com.simibubi.create.api.schematic.nbt.SafeNbtWriterRegistry.SafeNbtWriter;
import com.simibubi.create.api.schematic.state.SchematicStateFilter;
import com.simibubi.create.api.schematic.state.SchematicStateFilterRegistry;
import com.simibubi.create.api.schematic.state.SchematicStateFilterRegistry.StateFilter;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.framedblocks.FramedBlocksInSchematics;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.IMergeableBE;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;

import net.createmod.catnip.nbt.NBTProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

public class BlockHelper {
	private static final List<IntegerProperty> COUNT_STATES = List.of(
		BlockStateProperties.EGGS,
		BlockStateProperties.PICKLES,
		BlockStateProperties.CANDLES
	);

	private static final List<Block> VINELIKE_BLOCKS = List.of(
		Blocks.VINE, Blocks.GLOW_LICHEN
	);

	private static final List<BooleanProperty> VINELIKE_STATES = List.of(
		BlockStateProperties.UP,
		BlockStateProperties.NORTH,
		BlockStateProperties.EAST,
		BlockStateProperties.SOUTH,
		BlockStateProperties.WEST,
		BlockStateProperties.DOWN
	);

	public static BlockState setZeroAge(BlockState blockState) {
		if (blockState.hasProperty(BlockStateProperties.AGE_1))
			return blockState.setValue(BlockStateProperties.AGE_1, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_2))
			return blockState.setValue(BlockStateProperties.AGE_2, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_3))
			return blockState.setValue(BlockStateProperties.AGE_3, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_5))
			return blockState.setValue(BlockStateProperties.AGE_5, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_7))
			return blockState.setValue(BlockStateProperties.AGE_7, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_15))
			return blockState.setValue(BlockStateProperties.AGE_15, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_25))
			return blockState.setValue(BlockStateProperties.AGE_25, 0);
		if (blockState.hasProperty(BlockStateProperties.LEVEL_HONEY))
			return blockState.setValue(BlockStateProperties.LEVEL_HONEY, 0);
		if (blockState.hasProperty(BlockStateProperties.HATCH))
			return blockState.setValue(BlockStateProperties.HATCH, 0);
		if (blockState.hasProperty(BlockStateProperties.STAGE))
			return blockState.setValue(BlockStateProperties.STAGE, 0);
		if (blockState.is(BlockTags.CAULDRONS))
			return Blocks.CAULDRON
				.defaultBlockState();
		if (blockState.hasProperty(BlockStateProperties.LEVEL_COMPOSTER))
			return blockState.setValue(BlockStateProperties.LEVEL_COMPOSTER, 0);
		if (blockState.hasProperty(BlockStateProperties.EXTENDED))
			return blockState.setValue(BlockStateProperties.EXTENDED, false);
		return blockState;
	}

	public static int simulateFindAndRemoveInInventory(BlockState block, Player player, long amount) {
		try (Transaction t = Transaction.openOuter()) {
			return findAndRemoveInInventory(block, player, amount);
		}
	}

	public static int findAndRemoveInInventory(BlockState block, Player player, long amount) {
		ItemVariant required = ItemVariant.of(getRequiredItem(block));

		boolean needsTwo = block.hasProperty(BlockStateProperties.SLAB_TYPE)
			&& block.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE;

		if (needsTwo)
			amount *= 2;

		for (IntegerProperty property : COUNT_STATES)
			if (block.hasProperty(property))
				amount *= block.getValue(property);

		if (VINELIKE_BLOCKS.contains(block.getBlock())) {
			int vineCount = 0;

			for (BooleanProperty vineState : VINELIKE_STATES) {
				if (block.hasProperty(vineState) && block.getValue(vineState)) {
					vineCount++;
				}
			}

			amount += vineCount - 1;
		}

		try (Transaction t = Transaction.openOuter()) {
			PlayerInventoryStorage storage = PlayerInventoryStorage.of(player);
			int amountFound = (int) storage.extract(required, amount, t);

			if (needsTwo) {
				// Give back 1 if uneven amount was removed
				if (amountFound % 2 != 0)
					storage.insert(required, 1, t);
				amountFound /= 2;
			}

			t.commit();
			return amountFound;
		}
	}

	public static ItemStack getRequiredItem(BlockState state) {
		ItemStack itemStack = new ItemStack(state.getBlock());
		Item item = itemStack.getItem();
		if (item == Items.FARMLAND || item == Items.DIRT_PATH)
			itemStack = new ItemStack(Items.DIRT);
		return itemStack;
	}

	public static void destroyBlock(Level world, BlockPos pos, float effectChance) {
		destroyBlock(world, pos, effectChance, stack -> Block.popResource(world, pos, stack));
	}

	public static void destroyBlock(Level world, BlockPos pos, float effectChance,
									Consumer<ItemStack> droppedItemCallback) {
		destroyBlockAs(world, pos, null, ItemStack.EMPTY, effectChance, droppedItemCallback);
	}

	public static void destroyBlockAs(Level world, BlockPos pos, @Nullable Player player, ItemStack usedTool,
									  float effectChance, Consumer<ItemStack> droppedItemCallback) {
		FluidState fluidState = world.getFluidState(pos);
		BlockState state = world.getBlockState(pos);

		if (world.random.nextFloat() < effectChance)
			world.levelEvent(2001, pos, Block.getId(state));
		BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;

		if (player != null) {
			boolean allowed = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, player, pos, state, blockEntity);
			if (!allowed) {
				PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(world, player, pos, state, blockEntity);
				return;
			}

			usedTool.mineBlock(world, state, pos, player);
			player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
		}

		if (world instanceof ServerLevel serverLevel && world.getGameRules()
			.getBoolean(GameRules.RULE_DOBLOCKDROPS)
			&& (player == null || !player.isCreative())) {
			List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, usedTool);
			if (player != null) {
				BlockDropsEvent event = new BlockDropsEvent(serverLevel, pos, state, blockEntity, List.of(), player, usedTool);
					NeoForge.EVENT_BUS.post(event);
					if (!event.isCanceled()) {
						if ( event.getDroppedExperience() > 0)
							ExperienceOrb.award(serverLevel, Vec3.atCenterOf(pos), event.getDroppedExperience());
					}
				}
			for (ItemStack itemStack : drops) {
				if (itemStack.isEmpty())
					continue;
				droppedItemCallback.accept(itemStack);
			}

			// Simulating IceBlock#playerDestroy. Not calling method directly as it would drop item
			// entities as a side-effect
				Registry<Enchantment> enchantmentRegistry = world.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
				if (state.getBlock() instanceof IceBlock
					&& EnchantmentHelper.getItemEnchantmentLevel(enchantmentRegistry.getHolderOrThrow(Enchantments.SILK_TOUCH), usedTool) == 0) {
				if (world.dimensionType()
					.ultraWarm())
					return;

				BlockState blockstate = world.getBlockState(pos.below());
				if (blockstate.blocksMotion() || blockstate.liquid()) {
					world.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
					afterBreak(world, player, pos, state, blockEntity);
				}
				return;
			}

			state.spawnAfterBreak((ServerLevel) world, pos, ItemStack.EMPTY, true);
		}

		world.setBlockAndUpdate(pos, fluidState.createLegacyBlock());
		afterBreak(world, player, pos, state, blockEntity);
	}

	// fabric: after break event
	private static void afterBreak(Level level, @Nullable Player player, BlockPos pos, BlockState state, @Nullable BlockEntity be) {
		if (player != null)
			PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(level, player, pos, state, be);
	}

	public static boolean isSolidWall(BlockGetter reader, BlockPos fromPos, Direction toDirection) {
		return hasBlockSolidSide(reader.getBlockState(fromPos.relative(toDirection)), reader,
			fromPos.relative(toDirection), toDirection.getOpposite());
	}

	public static boolean noCollisionInSpace(BlockGetter reader, BlockPos pos) {
		return reader.getBlockState(pos)
			.getCollisionShape(reader, pos)
			.isEmpty();
	}

	private static void placeRailWithoutUpdate(Level world, BlockState state, BlockPos target) {
		LevelChunk chunk = world.getChunkAt(target);
		int idx = chunk.getSectionIndex(target.getY());
		LevelChunkSection chunksection = chunk.getSection(idx);
		if (chunksection == null) {
			chunksection = new LevelChunkSection(world.registryAccess()
				.registryOrThrow(Registries.BIOME));
			chunk.getSections()[idx] = chunksection;
		}
		BlockState old = chunksection.setBlockState(SectionPos.sectionRelative(target.getX()),
			SectionPos.sectionRelative(target.getY()), SectionPos.sectionRelative(target.getZ()), state);
		chunk.setUnsaved(true);
		world.markAndNotifyBlock(target, chunk, old, state, 82, 512);

		world.setBlock(target, state, 82);
		world.neighborChanged(target, world.getBlockState(target.below())
			.getBlock(), target.below());
	}

	public static CompoundTag prepareBlockEntityData(BlockState blockState, BlockEntity blockEntity) {
		CompoundTag data = null;
		if (blockEntity == null)
			return null;
		RegistryAccess access = blockEntity.getLevel().registryAccess();
		SafeNbtWriter writer = SafeNbtWriterRegistry.REGISTRY.get(blockEntity.getType());
		if (AllBlockTags.SAFE_NBT.matches(blockState)) {
			data = blockEntity.saveWithFullMetadata(access);
		} else if (writer != null) {
			data = new CompoundTag();
			writer.writeSafe(blockEntity, data, access);
		} else if (blockEntity instanceof PartialSafeNBT safeNbtBE) {
			data = new CompoundTag();
safeNbtBE.writeSafe(data, access);
		} else if (Mods.FRAMEDBLOCKS.contains(blockState.getBlock())) {
			data = FramedBlocksInSchematics.prepareBlockEntityData(blockState, blockEntity);
}
		return NBTProcessors.process(blockState, blockEntity, data, true);
	}

	public static void placeSchematicBlock(Level world, BlockState state, BlockPos target, ItemStack stack,
										   @Nullable CompoundTag data) {
		Block block = state.getBlock();
		BlockEntity existingBlockEntity = world.getBlockEntity(target);
		boolean alreadyPlaced = false;

		StateFilter filter = SchematicStateFilterRegistry.REGISTRY.get(state);
		if (filter != null) {
			state = filter.filterStates(existingBlockEntity, state);
		} else if (block instanceof SchematicStateFilter schematicStateFilter) {
			state = schematicStateFilter.filterStates(existingBlockEntity, state);
		}

		// Piston
		if (state.hasProperty(BlockStateProperties.EXTENDED))
			state = state.setValue(BlockStateProperties.EXTENDED, Boolean.FALSE);
		if (state.hasProperty(BlockStateProperties.WATERLOGGED))
			state = state.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE);

		if (block == Blocks.COMPOSTER) {
			state = Blocks.COMPOSTER.defaultBlockState();
		} else if (block != Blocks.SEA_PICKLE && block instanceof SpecialPlantable specialPlantable) {
			alreadyPlaced = true;
			if (specialPlantable.canPlacePlantAtPosition(stack, world, target, null))
				specialPlantable.spawnPlantAtPosition(stack, world, target, null);
		} else if (state.is(BlockTags.CAULDRONS)) {
			state = Blocks.CAULDRON.defaultBlockState();
		}

		if (world.dimensionType()
			.ultraWarm() && state.getFluidState().is(FluidTags.WATER)) {
			int i = target.getX();
			int j = target.getY();
			int k = target.getZ();
			world.playSound(null, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
				2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

			for (int l = 0; l < 8; ++l) {
				world.addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(),
					0.0D, 0.0D, 0.0D);
			}
			Block.dropResources(state, world, target);
			return;
		}

		//noinspection StatementWithEmptyBody
		if (alreadyPlaced) {
			// pass
		} else if (state.getBlock() instanceof BaseRailBlock) {
			placeRailWithoutUpdate(world, state, target);
		} else if (AllBlocks.BELT.has(state)) {
			world.setBlock(target, state, 2);
		} else {
			world.setBlock(target, state, 18);
		}

		if (data != null) {
			if (existingBlockEntity instanceof IMergeableBE mergeable) {
				BlockEntity loaded = BlockEntity.loadStatic(target, state, data, world.registryAccess());
				if (loaded != null) {
					if (existingBlockEntity.getType()
						.equals(loaded.getType())) {
						mergeable.accept(loaded);
						return;
					}
				}
			}
			BlockEntity blockEntity = world.getBlockEntity(target);
			if (blockEntity != null) {
				data.putInt("x", target.getX());
				data.putInt("y", target.getY());
				data.putInt("z", target.getZ());
				if (blockEntity instanceof KineticBlockEntity kbe)
					kbe.warnOfMovement();
				if (blockEntity instanceof IMultiBlockEntityContainer imbe)
					if (!imbe.isController())
						data.put("Controller", NbtUtils.writeBlockPos(imbe.getController()));
				blockEntity.loadWithComponents(data, world.registryAccess());
			}
		}

		try {
			state.getBlock()
				.setPlacedBy(world, target, state, null, stack);
		} catch (Exception ignored) {
		}
	}

	public static double getBounceMultiplier(Block block) {
		if (block instanceof SlimeBlock)
			return 0.8D;
		if (block instanceof BedBlock)
			return 0.66 * 0.8D;
		return 0;
	}

	public static boolean hasBlockSolidSide(BlockState p_220056_0_, BlockGetter p_220056_1_, BlockPos p_220056_2_,
											Direction p_220056_3_) {
		return !p_220056_0_.is(BlockTags.LEAVES)
			&& Block.isFaceFull(p_220056_0_.getCollisionShape(p_220056_1_, p_220056_2_), p_220056_3_);
	}

	public static boolean extinguishFire(Level world, @Nullable Player p_175719_1_, BlockPos p_175719_2_,
										 Direction p_175719_3_) {
		p_175719_2_ = p_175719_2_.relative(p_175719_3_);
		if (world.getBlockState(p_175719_2_)
			.getBlock() == Blocks.FIRE) {
			world.levelEvent(p_175719_1_, 1009, p_175719_2_, 0);
			world.removeBlock(p_175719_2_, false);
			return true;
		} else {
			return false;
		}
	}

	public static BlockState copyProperties(BlockState fromState, BlockState toState) {
		for (Property<?> property : fromState.getProperties()) {
			toState = copyProperty(property, fromState, toState);
		}
		return toState;
	}

	public static <T extends Comparable<T>> BlockState copyProperty(Property<T> property, BlockState fromState,
																	BlockState toState) {
		if (fromState.hasProperty(property) && toState.hasProperty(property)) {
			return toState.setValue(property, fromState.getValue(property));
		}
		return toState;
	}

	public static boolean isNotUnheated(BlockState state) {
		if (state.is(BlockTags.CAMPFIRES) && state.hasProperty(CampfireBlock.LIT)) {
			return state.getValue(CampfireBlock.LIT);
		}
		if (state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
			return state.getValue(BlazeBurnerBlock.HEAT_LEVEL) != HeatLevel.NONE;
		}
		return true;
	}

	public static InteractionResult invokeUse(BlockState state, Level level, Player player,
											   InteractionHand hand, BlockHitResult ray) {
		ItemInteractionResult iteminteractionresult = state.useItemOn(
				player.getItemInHand(hand), level, player, hand, ray
		);
		if (iteminteractionresult.consumesAction()) {
			return iteminteractionresult.result();
		}

		if (iteminteractionresult == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && hand == InteractionHand.MAIN_HAND) {
			InteractionResult interactionresult = state.useWithoutItem(level, player, ray);
			if (interactionresult.consumesAction()) {
				return interactionresult;
			}
		}

		return InteractionResult.PASS;
	}

	public static void runWithBreakEvents(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be, Player player, Runnable runnable) {
		boolean shouldBreak = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, state, be);
		if (shouldBreak) {
			runnable.run();
			PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(level, player, pos, state, be);
		} else {
			PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(level, player, pos, state, be);
		}
	}
}
