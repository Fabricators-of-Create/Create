package com.simibubi.create.content.fluids.potion;

import java.util.Collection;
import java.util.List;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.VirtualFluid;

import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class PotionFluid extends VirtualFluid {

	public static PotionFluid createSource(Properties properties) {
		return new PotionFluid(properties, true);
	}

	public static PotionFluid createFlowing(Properties properties) {
		return new PotionFluid(properties, false);
	}

	public PotionFluid(Properties properties, boolean source) {
		super(properties, source);
	}

	public static FluidStack of(long amount, Potion potion, BottleType bottleType) {

		FluidStack fluidStack;
		fluidStack = new FluidStack(AllFluids.POTION.get().getSource(), amount);
		fluidStack = addPotionToFluidStack(fluidStack, potion);

		CompoundTag nbt = fluidStack.getOrCreateTag().copy();
		NBTHelper.writeEnum(nbt, "Bottle", bottleType);
		return new FluidStack(fluidStack.getFluid(), amount, nbt);
	}

	public static FluidStack withEffects(long amount, Potion potion, List<MobEffectInstance> customEffects) {
		FluidStack fluidStack = of(amount, potion, BottleType.REGULAR);
		return appendEffects(fluidStack, customEffects);
	}

	public static FluidStack addPotionToFluidStack(FluidStack fs, Potion potion) {
		ResourceLocation resourcelocation = CatnipServices.REGISTRIES.getKeyOrThrow(potion);
		if (potion == Potions.EMPTY) {
			fs.removeChildTag("Potion");
			return new FluidStack(fs.getFluid(), fs.getAmount(), fs.getTag());
		}
		fs.getOrCreateTag()
				.putString("Potion", resourcelocation.toString());
		return new FluidStack(fs.getFluid(), fs.getAmount(), fs.getTag());
	}

	public static FluidStack appendEffects(FluidStack fs, Collection<MobEffectInstance> customEffects) {
		if (customEffects.isEmpty())
			return fs;
		CompoundTag compoundnbt = fs.getOrCreateTag();
		ListTag listnbt = compoundnbt.getList("CustomPotionEffects", 9);
		for (MobEffectInstance effectinstance : customEffects)
			listnbt.add(effectinstance.save(new CompoundTag()));
		compoundnbt.put("CustomPotionEffects", listnbt);
		return new FluidStack(fs.getFluid(), fs.getAmount(), fs.getTag());
	}

	public enum BottleType {
		REGULAR, SPLASH, LINGERING;
	}
/*
	// fabric: PotionFluidVariantRenderHandler and PotionFluidVariantAttributeHandler in AllFluids
	public static class PotionFluidAttributes extends FluidAttributes {

		public PotionFluidAttributes(Builder builder, Fluid fluid) {
			super(builder, fluid);
		}

		@Override
		public int getColor(FluidStack stack) {
			CompoundTag tag = stack.getOrCreateTag();
			int color = PotionUtils.getColor(PotionUtils.getAllEffects(tag)) | 0xff000000;
			return color;
		}

		@Override
		public Component getDisplayName(FluidStack stack) {
			return Components.translatable(getTranslationKey(stack));
		}

		@Override
		public String getTranslationKey(FluidStack stack) {
			CompoundTag tag = stack.getOrCreateTag();
			ItemLike itemFromBottleType =
					PotionFluidHandler.itemFromBottleType(NBTHelper.readEnum(tag, "Bottle", BottleType.class));
			return PotionUtils.getPotion(tag)
					.getName(itemFromBottleType.asItem()
							.getDescriptionId() + ".effect.");
		}

	}*/

}
