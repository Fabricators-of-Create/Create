package com.simibubi.create.compat.emi;

import java.util.List;

import com.simibubi.create.foundation.mixin.fabric.ClientTextTooltipAccessor;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;

import io.github.fabricators_of_create.porting_lib.util.FluidTextUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidUnit;

public class CreateSlotWidget extends SlotWidget {
	public CreateSlotWidget(EmiIngredient stack, int x, int y) {
		super(stack, x, y);
	}

	@Override
	public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
		List<ClientTooltipComponent> tooltip = super.getTooltip(mouseX, mouseY);
		if (stack instanceof EmiStack emiStack && emiStack.getKey() instanceof Fluid fluid) {
			// add custom fluid tooltip
			FluidVariant variant = TransferUtil.fluidVariantOf(fluid, emiStack.getNbt());
			addCreateAmount(tooltip, variant);
			removeEmiAmount(tooltip, variant);
		}
		return tooltip;
	}

	private void addCreateAmount(List<ClientTooltipComponent> tooltip, FluidVariant fluid) {
		FluidUnit unit = AllConfigs.client().fluidUnitType.get();
		String amount = FluidTextUtil.getUnicodeMillibuckets(stack.getAmount(), unit, AllConfigs.client().simplifyFluidUnit.get());

		Component amountComponent = Component.literal(" " + amount)
				.append(CreateLang.translateDirect(unit.getTranslationKey()))
				.withStyle(ChatFormatting.GOLD);

		MutableComponent fluidName = FluidVariantAttributes.getName(fluid)
				.copy()
				.append(amountComponent);

		ClientTooltipComponent component = toTooltip(fluidName);

		if (tooltip.isEmpty()) {
			tooltip.add(component);
		} else {
			tooltip.set(0, toTooltip(fluidName));
		}
	}

	private void removeEmiAmount(List<ClientTooltipComponent> tooltip, FluidVariant entry) {
		Fluid fluid = entry.getFluid();
		String namespace = BuiltInRegistries.FLUID.getKey(fluid).getNamespace();
		String modName = FabricLoader.getInstance().getModContainer(namespace).map(c -> c.getMetadata().getName()).orElse(null);
		if (modName == null)
			return;
		int indexOfModName = -1;
		for (int i = 0; i < tooltip.size(); i++) {
			ClientTooltipComponent component = tooltip.get(i);
			if (component instanceof ClientTextTooltipAccessor text) {
				FormattedCharSequence contents = text.create$text();
				StringBuilder string = new StringBuilder();
				contents.accept((what, style, c) -> {
					string.append((char) c);
					return true;
				});
				if (string.toString().equals(modName)) {
					indexOfModName = i;
					break;
				}
			}
		}
		if (indexOfModName != -1) {
			// emi amount is always(?) right above the mod name
			int indexOfAmount = indexOfModName - 1;
			if (indexOfAmount > 0) {
				tooltip.remove(indexOfAmount);
			}
		}
	}

	private static ClientTooltipComponent toTooltip(Component component) {
		return ClientTooltipComponent.create(component.getVisualOrderText());
	}
}
