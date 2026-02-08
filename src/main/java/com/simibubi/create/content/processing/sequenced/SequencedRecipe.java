package com.simibubi.create.content.processing.sequenced;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SequencedRecipe<T extends ProcessingRecipe<?>> {
	public static final Codec<SequencedRecipe<?>> CODEC = AllRecipeTypes.CODEC
		.<ProcessingRecipe<?>>dispatch(ProcessingRecipe::getRecipeType, AllRecipeTypes::processingCodec)
		.validate(r -> r instanceof IAssemblyRecipe ? DataResult.success(r) :
			DataResult.error(() -> r.getType() + " is not a supported recipe type"))
		.xmap(SequencedRecipe::new, SequencedRecipe::getRecipe);

	public static final StreamCodec<RegistryFriendlyByteBuf, SequencedRecipe<?>> STREAM_CODEC = StreamCodec.of(
			(b, v) -> v.writeToBuffer(b), SequencedRecipe::readFromBuffer
	);

	private final T wrapped;

	public SequencedRecipe(T wrapped) {
		this.wrapped = wrapped;
	}

	public IAssemblyRecipe getAsAssemblyRecipe() {
		return (IAssemblyRecipe) wrapped;
	}

	public ProcessingRecipe<?> getRecipe() {
		return wrapped;
	}

	private void writeToBuffer(RegistryFriendlyByteBuf buffer) {
		@SuppressWarnings("unchecked")
		ProcessingRecipeSerializer<T> serializer = (ProcessingRecipeSerializer<T>) wrapped.getSerializer();
		buffer.writeResourceLocation(RegisteredObjectsHelper.getKeyOrThrow(serializer));
		serializer.STREAM_CODEC.encode(buffer, wrapped);
	}

	private static SequencedRecipe<?> readFromBuffer(RegistryFriendlyByteBuf buffer) {
		ResourceLocation resourcelocation = buffer.readResourceLocation();
		RecipeSerializer<?> serializer = BuiltInRegistries.RECIPE_SERIALIZER.get(resourcelocation);
		//noinspection rawtypes
		if (!(serializer instanceof ProcessingRecipeSerializer processingRecipeSerializer))
			throw new JsonParseException("Not a supported recipe type");
		@SuppressWarnings({"rawtypes", "unchecked"})
		ProcessingRecipe recipe = (ProcessingRecipe) processingRecipeSerializer.STREAM_CODEC.decode(buffer);
		return new SequencedRecipe<>(recipe);
	}

	void initFromSequencedAssembly(SequencedAssemblyRecipe parent, boolean isFirst) {
		if (getAsAssemblyRecipe().supportsAssembly()) {
			Ingredient transit = Ingredient.of(parent.getTransitionalItem());
			wrapped.getIngredients()
				.set(0, transit);
		}
	}
}
