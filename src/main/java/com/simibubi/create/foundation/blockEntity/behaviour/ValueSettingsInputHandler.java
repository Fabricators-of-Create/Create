package com.simibubi.create.foundation.blockEntity.behaviour;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.simibubi.create.foundation.utility.AdventureUtil;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.entity.FakePlayer;

import io.github.fabricators_of_create.porting_lib.common.util.EnvExecutor;

public class ValueSettingsInputHandler {

	public static InteractionResult onBlockActivated(Player player, Level world, InteractionHand hand, BlockHitResult ray) {
		BlockPos pos = ray.getBlockPos();

		if (!canInteract(player))
			return InteractionResult.PASS;
		if (AllBlocks.CLIPBOARD.isIn(player.getMainHandItem()))
			return InteractionResult.PASS;
		if (!(world.getBlockEntity(pos)instanceof SmartBlockEntity sbe))
			return InteractionResult.PASS;

		MutableBoolean cancelled = new MutableBoolean(false);
		if (world.isClientSide)
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> CreateClient.VALUE_SETTINGS_HANDLER.cancelIfWarmupAlreadyStarted(pos, cancelled));

		if (cancelled.booleanValue())
			return InteractionResult.FAIL;

		for (BlockEntityBehaviour behaviour : sbe.getAllBehaviours()) {
			if (!(behaviour instanceof ValueSettingsBehaviour valueSettingsBehaviour))
				continue;
			if (valueSettingsBehaviour.bypassesInput(player.getMainHandItem()))
				continue;
			if (!valueSettingsBehaviour.mayInteract(player))
				continue;

			if (ray == null)
				return InteractionResult.PASS;
			if (behaviour instanceof SidedFilteringBehaviour) {
				behaviour = ((SidedFilteringBehaviour) behaviour).get(ray.getDirection());
				if (behaviour == null)
					continue;
			}

			if (!valueSettingsBehaviour.isActive())
				continue;
			if (valueSettingsBehaviour.onlyVisibleWithWrench()
				&& !AllItemTags.WRENCH.matches(player.getItemInHand(hand)))
				continue;
			if (valueSettingsBehaviour.getSlotPositioning()instanceof ValueBoxTransform.Sided sidedSlot) {
				if (!sidedSlot.isSideActive(sbe.getBlockState(), ray.getDirection()))
					continue;
				sidedSlot.fromSide(ray.getDirection());
			}

			boolean fakePlayer = player instanceof FakePlayer;
			if (!valueSettingsBehaviour.testHit(ray.getLocation()) && !fakePlayer)
				continue;

			if (!valueSettingsBehaviour.acceptsValueSettings() || fakePlayer) {
				valueSettingsBehaviour.onShortInteract(player, hand, ray.getDirection(), ray);
				// fabric: https://github.com/Fabricators-of-Create/Create/issues/1553
				// Fabric api doesn't have a replacement for Forge's Event.Result so we need to hackily set this
				// to fail so that other code isn't run, this just simulates the same behavior as create forge since we
				// skip running further stuff if InteractionResult is FAIL
				return fakePlayer ? InteractionResult.FAIL : InteractionResult.SUCCESS;
			}

			if (world.isClientSide) {
				BehaviourType<?> type = behaviour.getType();
				CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> CreateClient.VALUE_SETTINGS_HANDLER
					.startInteractionWith(pos, type, hand, ray.getDirection()));
			}

			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public static boolean canInteract(Player player) {
		return player != null && !player.isSpectator() && !player.isShiftKeyDown() && !AdventureUtil.isAdventure(player);
	}
}
