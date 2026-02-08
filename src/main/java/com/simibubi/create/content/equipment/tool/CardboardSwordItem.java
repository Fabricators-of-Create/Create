package com.simibubi.create.content.equipment.tool;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;

import io.github.fabricators_of_create.porting_lib.entity.events.LivingAttackEvent;

public class CardboardSwordItem extends SwordItem {

	public CardboardSwordItem(Properties properties) {
		super(AllToolMaterials.CARDBOARD, properties);
	}

	public static InteractionResult cardboardSwordsMakeNoiseOnClick(Player player, Level level, InteractionHand hand,
		BlockPos pos, Direction direction) {
		ItemStack stack = player.getItemInHand(hand);
		if (!AllItems.CARDBOARD_SWORD.isIn(stack)) {
			return InteractionResult.PASS;
		}
		AllSoundEvents.CARDBOARD_SWORD.play(level, player, pos, 0.5f, 1.85f);
		return InteractionResult.SUCCESS;
	}

	public static void cardboardSwordsCannotHurtYou(LivingAttackEvent event) {
		LivingEntity target = event.getEntity();
		if (target == null) {
			return;
		}
		event.setCanceled(true);
	}
}
