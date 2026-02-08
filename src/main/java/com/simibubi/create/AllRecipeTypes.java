package com.simibubi.create;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.core.Registry;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.equipment.toolbox.ToolboxDyeingRecipe;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.kinetics.fan.processing.HauntingRecipe;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeFactory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.recipe.ItemCopyingRecipe;

import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public enum AllRecipeTypes implements IRecipeTypeInfo, StringRepresentable {

	CONVERSION(PressingRecipe::new),
	CRUSHING(CrushingRecipe::new),
	CUTTING(CuttingRecipe::new),
	MILLING(MillingRecipe::new),
	BASIN(BasinRecipe::new),
	MIXING(MixingRecipe::new),
	COMPACTING(CompactingRecipe::new),
	PRESSING(PressingRecipe::new),
	SANDPAPER_POLISHING(SandPaperPolishingRecipe::new),
	SPLASHING(SplashingRecipe::new),
	HAUNTING(HauntingRecipe::new),
	DEPLOYING(DeployerApplicationRecipe::new),
	FILLING(FillingRecipe::new),
	EMPTYING(EmptyingRecipe::new),
	ITEM_APPLICATION(ManualApplicationRecipe::new),

	MECHANICAL_CRAFTING(MechanicalCraftingRecipe.Serializer::new),
	SEQUENCED_ASSEMBLY(SequencedAssemblyRecipeSerializer::new),

	TOOLBOX_DYEING(() -> new SimpleCraftingRecipeSerializer<>(ToolboxDyeingRecipe::new), () -> RecipeType.CRAFTING, false),
	ITEM_COPYING(() -> new SimpleCraftingRecipeSerializer<>(ItemCopyingRecipe::new), () -> RecipeType.CRAFTING, false);

	public static final Predicate<RecipeHolder<?>> CAN_BE_AUTOMATED = r -> !r.id()
			.getPath()
			.endsWith("_manual_only");

	private final ResourceLocation id;
	private final RecipeSerializer<?> serializerObject;
	@Nullable
	private final RecipeType<?> typeObject;

	private boolean isProcessingRecipe;

	public static final Codec<AllRecipeTypes> CODEC = StringRepresentable.fromEnum(AllRecipeTypes::values);

	AllRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier, Supplier<RecipeType<?>> typeSupplier, boolean registerType) {
		String name = Lang.asId(name());
		id = Create.asResource(name);
		serializerObject = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializerSupplier.get());
		if (registerType) {
			typeObject = typeSupplier.get();
			Registry.register(BuiltInRegistries.RECIPE_TYPE, id, typeObject);
		} else {
			typeObject = typeSupplier.get();
		}
		isProcessingRecipe = false;
	}

	AllRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
		String name = Lang.asId(name());
		id = Create.asResource(name);
		serializerObject = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializerSupplier.get());
		typeObject = Registry.register(BuiltInRegistries.RECIPE_TYPE, id, createType(id));
		isProcessingRecipe = false;
	}

	AllRecipeTypes(ProcessingRecipeFactory<?> processingFactory) {
		String name = Lang.asId(name());
		id = Create.asResource(name);
		serializerObject = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, new ProcessingRecipeSerializer<>(processingFactory, this));
		typeObject = Registry.register(BuiltInRegistries.RECIPE_TYPE, id, createType(id));
		isProcessingRecipe = true;
	}

	@Internal
	public static void register() {
		ShapedRecipeUtil.setCraftingSize(9, 9);
		// fabric: just load the class
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeSerializer<?>> T getSerializer() {
		return (T) serializerObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
		return (RecipeType<R>) this.typeObject;
	}

	public <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> find(I inv, Level world) {
		return world.getRecipeManager()
			.getRecipeFor(getType(), inv, world);
	}

	public static boolean shouldIgnoreInAutomation(RecipeHolder<?> recipe) {
		RecipeSerializer<?> serializer = recipe.value().getSerializer();
		if (serializer != null && AllTags.AllRecipeSerializerTags.AUTOMATION_IGNORE.matches(serializer))
			return true;
		return !CAN_BE_AUTOMATED.test(recipe);
	}

	@Override
	public @NotNull String getSerializedName() {
		return id.toString();
	}

	private static <T extends Recipe<?>> RecipeType<T> createType(ResourceLocation id) {
		String string = id.toString();
		return new RecipeType<T>() {
			@Override
			public String toString() {
				return string;
			}
		};
	}

	public <T extends ProcessingRecipe<?>> MapCodec<T> processingCodec() {
		if (!isProcessingRecipe)
			throw new AssertionError("AllRecipeTypes#processingCodec called on " + name() + ", which is not a processing recipe");
		if (this == DEPLOYING || this == ITEM_APPLICATION)
			return ItemApplicationRecipe.codec(this);
		return ProcessingRecipeSerializer.codec(this);
	}
}
