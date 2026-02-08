package com.simibubi.create.content.contraptions.glue;

import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.foundation.utility.AdventureUtil;
import com.simibubi.create.foundation.utility.fabric.ReachUtil;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.levelWrappers.RayTraceLevel;
import net.createmod.catnip.placement.IPlacementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class SuperGlueHandler {

	public static void glueListensForBlockPlacement(BlockPlaceContext context, BlockPos pos, BlockState state) {
		LevelAccessor world = context.getLevel();
		Player entity = context.getPlayer();

		if (entity == null || AdventureUtil.isAdventure(entity))
			return;
		if (world.isClientSide())
			return;

		Set<SuperGlueEntity> cached = new HashSet<>();
		for (Direction direction : Iterate.directions) {
			BlockPos relative = pos.relative(direction);
			if (SuperGlueEntity.isGlued(world, pos, direction, cached)
				&& BlockMovementChecks.isMovementNecessary(world.getBlockState(relative), entity.level(), relative))
				CatnipServices.NETWORK.sendToClientsTrackingAndSelf(entity, new GlueEffectPacket(pos, direction, true));
		}

		glueInOffHandAppliesOnBlockPlace(context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite())), pos, entity);
	}

	public static void glueInOffHandAppliesOnBlockPlace(BlockState placedAgainst, BlockPos pos, Player placer) {
		ItemStack itemstack = placer.getOffhandItem();
		AttributeInstance reachAttribute = placer.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
		if (!AllItems.SUPER_GLUE.isIn(itemstack) || reachAttribute == null)
			return;
		if (AllItems.WRENCH.isIn(placer.getMainHandItem()))
			return;
		if (placedAgainst == IPlacementHelper.ID)
			return;

		double distance = ReachUtil.reach(placer);
		Vec3 start = placer.getEyePosition(1);
		Vec3 look = placer.getViewVector(1);
		Vec3 end = start.add(look.x * distance, look.y * distance, look.z * distance);
		Level world = placer.level();

		RayTraceLevel rayTraceLevel =
			new RayTraceLevel(world, (p, state) -> p.equals(pos) ? Blocks.AIR.defaultBlockState() : state);
		BlockHitResult ray =
			rayTraceLevel.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, placer));

		Direction face = ray.getDirection();
		if (face == null || ray.getType() == Type.MISS)
			return;

		BlockPos gluePos = ray.getBlockPos();
		if (!gluePos.relative(face)
			.equals(pos)) {
			return;
		}

		if (SuperGlueEntity.isGlued(world, gluePos, face, null))
			return;

		SuperGlueEntity entity = new SuperGlueEntity(world, SuperGlueEntity.span(gluePos, gluePos.relative(face)));
		CustomData customData = itemstack.get(DataComponents.CUSTOM_DATA);
		if (customData != null)
			EntityType.updateCustomEntityTag(world, placer, entity, customData);

		if (SuperGlueEntity.isValidFace(world, gluePos, face)) {
			if (!world.isClientSide) {
				world.addFreshEntity(entity);
				CatnipServices.NETWORK.sendToClientsTrackingEntity(entity,
					new GlueEffectPacket(gluePos, face, true));
			}
			if (placer.level() instanceof ServerLevel serverLevel && placer instanceof ServerPlayer serverPlayer)
				itemstack.hurtAndBreak(1, serverLevel, serverPlayer, $ -> SuperGlueItem.onBroken(serverPlayer));
		}
	}

}
