package com.simibubi.create.content.schematics.block;

import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.container.ContainerBase;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SchematicTableContainer extends ContainerBase<SchematicTableTileEntity> {

	private Slot inputSlot;
	private Slot outputSlot;

	public SchematicTableContainer(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public SchematicTableContainer(MenuType<?> type, int id, Inventory inv, SchematicTableTileEntity te) {
		super(type, id, inv, te);
	}

	public static SchematicTableContainer create(int id, Inventory inv, SchematicTableTileEntity te) {
		return new SchematicTableContainer(AllContainerTypes.SCHEMATIC_TABLE.get(), id, inv, te);
	}

	public boolean canWrite() {
		return inputSlot.hasItem() && !outputSlot.hasItem();
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		if (index < 2)
			moveItemStackTo(stack, 2, slots.size(), false);
		else
			moveItemStackTo(stack, 0, 1, false);

		return ItemStack.EMPTY;
	}

	@Override
	protected SchematicTableTileEntity createOnClient(FriendlyByteBuf extraData) {
		ClientLevel world = Minecraft.getInstance().level;
		BlockEntity tileEntity = world.getBlockEntity(extraData.readBlockPos());
		if (tileEntity instanceof SchematicTableTileEntity schematicTable) {
			schematicTable.readClient(extraData.readNbt());
			return schematicTable;
		}
		return null;
	}

	@Override
	protected void initAndReadInventory(SchematicTableTileEntity contentHolder) {
	}

	@Override
	protected void addSlots() {
		inputSlot = new SlotItemHandler(contentHolder.inventory, 0, 21, 57) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return AllItems.EMPTY_SCHEMATIC.isIn(stack) || AllItems.SCHEMATIC_AND_QUILL.isIn(stack)
						|| AllItems.SCHEMATIC.isIn(stack);
			}
		};

		outputSlot = new SlotItemHandler(contentHolder.inventory, 1, 166, 57) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		};

		addSlot(inputSlot);
		addSlot(outputSlot);

		// player Slots
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(player.getInventory(), col + row * 9 + 9, 38 + col * 18, 105 + row * 18));
			}
		}

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
			this.addSlot(new Slot(player.getInventory(), hotbarSlot, 38 + hotbarSlot * 18, 163));
		}
	}

	@Override
	protected void saveData(SchematicTableTileEntity contentHolder) {
	}

}
