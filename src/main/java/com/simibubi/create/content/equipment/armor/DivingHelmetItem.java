package com.simibubi.create.content.equipment.armor;

import java.util.List;
import java.util.Map;

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.foundation.advancement.AllAdvancements;

import io.github.fabricators_of_create.porting_lib.enchant.CustomEnchantingBehaviorItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import io.github.fabricators_of_create.porting_lib.item.CustomEnchantmentLevelItem;
import io.github.fabricators_of_create.porting_lib.item.CustomEnchantmentsItem;

public class DivingHelmetItem extends BaseArmorItem implements CustomEnchantingBehaviorItem, CustomEnchantmentLevelItem, CustomEnchantmentsItem {
	public static final EquipmentSlot SLOT = EquipmentSlot.HEAD;
	public static final ArmorItem.Type TYPE = ArmorItem.Type.HELMET;

	public DivingHelmetItem(ArmorMaterial material, Properties properties, ResourceLocation textureLoc) {
		super(material, TYPE, properties, textureLoc);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		if (enchantment == Enchantments.AQUA_AFFINITY) {
			return false;
		}
		return CustomEnchantingBehaviorItem.super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public int modifyEnchantmentLevel(ItemStack stack, Enchantment enchantment, int level) {
		if (enchantment == Enchantments.AQUA_AFFINITY) {
			return 1;
		}
		return level;
	}

	@Override
	public void modifyEnchantments(Map<Enchantment, Integer> enchantments, ItemStack stack) {
		enchantments.put(Enchantments.AQUA_AFFINITY, 1);
	}

	public static boolean isWornBy(Entity entity) {
		return !getWornItem(entity).isEmpty();
	}

	public static ItemStack getWornItem(Entity entity) {
		if (!(entity instanceof LivingEntity livingEntity)) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = livingEntity.getItemBySlot(SLOT);
		if (!(stack.getItem() instanceof DivingHelmetItem)) {
			return ItemStack.EMPTY;
		}
		return stack;
	}

	public static boolean breatheUnderwater(LivingEntity entity) {
//		LivingEntity entity = event.getEntityLiving();
		Level world = entity.level();
		boolean second = world.getGameTime() % 20 == 0;
		boolean drowning = entity.getAirSupply() == 0;

		if (world.isClientSide)
			entity.getCustomData().remove("VisualBacktankAir");

		ItemStack helmet = getWornItem(entity);
		if (helmet.isEmpty())
			return false;

		boolean lavaDiving = entity.isInLava();
		if (!helmet.getItem().isFireResistant() && lavaDiving)
			return false;
		if (!entity.isEyeInFluid(AllFluidTags.DIVING_FLUIDS.tag) && !lavaDiving)
			return false;
		if (entity instanceof Player player && (player.isSpectator() || player.isCreative()))
			return false;

		List<ItemStack> backtanks = BacktankUtil.getAllWithAir(entity);
		if (backtanks.isEmpty())
			return false;

		if (lavaDiving) {
			if (entity instanceof ServerPlayer sp)
				AllAdvancements.DIVING_SUIT_LAVA.awardTo(sp);
			if (backtanks.stream()
				.noneMatch(backtank -> backtank.getItem()
					.isFireResistant()))
				return false;
		}

		float visualBacktankAir = 0f;
		for (ItemStack stack : backtanks)
			visualBacktankAir += BacktankUtil.getAir(stack);

		if (world.isClientSide)
			entity.getCustomData()
				.putInt("VisualBacktankAir", Math.round(visualBacktankAir));

		if (world.getGameTime() % 20 == 0)
			BacktankUtil.consumeAir(entity, backtanks.get(0), 1);

		if (lavaDiving)
			return false;

		if (entity instanceof ServerPlayer sp)
			AllAdvancements.DIVING_SUIT.awardTo(sp);

		return true;
	}
}
