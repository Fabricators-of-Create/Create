package net.fabricmc.fabric.api.item.v1;

import java.util.function.Function;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FabricItemSettings extends Item.Properties {
	public FabricItemSettings equipmentSlot(Function<ItemStack, EquipmentSlot> provider) {
		return this;
	}
}
