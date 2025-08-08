package com.simibubi.create.content.equipment.armor;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.FluidState;

public class RemainingAirOverlay {
	public static void render(GuiGraphics graphics, int width, int height) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;

		LocalPlayer player = mc.player;
		if (player == null)
			return;
		if (player.isCreative())
			return;
		if (!player.getCustomData()
			.contains("VisualBacktankAir"))
			return;

		//Code from Entity#updateFluidOnEyes
		double breatheY = player.getEyeY() - 0.11111111F;
		BlockPos blockPos = BlockPos.containing(player.getX(), breatheY, player.getZ());
		FluidState fluidState = player.level().getFluidState(blockPos);
		double fluidMaxY = blockPos.getY() + fluidState.getHeight(player.level(), blockPos);

		boolean canDrown = false;
		if(fluidMaxY > breatheY) {
			if(fluidState.is(FluidTags.WATER)) {
				canDrown = true;
			} else if(fluidState.getFluidType() != null) {
				canDrown = fluidState.getFluidType().canDrownIn(player);
			}
		}

		if (!canDrown && !player.isInLava())
			return;

		int timeLeft = player.getCustomData()
			.getInt("VisualBacktankAir");

		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();

		ItemStack backtank = getDisplayedBacktank(player);
		poseStack.translate(width / 2 + 90, height - 53 + (backtank.getItem()
			.isFireResistant() ? 9 : 0), 0);

		Component text = Component.literal(StringUtil.formatTickDuration(Math.max(0, timeLeft - 1) * 20));
		GuiGameElement.of(backtank)
			.at(0, 0)
			.render(graphics);
		int color = 0xFF_FFFFFF;
		if (timeLeft < 60 && timeLeft % 2 == 0) {
			color = Color.mixColors(0xFF_FF0000, color, Math.max(timeLeft / 60f, .25f));
		}
		graphics.drawString(mc.font, text, 16, 5, color);

		poseStack.popPose();
	}

	public static ItemStack getDisplayedBacktank(LocalPlayer player) {
		List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
		if (!backtanks.isEmpty()) {
			return backtanks.get(0);
		}
		return AllItems.COPPER_BACKTANK.asStack();
	}
}
