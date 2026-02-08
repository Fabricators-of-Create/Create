package com.simibubi.create;

import com.simibubi.create.content.equipment.blueprint.BlueprintMenu;
import com.simibubi.create.content.equipment.blueprint.BlueprintScreen;
import com.simibubi.create.content.equipment.toolbox.ToolboxMenu;
import com.simibubi.create.content.equipment.toolbox.ToolboxScreen;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemMenu;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu;
import com.simibubi.create.content.logistics.filter.AttributeFilterScreen;
import com.simibubi.create.content.logistics.filter.FilterMenu;
import com.simibubi.create.content.logistics.filter.FilterScreen;
import com.simibubi.create.content.logistics.filter.PackageFilterMenu;
import com.simibubi.create.content.logistics.filter.PackageFilterScreen;
import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.simibubi.create.content.logistics.packagePort.PackagePortScreen;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryScreen;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerMenu;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerScreen;
import com.simibubi.create.content.schematics.cannon.SchematicannonMenu;
import com.simibubi.create.content.schematics.cannon.SchematicannonScreen;
import com.simibubi.create.content.schematics.table.SchematicTableMenu;
import com.simibubi.create.content.schematics.table.SchematicTableScreen;
import com.simibubi.create.content.trains.schedule.ScheduleMenu;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.builders.MenuBuilder.ScreenFactory;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class AllMenuTypes {

	public static final MenuEntry<SchematicTableMenu> SCHEMATIC_TABLE =
		register("schematic_table", (type, id, inv, data) -> new SchematicTableMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> SchematicTableScreen::new);

	public static final MenuEntry<SchematicannonMenu> SCHEMATICANNON =
		register("schematicannon", (type, id, inv, data) -> new SchematicannonMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> SchematicannonScreen::new);

	public static final MenuEntry<FilterMenu> FILTER =
		register("filter", (type, id, inv, data) -> new FilterMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> FilterScreen::new);

	public static final MenuEntry<AttributeFilterMenu> ATTRIBUTE_FILTER =
		register("attribute_filter", (type, id, inv, data) -> new AttributeFilterMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> AttributeFilterScreen::new);

	public static final MenuEntry<PackageFilterMenu> PACKAGE_FILTER =
		register("package_filter", (type, id, inv, data) -> new PackageFilterMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> PackageFilterScreen::new);

	public static final MenuEntry<BlueprintMenu> CRAFTING_BLUEPRINT =
		register("crafting_blueprint", (type, id, inv, data) -> new BlueprintMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> BlueprintScreen::new);

	public static final MenuEntry<LinkedControllerMenu> LINKED_CONTROLLER =
		register("linked_controller", (type, id, inv, data) -> new LinkedControllerMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> LinkedControllerScreen::new);

	public static final MenuEntry<ToolboxMenu> TOOLBOX =
		register("toolbox", (type, id, inv, data) -> new ToolboxMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> ToolboxScreen::new);

	public static final MenuEntry<ScheduleMenu> SCHEDULE =
		register("schedule", (type, id, inv, data) -> new ScheduleMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> ScheduleScreen::new);

	public static final MenuEntry<StockKeeperCategoryMenu> STOCK_KEEPER_CATEGORY =
		register("stock_keeper_category", (type, id, inv, data) -> new StockKeeperCategoryMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> StockKeeperCategoryScreen::new);

	public static final MenuEntry<StockKeeperRequestMenu> STOCK_KEEPER_REQUEST =
		register("stock_keeper_request", (type, id, inv, data) -> new StockKeeperRequestMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> StockKeeperRequestScreen::new);

	public static final MenuEntry<PackagePortMenu> PACKAGE_PORT =
		register("package_port", (type, id, inv, data) -> new PackagePortMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> PackagePortScreen::new);

	public static final MenuEntry<RedstoneRequesterMenu> REDSTONE_REQUESTER =
		register("redstone_requester", (type, id, inv, data) -> new RedstoneRequesterMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> RedstoneRequesterScreen::new);

	public static final MenuEntry<FactoryPanelSetItemMenu> FACTORY_PANEL_SET_ITEM =
		register("factory_panel_set_item", (type, id, inv, data) -> new FactoryPanelSetItemMenu(type, id, inv, (RegistryFriendlyByteBuf) data), () -> FactoryPanelSetItemScreen::new);

	private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>> MenuEntry<C> register(
			String name, MenuBuilder.ForgeMenuFactory<C> factory, NonNullSupplier<ScreenFactory<C, S>> screenFactory) {
		return Create.registrate()
			.menu(name, factory, screenFactory)
			.register();
	}

	public static void register() {
	}

}
