package com.simibubi.create.content.decoration.copycat;

import java.util.function.BiFunction;
import java.util.function.Function;

import io.github.fabricators_of_create.porting_lib.block.CustomFrictionBlock;

import io.github.fabricators_of_create.porting_lib.block.CustomLandingEffectsBlock;
import io.github.fabricators_of_create.porting_lib.block.CustomRunningEffectsBlock;
import io.github.fabricators_of_create.porting_lib.block.ExplosionResistanceBlock;
import io.github.fabricators_of_create.porting_lib.block.LightEmissiveBlock;

import io.github.fabricators_of_create.porting_lib.enchant.EnchantmentBonusBlock;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.block.BlockPickInteractionAware;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.AdventureUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.BlockPickInteractionAware;

import io.github.fabricators_of_create.porting_lib.block.CustomFrictionBlock;
import io.github.fabricators_of_create.porting_lib.block.CustomLandingEffectsBlock;
import io.github.fabricators_of_create.porting_lib.block.CustomRunningEffectsBlock;
import io.github.fabricators_of_create.porting_lib.block.CustomSoundTypeBlock;
import io.github.fabricators_of_create.porting_lib.block.ExplosionResistanceBlock;
import io.github.fabricators_of_create.porting_lib.block.LightEmissiveBlock;
import io.github.fabricators_of_create.porting_lib.block.ValidSpawnBlock;
import io.github.fabricators_of_create.porting_lib.enchant.EnchantmentBonusBlock;

public abstract class CopycatBlock extends Block implements IBE<CopycatBlockEntity>, IWrenchable,
		CustomFrictionBlock, CustomSoundTypeBlock, LightEmissiveBlock, ExplosionResistanceBlock,
		BlockPickInteractionAware, CustomLandingEffectsBlock, CustomRunningEffectsBlock, EnchantmentBonusBlock,
		ValidSpawnBlock {

	public CopycatBlock(Properties pProperties) {
		super(pProperties);
	}

	@Nullable
	@Override
	public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level p_153212_, BlockState p_153213_,
																  BlockEntityType<S> p_153214_) {
		return null;
	}

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		onWrenched(state, context);
		return IWrenchable.super.onSneakWrenched(state, context);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return onBlockEntityUse(context.getLevel(), context.getClickedPos(), ufte -> {
			ItemStack consumedItem = ufte.getConsumedItem();
			if (!ufte.hasCustomMaterial())
				return InteractionResult.PASS;
			Player player = context.getPlayer();
			if (!player.isCreative())
				player.getInventory()
					.placeItemBackInInventory(consumedItem);
			context.getLevel()
				.levelEvent(2001, context.getClickedPos(), Block.getId(ufte.getBlockState()));
			ufte.setMaterial(AllBlocks.COPYCAT_BASE.getDefaultState());
			ufte.setConsumedItem(ItemStack.EMPTY);
			return InteractionResult.SUCCESS;
		});
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
								 if (player == null || AdventureUtil.isAdventure(player))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		Direction face = hitResult.getDirection();
		BlockState materialIn = getAcceptedBlockState(level, pos, stack, face);

		if (materialIn != null)
			materialIn = prepareMaterial(level, pos, state, player, hand, hitResult, materialIn);
		if (materialIn == null)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		BlockState material = materialIn;
		return onBlockEntityUseItemOn(level, pos, ufte -> {
			if (ufte.getMaterial()
				.is(material.getBlock())) {
				if (!ufte.cycleMaterial())
					return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
				ufte.getLevel()
					.playSound(null, ufte.getBlockPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .75f,
						.95f);
				return ItemInteractionResult.SUCCESS;
			}
			if (ufte.hasCustomMaterial())
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			if (level.isClientSide())
				return ItemInteractionResult.SUCCESS;

			ufte.setMaterial(material);
			ufte.setConsumedItem(stack);
			ufte.getLevel()
				.playSound(null, ufte.getBlockPos(), material.getSoundType()
					.getPlaceSound(), SoundSource.BLOCKS, 1, .75f);

			if (player.isCreative())
				return ItemInteractionResult.SUCCESS;

			stack.shrink(1);
			if (stack.isEmpty())
				player.setItemInHand(hand, ItemStack.EMPTY);
			return ItemInteractionResult.SUCCESS;
		});
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		if (pPlacer == null)
			return;
		ItemStack offhandItem = pPlacer.getItemInHand(InteractionHand.OFF_HAND);
		BlockState appliedState =
			getAcceptedBlockState(pLevel, pPos, offhandItem, Direction.orderedByNearest(pPlacer)[0]);

		if (appliedState == null)
			return;
		withBlockEntityDo(pLevel, pPos, ufte -> {
			if (ufte.hasCustomMaterial())
				return;

			ufte.setMaterial(appliedState);
			ufte.setConsumedItem(offhandItem);

			if (pPlacer instanceof Player player && player.isCreative())
				return;
			offhandItem.shrink(1);
			if (offhandItem.isEmpty())
				pPlacer.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		});
	}

	@Nullable
	public BlockState getAcceptedBlockState(Level pLevel, BlockPos pPos, ItemStack item, Direction face) {
		if (!(item.getItem() instanceof BlockItem bi))
			return null;

		Block block = bi.getBlock();
		if (block instanceof CopycatBlock)
			return null;

		BlockState appliedState = block.defaultBlockState();
		boolean hardCodedAllow = isAcceptedRegardless(appliedState);

		if (!AllBlockTags.COPYCAT_ALLOW.matches(block) && !hardCodedAllow) {

			if (AllBlockTags.COPYCAT_DENY.matches(block))
				return null;
			if (block instanceof EntityBlock)
				return null;
			if (block instanceof StairBlock)
				return null;

			if (pLevel != null) {
				VoxelShape shape = appliedState.getShape(pLevel, pPos);
				if (shape.isEmpty() || !shape.bounds()
					.equals(Shapes.block()
						.bounds()))
					return null;

				VoxelShape collisionShape = appliedState.getCollisionShape(pLevel, pPos);
				if (collisionShape.isEmpty())
					return null;
			}
		}

		if (face != null) {
			Axis axis = face.getAxis();

			if (appliedState.hasProperty(BlockStateProperties.FACING))
				appliedState = appliedState.setValue(BlockStateProperties.FACING, face);
			if (appliedState.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && axis != Axis.Y)
				appliedState = appliedState.setValue(BlockStateProperties.HORIZONTAL_FACING, face);
			if (appliedState.hasProperty(BlockStateProperties.AXIS))
				appliedState = appliedState.setValue(BlockStateProperties.AXIS, axis);
			if (appliedState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS) && axis != Axis.Y)
				appliedState = appliedState.setValue(BlockStateProperties.HORIZONTAL_AXIS, axis);
		}

		return appliedState;
	}

	public boolean isAcceptedRegardless(BlockState material) {
		return false;
	}

	public BlockState prepareMaterial(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer,
									  InteractionHand pHand, BlockHitResult pHit, BlockState material) {
		return material;
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		if (!pState.hasBlockEntity() || pState.getBlock() == pNewState.getBlock())
			return;
		if (!pIsMoving)
			withBlockEntityDo(pLevel, pPos, ufte -> Block.popResource(pLevel, pPos, ufte.getConsumedItem()));
		pLevel.removeBlockEntity(pPos);
	}

	@Override
	public BlockState playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
		super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
		if (pPlayer.isCreative())
			withBlockEntityDo(pLevel, pPos, ufte -> ufte.setConsumedItem(ItemStack.EMPTY));
		return pState;
	}

	@Override
	public Class<CopycatBlockEntity> getBlockEntityClass() {
		return CopycatBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends CopycatBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.COPYCAT.get();
	}

	// Connected Textures

	@Override
	@Environment(EnvType.CLIENT)
	public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
									@Nullable BlockState queryState, @Nullable BlockPos queryPos) {

		if (isIgnoredConnectivitySide(level, state, side, pos, queryPos))
			return state;

		return CopycatModel.getMaterial(getMaterial(level, pos));
	}

	public boolean isIgnoredConnectivitySide(BlockAndTintGetter reader, BlockState state, Direction face,
											 @Nullable BlockPos fromPos, @Nullable BlockPos toPos) {
		return false;
	}

	public abstract boolean canConnectTexturesToward(BlockAndTintGetter reader, BlockPos fromPos, BlockPos toPos,
													 BlockState state);

	//

	public static BlockState getMaterial(BlockGetter reader, BlockPos targetPos) {
		if (reader.getBlockEntity(targetPos) instanceof CopycatBlockEntity cbe)
			return cbe.getMaterial();
		return Blocks.AIR.defaultBlockState();
	}

	public boolean canFaceBeOccluded(BlockState state, Direction face) {
		return false;
	}

	public boolean shouldFaceAlwaysRender(BlockState state, Direction face) {
		return false;
	}

	// Wrapped properties

	@Override
	public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
		return getMaterial(level, pos).getSoundType();
	}

	@Override
	public float getFriction(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
		return maybeMaterialAs(
				level, pos, CustomFrictionBlock.class,
				(material, block) -> block.getFriction(material, level, pos, entity),
				material -> material.getBlock().getFriction()
		);
	}

	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		return maybeMaterialAs(
				level, pos, LightEmissiveBlock.class,
				(material, block) -> block.getLightEmission(material, level, pos),
				BlockStateBase::getLightEmission
		);
	}

	@Override
	public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
		return maybeMaterialAs(
				level, pos, ExplosionResistanceBlock.class,
				(material, block) -> block.getExplosionResistance(material, level, pos, explosion),
				material -> material.getBlock().getExplosionResistance()
		);
	}

	@Override
	public ItemStack getPickedStack(BlockState state, BlockGetter level, BlockPos pos, @Nullable Player player, @Nullable HitResult result) {
		BlockState material = getMaterial(level, pos);
		if (AllBlocks.COPYCAT_BASE.has(material) || player != null && player.isShiftKeyDown())
			return new ItemStack(this);
			return maybeMaterialAs(
					level, pos, BlockPickInteractionAware.class,
					(mat, block) -> block.getPickedStack(mat, level, pos, player, result),
					mat -> level instanceof LevelReader reader ? mat.getBlock().getCloneItemStack(reader, pos, mat) : new ItemStack(mat.getBlock())
			);
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2,
									 LivingEntity entity, int numberOfParticles) {
		return maybeMaterialAs(
				level, pos, CustomLandingEffectsBlock.class, // duplicate material is not a bug
				(material, block) -> block.addLandingEffects(material, level, pos, material, entity, numberOfParticles),
				material -> false // default to vanilla, true cancels
		);
	}

	@Override
	public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
		return maybeMaterialAs(
				level, pos, CustomRunningEffectsBlock.class,
				(material, block) -> block.addRunningEffects(material, level, pos, entity),
				material -> false // default to vanilla, true cancels
		);
	}

	@Override
	public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
		return maybeMaterialAs(
				level, pos, EnchantmentBonusBlock.class,
				(material, block) -> block.getEnchantPowerBonus(material, level, pos),
				material -> EnchantmentBonusBlock.super.getEnchantPowerBonus(material, level, pos)
		);
	}

	// fabric: unsupported
//	@Override
//	public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
//		return getMaterial(level, pos).canEntityDestroy(level, pos, entity);
//	}

	@Override
	public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float p_152430_) {
		BlockState material = getMaterial(pLevel, pPos);
		material.getBlock()
			.fallOn(pLevel, material, pPos, pEntity, p_152430_);
	}

	@Override
	public float getDestroyProgress(BlockState pState, Player pPlayer, BlockGetter pLevel, BlockPos pPos) {
		return getMaterial(pLevel, pPos).getDestroyProgress(pPlayer, pLevel, pPos);
	}

	//

	@Environment(EnvType.CLIENT)
	public static BlockColor wrappedColor() {
		return new WrappedBlockColor();
	}

	@Environment(EnvType.CLIENT)
	public static class WrappedBlockColor implements BlockColor {

		@Override
		public int getColor(BlockState pState, @Nullable BlockAndTintGetter pLevel, @Nullable BlockPos pPos,
							int pTintIndex) {
			if (pLevel == null || pPos == null)
				return GrassColor.get(0.5D, 1.0D);
			return Minecraft.getInstance()
				.getBlockColors()
				.getColor(getMaterial(pLevel, pPos), pLevel, pPos, pTintIndex);
		}

	}


	// fabric: util
	private static <T, R> R maybeMaterialAs(BlockGetter level, BlockPos pos, Class<T> clazz,
											BiFunction<BlockState, T, R> ifType, Function<BlockState, R> ifNot) {
		BlockState material = getMaterial(level, pos);
		Block block = material.getBlock();
		if (clazz.isInstance(block))
			return ifType.apply(material, clazz.cast(block));
		return ifNot.apply(material);
	}


}
