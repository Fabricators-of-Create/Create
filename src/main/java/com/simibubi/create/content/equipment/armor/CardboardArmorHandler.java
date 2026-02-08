package com.simibubi.create.content.equipment.armor;

import java.util.UUID;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.advancement.AllAdvancements;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents;
import io.github.fabricators_of_create.porting_lib.entity.events.LivingEntityEvents;

public class CardboardArmorHandler {

	public static void playerHitboxChangesWhenHidingAsBox(EntityEvents.Size event) {
		Entity entity = event.getEntity();
		if (!entity.isAlive())
			return;
		if (!testForStealth(entity))
			return;

		event.setNewSize(EntityDimensions.fixed(0.6F, 0.8F).withEyeHeight(0.6F));

		if (!entity.level()
			.isClientSide() && entity instanceof Player p)
			AllAdvancements.CARDBOARD_ARMOR.awardTo(p);
	}

	public static void playersStealthWhenWearingCardboard(LivingEntity entity) {
		// Visibility event hook is temporarily disabled on this port path.
	}

	public static void mobsMayLoseTargetWhenItIsWearingCardboard(LivingEntity entity) {
		if (entity.tickCount % 16 != 0)
			return;
		if (!(entity instanceof Mob mob))
			return;

		if (testForStealth(mob.getTarget())) {
			mob.setTarget(null);
			if (mob.targetSelector != null)
				for (WrappedGoal goal : mob.targetSelector.getAvailableGoals()) {
					if (goal.isRunning() && goal.getGoal() instanceof TargetGoal tg)
						tg.stop();
				}
		}

		if (entity instanceof NeutralMob nMob && entity.level() instanceof ServerLevel sl) {
			UUID uuid = nMob.getPersistentAngerTarget();
			if (uuid != null && testForStealth(sl.getEntity(uuid)))
				nMob.stopBeingAngry();
		}

		if (testForStealth(mob.getLastHurtByMob())) {
			mob.setLastHurtByMob(null);
			mob.setLastHurtByPlayer(null);
		}
	}

	public static boolean testForStealth(Entity entityIn) {
		if (!(entityIn instanceof LivingEntity entity))
			return false;
		if (entity.getPose() != Pose.CROUCHING)
			return false;
		if (entity instanceof Player player && player.getAbilities().flying)
			return false;
		if (!AllItems.CARDBOARD_HELMET.isIn(entity.getItemBySlot(EquipmentSlot.HEAD)))
			return false;
		if (!AllItems.CARDBOARD_CHESTPLATE.isIn(entity.getItemBySlot(EquipmentSlot.CHEST)))
			return false;
		if (!AllItems.CARDBOARD_LEGGINGS.isIn(entity.getItemBySlot(EquipmentSlot.LEGS)))
			return false;
		if (!AllItems.CARDBOARD_BOOTS.isIn(entity.getItemBySlot(EquipmentSlot.FEET)))
			return false;
		return true;
	}

}
