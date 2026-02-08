package com.simibubi.create.content.fluids.potion;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.lang.Lang;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.VirtualFluid;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.alchemy.PotionContents;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

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

	public static FluidStack of(long amount, PotionContents potionContents, BottleType bottleType) {
		return new FluidStack(AllFluids.POTION.get().getSource(), amount);
	}

	public static FluidStack addPotionToFluidStack(FluidStack fs, PotionContents potionContents) {
		return fs;
	}

	public enum BottleType implements StringRepresentable {
		REGULAR, SPLASH, LINGERING;

		public static final Codec<BottleType> CODEC = StringRepresentable.fromEnum(BottleType::values);
		public static final StreamCodec<ByteBuf, BottleType> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(BottleType.class);

		@Override
		public @NotNull String getSerializedName() {
			return Lang.asId(name());
		}
	}
/*
	// fabric: PotionFluidVariantRenderHandler and PotionFluidVariantAttributeHandler in AllFluids
	public static class PotionFluidAttributes extends FluidAttributes {

		public PotionFluidAttributes(Builder builder, Fluid fluid) {
			super(builder, fluid);
		}

		@Override
		public int getColor(FluidStack stack) {
			return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor() | 0xff000000;
		}

		@Override
		public Component getDisplayName(FluidStack stack) {
			return Components.translatable(getTranslationKey(stack));
		}

		@Override
		public String getTranslationKey(FluidStack stack) {
			PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
			ItemLike itemFromBottleType =
					PotionFluidHandler.itemFromBottleType(stack.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR));
			return Potion
					.getName(contents.potion(),itemFromBottleType.asItem()
							.getDescriptionId() + ".effect.");
		}

	}*/

}
