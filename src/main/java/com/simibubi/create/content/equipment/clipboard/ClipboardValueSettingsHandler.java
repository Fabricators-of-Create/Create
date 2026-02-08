package com.simibubi.create.content.equipment.clipboard;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ClipboardValueSettingsHandler {

	@Environment(EnvType.CLIENT)
	public static boolean drawCustomBlockSelection(LevelRenderer context, Camera camera, HitResult hitResult,
		float partialTicks, PoseStack ms, MultiBufferSource buffers) {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public static void clientTick() {
	}

	public static InteractionResult rightClickToCopy(Player player, Level world, InteractionHand hand,
		net.minecraft.world.phys.BlockHitResult hitResult) {
		return InteractionResult.PASS;
	}

	public static InteractionResult leftClickToPaste(Player player, Level world, InteractionHand hand, BlockPos pos,
		Direction direction) {
		return InteractionResult.PASS;
	}
}
