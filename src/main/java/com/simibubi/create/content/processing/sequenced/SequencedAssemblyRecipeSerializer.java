package com.simibubi.create.content.processing.sequenced;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SequencedAssemblyRecipeSerializer implements RecipeSerializer<SequencedAssemblyRecipe> {

	private MapCodec<SequencedAssemblyRecipe> codec;

	public final StreamCodec<RegistryFriendlyByteBuf, SequencedAssemblyRecipe> STREAM_CODEC = StreamCodec.of(
			this::toNetwork, this::fromNetwork
	);

	public SequencedAssemblyRecipeSerializer() {}

	private MapCodec<SequencedAssemblyRecipe> createCodec() {
		return RecordCodecBuilder.mapCodec(
			i -> i.group(
				Ingredient.CODEC.fieldOf("ingredient")
					.forGetter(SequencedAssemblyRecipe::getIngredient),
				ProcessingOutput.CODEC.fieldOf("transitional_item")
					.forGetter(r -> r.transitionalItem),
				SequencedRecipe.CODEC.listOf()
					.fieldOf("sequence")
					.forGetter(SequencedAssemblyRecipe::getSequence),
				ProcessingOutput.CODEC.listOf()
					.fieldOf("results")
					.forGetter(r -> r.resultPool),
				ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("loops")
					.forGetter(r -> Optional.of(r.getLoops()))
			).apply(i, (ingredient, transitionalItem, sequence, results, loops) -> {
				SequencedAssemblyRecipe recipe = new SequencedAssemblyRecipe(this);
				recipe.ingredient = ingredient;
				recipe.transitionalItem = transitionalItem;
				recipe.sequence.addAll(sequence);
				recipe.resultPool.addAll(results);
				recipe.loops = loops.orElse(5);

				for (int j = 0; j < recipe.sequence.size(); j++)
					sequence.get(j).initFromSequencedAssembly(recipe, j == 0);

				return recipe;
			})
		);
	}

	protected void toNetwork(RegistryFriendlyByteBuf buffer, SequencedAssemblyRecipe recipe) {
		Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getIngredient());
		SequencedRecipe.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.getSequence());
		ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.resultPool);
		recipe.transitionalItem.write(buffer);
		buffer.writeInt(recipe.loops);
	}

	protected SequencedAssemblyRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
		SequencedAssemblyRecipe recipe = new SequencedAssemblyRecipe(this);
		recipe.ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
		recipe.getSequence().addAll(SequencedRecipe.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer));
		recipe.resultPool.addAll(ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer));
		recipe.transitionalItem = ProcessingOutput.read(buffer);
		recipe.loops = buffer.readInt();
		return recipe;
	}

	@Override
	public @NotNull MapCodec<SequencedAssemblyRecipe> codec() {
		if (codec == null)
			codec = createCodec();
		return codec;
	}

	@Override
	public @NotNull StreamCodec<RegistryFriendlyByteBuf, SequencedAssemblyRecipe> streamCodec() {
		return STREAM_CODEC;
	}

}
