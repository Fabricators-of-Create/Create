package com.simibubi.create.infrastructure.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.github.fabricators_of_create.porting_lib.mixin.client.accessor.ScreenAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import org.apache.commons.lang3.mutable.MutableObject;

public class OpenCreateMenuButton extends Button {

	public static final ItemStack ICON = AllItems.GOGGLES.asStack();

	public OpenCreateMenuButton(int x, int y) {
		super(x, y, 20, 20, Components.immutableEmpty(), OpenCreateMenuButton::click);
	}

	@Override
	public void renderBg(PoseStack mstack, Minecraft mc, int mouseX, int mouseY) {
		Minecraft.getInstance().getItemRenderer().renderGuiItem(ICON, x + 2, y + 2);
	}

	public static void click(Button b) {
		ScreenOpener.open(new CreateMainMenuScreen(Minecraft.getInstance().screen));
	}

	public record SingleMenuRow(String leftTextKey, String rightTextKey) {
		public SingleMenuRow(String centerTextKey) {
			this(centerTextKey, centerTextKey);
		}
	}

	public static class MenuRows {
		public static final MenuRows MAIN_MENU = new MenuRows(Arrays.asList(
			new SingleMenuRow("menu.singleplayer"),
			new SingleMenuRow("menu.multiplayer"),
			new SingleMenuRow("menu.online"),
			new SingleMenuRow("narrator.button.language", "narrator.button.accessibility")
		));

		public static final MenuRows INGAME_MENU = new MenuRows(Arrays.asList(
			new SingleMenuRow("menu.returnToGame"),
			new SingleMenuRow("gui.advancements", "gui.stats"),
			new SingleMenuRow("menu.sendFeedback", "menu.reportBugs"),
			new SingleMenuRow("menu.options", "menu.shareToLan"),
			new SingleMenuRow("menu.returnToMenu")
		));

		protected final List<String> leftTextKeys, rightTextKeys;

		public MenuRows(List<SingleMenuRow> rows) {
			leftTextKeys = rows.stream().map(SingleMenuRow::leftTextKey).collect(Collectors.toList());
			rightTextKeys = rows.stream().map(SingleMenuRow::rightTextKey).collect(Collectors.toList());
		}
	}

	public static class OpenConfigButtonHandler {

		public static void onGuiInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
			MenuRows menu;
			int rowIdx;
			int offsetX;
			if (screen instanceof TitleScreen) {
				menu = MenuRows.MAIN_MENU;
				rowIdx = AllConfigs.client().mainMenuConfigButtonRow.get();
				offsetX = AllConfigs.client().mainMenuConfigButtonOffsetX.get();
			} else if (screen instanceof PauseScreen) {
				menu = MenuRows.INGAME_MENU;
				rowIdx = AllConfigs.client().ingameMenuConfigButtonRow.get();
				offsetX = AllConfigs.client().ingameMenuConfigButtonOffsetX.get();
			} else {
				return;
			}

			if (rowIdx == 0) {
				return;
			}

			boolean onLeft = offsetX < 0;
			String targetMessage = I18n.get((onLeft ? menu.leftTextKeys : menu.rightTextKeys).get(rowIdx - 1));

			int offsetX_ = offsetX;
			MutableObject<OpenCreateMenuButton> toAdd = new MutableObject<>(null);
			((ScreenAccessor) screen).port_lib$getChildren().stream()
				.filter(w -> w instanceof AbstractWidget)
				.map(w -> (AbstractWidget) w)
				.filter(w -> w.getMessage()
					.getString()
					.equals(targetMessage))
				.findFirst()
				.ifPresent(w -> toAdd
					.setValue(new OpenCreateMenuButton(w.x + offsetX_ + (onLeft ? -20 : w.getWidth()), w.y)));
			if (toAdd.getValue() != null)
				screen.addRenderableWidget(toAdd.getValue());
		}

	}

}
