package com.simibubi.create.content.kinetics.deployer;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.utility.AdventureUtil;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ManualApplicationRecipe extends ItemApplicationRecipe {

	public static InteractionResult manualApplicationRecipesApplyInWorld(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (AdventureUtil.isAdventure(player))
			return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);
		BlockPos pos = hitResult.getBlockPos();
		BlockState blockState = level.getBlockState(pos);

		if (heldItem.isEmpty())
			return InteractionResult.PASS;
		if (blockState.isAir())
			return InteractionResult.PASS;

		RecipeType<Recipe<RecipeInput>> type = AllRecipeTypes.ITEM_APPLICATION.getType();
		Optional<RecipeHolder<Recipe<RecipeInput>>> foundRecipe = level.getRecipeManager()
			.getAllRecipesFor(type)
			.stream()
			.filter(r -> {
				ManualApplicationRecipe mar = (ManualApplicationRecipe) r.value();
				return mar.testBlock(blockState) && mar.ingredients.get(1)
					.test(heldItem);
			})
			.findFirst();

		if (foundRecipe.isEmpty())
			return InteractionResult.PASS;

//		event.setCancellationResult(InteractionResult.SUCCESS);
//		event.setCanceled(true);

		if (level.isClientSide())
			return InteractionResult.SUCCESS;

		level.playSound(null, pos, SoundEvents.COPPER_BREAK, SoundSource.PLAYERS, 1, 1.45f);
		ManualApplicationRecipe recipe = (ManualApplicationRecipe) foundRecipe.get().value();
		level.destroyBlock(pos, false);

		BlockState transformedBlock = recipe.transformBlock(blockState);
		level.setBlock(pos, transformedBlock, 3);
		recipe.rollResults()
			.forEach(stack -> Block.popResource(level, pos, stack));

		boolean creative = player != null && player
			.isCreative();
		boolean unbreakable = heldItem.has(DataComponents.UNBREAKABLE);
		boolean keepHeld = recipe.shouldKeepHeldItem() || creative;

		if (!unbreakable && !keepHeld) {
			if (heldItem.isDamageableItem())
				heldItem.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
			else
				heldItem.shrink(1);
		}

		awardAdvancements(player, transformedBlock);
		return InteractionResult.SUCCESS;
	}

	private static void awardAdvancements(Player player, BlockState placed) {
		CreateAdvancement advancement = null;

		if (AllBlocks.ANDESITE_CASING.has(placed))
			advancement = AllAdvancements.ANDESITE_CASING;
		else if (AllBlocks.BRASS_CASING.has(placed))
			advancement = AllAdvancements.BRASS_CASING;
		else if (AllBlocks.COPPER_CASING.has(placed))
			advancement = AllAdvancements.COPPER_CASING;
		else if (AllBlocks.RAILWAY_CASING.has(placed))
			advancement = AllAdvancements.TRAIN_CASING;
		else
			return;

		advancement.awardTo(player);
	}

	public ManualApplicationRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.ITEM_APPLICATION, params);
	}

	public static RecipeHolder<DeployerApplicationRecipe> asDeploying(RecipeHolder<?> recipe) {
		ManualApplicationRecipe mar = (ManualApplicationRecipe) recipe.value();
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(mar.id.getNamespace(), mar.id.getPath() + "_using_deployer");
		ProcessingRecipeBuilder<DeployerApplicationRecipe> builder =
			new ProcessingRecipeBuilder<>(DeployerApplicationRecipe::new, id)
					.require(mar.ingredients.get(0))
					.require(mar.ingredients.get(1));
		for (ProcessingOutput output : mar.results)
			builder.output(output);
		if (mar.shouldKeepHeldItem())
			builder.toolNotConsumed();
		return new RecipeHolder<>(id, builder.build());
	}

	public boolean testBlock(BlockState in) {
		return ingredients.get(0)
			.test(new ItemStack(in.getBlock()
				.asItem()));
	}

	public BlockState transformBlock(BlockState in) {
		ProcessingOutput mainOutput = results.get(0);
		ItemStack output = mainOutput.rollOutput();
		if (output.getItem() instanceof BlockItem bi)
			return BlockHelper.copyProperties(in, bi.getBlock()
				.defaultBlockState());
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public List<ItemStack> rollResults() {
		return rollResults(getRollableResultsExceptBlock());
	}

	public List<ProcessingOutput> getRollableResultsExceptBlock() {
		ProcessingOutput mainOutput = results.get(0);
		if (mainOutput.getStack()
			.getItem() instanceof BlockItem)
			return results.subList(1, results.size());
		return results;
	}

}
