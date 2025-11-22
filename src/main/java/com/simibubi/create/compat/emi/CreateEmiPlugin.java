package com.simibubi.create.compat.emi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.emi.recipes.AutomaticPackingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.BlockCuttingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.BlockCuttingEmiRecipe.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.emi.recipes.ConversionRecipe;
import com.simibubi.create.compat.emi.recipes.CreateEmiRecipe;
import com.simibubi.create.compat.emi.recipes.CrushingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.DeployingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.DrainEmiRecipe;
import com.simibubi.create.compat.emi.recipes.ManualItemApplicationEmiRecipe;
import com.simibubi.create.compat.emi.recipes.MechanicalCraftingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.MillingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.MysteriousConversionEmiRecipe;
import com.simibubi.create.compat.emi.recipes.PolishingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.PressingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.SawingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.SequencedAssemblyEmiRecipe;
import com.simibubi.create.compat.emi.recipes.SpoutEmiRecipe;
import com.simibubi.create.compat.emi.recipes.basin.MixingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.basin.PackingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.basin.ShapelessEmiRecipe;
import com.simibubi.create.compat.emi.recipes.fan.FanBlastingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.fan.FanEmiRecipe;
import com.simibubi.create.compat.emi.recipes.fan.FanHauntingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.fan.FanSmokingEmiRecipe;
import com.simibubi.create.compat.emi.recipes.fan.FanWashingEmiRecipe;
import com.simibubi.create.compat.recipeViewerCommon.HiddenItems;
import com.simibubi.create.compat.rei.ToolboxColoringRecipeMaker;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.equipment.blueprint.BlueprintScreen;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlock;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.potion.PotionMixingRecipes;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.simibubi.create.content.logistics.filter.AttributeFilterScreen;
import com.simibubi.create.content.logistics.filter.FilterScreen;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerScreen;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;
import com.tterrag.registrate.util.entry.FluidEntry;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.MutableContainerItemContext;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;

public class CreateEmiPlugin implements EmiPlugin {
	public static final Map<ResourceLocation, EmiRecipeCategory> ALL = new LinkedHashMap<>();

	public static final EmiRecipeCategory
			MILLING = register("milling", DoubleItemIcon.of(AllBlocks.MILLSTONE.get(), AllItems.WHEAT_FLOUR.get())),
			CRUSHING = register("crushing", DoubleItemIcon.of(AllBlocks.CRUSHING_WHEEL.get(), AllItems.CRUSHED_GOLD.get())),
			PRESSING = register("pressing", DoubleItemIcon.of(AllBlocks.MECHANICAL_PRESS.get(), AllItems.IRON_SHEET.get())),
			FAN_WASHING = register("fan_washing", DoubleItemIcon.of(AllItems.PROPELLER.get(), Items.WATER_BUCKET)),
			FAN_SMOKING = register("fan_smoking", DoubleItemIcon.of(AllItems.PROPELLER.get(), Items.CAMPFIRE)),
			FAN_BLASTING = register("fan_blasting", DoubleItemIcon.of(AllItems.PROPELLER.get(), Items.LAVA_BUCKET)),
			FAN_HAUNTING = register("fan_haunting", DoubleItemIcon.of(AllItems.PROPELLER.get(), Items.SOUL_CAMPFIRE)),
			MIXING = register("mixing", DoubleItemIcon.of(AllBlocks.MECHANICAL_MIXER.get(), AllBlocks.BASIN.get())),
			AUTOMATIC_SHAPELESS = register("automatic_shapeless", DoubleItemIcon.of(AllBlocks.MECHANICAL_MIXER.get(), Items.CRAFTING_TABLE)),
			AUTOMATIC_BREWING = register("automatic_brewing", DoubleItemIcon.of(AllBlocks.MECHANICAL_MIXER.get(), Blocks.BREWING_STAND)),
			PACKING = register("packing", DoubleItemIcon.of(AllBlocks.MECHANICAL_PRESS.get(), AllBlocks.BASIN.get())),
			AUTOMATIC_PACKING = register("automatic_packing", DoubleItemIcon.of(AllBlocks.MECHANICAL_PRESS.get(), Blocks.CRAFTING_TABLE)),
			SAWING = register("sawing", DoubleItemIcon.of(AllBlocks.MECHANICAL_SAW.get(), Items.OAK_LOG)),
			BLOCK_CUTTING = register("block_cutting", DoubleItemIcon.of(AllBlocks.MECHANICAL_SAW.get(), Items.STONE_BRICK_STAIRS)),
			WOOD_CUTTING = register("wood_cutting", DoubleItemIcon.of(AllBlocks.MECHANICAL_SAW.get(), Items.OAK_STAIRS)),
			SANDPAPER_POLISHING = register("sandpaper_polishing", EmiStack.of(AllItems.SAND_PAPER.get())),
			ITEM_APPLICATION = register("item_application", EmiStack.of(AllItems.BRASS_HAND.get())),
			DEPLOYING = register("deploying", EmiStack.of(AllBlocks.DEPLOYER.get())),
			SPOUT_FILLING = register("spout_filling", DoubleItemIcon.of(AllBlocks.SPOUT.get(), Items.WATER_BUCKET)),
			DRAINING = register("draining", DoubleItemIcon.of(AllBlocks.ITEM_DRAIN.get(), Items.WATER_BUCKET)),
			AUTOMATIC_SHAPED = register("automatic_shaped", EmiStack.of(AllBlocks.MECHANICAL_CRAFTER.get())),
			MECHANICAL_CRAFTING = register("mechanical_crafting", EmiStack.of(AllBlocks.MECHANICAL_CRAFTER.get())),
			SEQUENCED_ASSEMBLY = register("sequenced_assembly", EmiStack.of(AllItems.PRECISION_MECHANISM.get())),
			MYSTERY_CONVERSION = register("mystery_conversion", EmiStack.of(AllBlocks.PECULIAR_BELL.get()));

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void register(EmiRegistry registry) {
		registry.removeEmiStacks(s -> {
			Object key = s.getKey();
			Item item = s.getItemStack().getItem();
			if (key instanceof TagDependentIngredientItem tagDependent && tagDependent.shouldHide())
				return true;
			if (HiddenItems.getHiddenPredicate().test(item))
				return true;
			return key instanceof VirtualFluid;
		});

		registry.addGenericExclusionArea((screen, consumer) -> {
			if (screen instanceof AbstractSimiContainerScreen<?> simi) {
				simi.getExtraAreas().forEach(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
			}
		});

		registry.addRecipeHandler(AllMenuTypes.CRAFTING_BLUEPRINT.get(), new BlueprintTransferHandler());

		registry.addDragDropHandler(FilterScreen.class, new GhostIngredientHandler());
		registry.addDragDropHandler(AttributeFilterScreen.class, new GhostIngredientHandler());
		registry.addDragDropHandler(BlueprintScreen.class, new GhostIngredientHandler());
		registry.addDragDropHandler(LinkedControllerScreen.class, new GhostIngredientHandler());
		registry.addDragDropHandler(ScheduleScreen.class, new GhostIngredientHandler());
		registry.addDragDropHandler(RedstoneRequesterScreen.class, new GhostIngredientHandler());
		registry.addDragDropHandler(FactoryPanelSetItemScreen.class, new GhostIngredientHandler());

		registerGeneratedRecipes(registry);

		registry.setDefaultComparison(AllFluids.POTION.get().getSource(), c -> Comparison.compareNbt());

		ALL.forEach((id, category) -> registry.addCategory(category));

		registry.addWorkstation(MILLING, EmiStack.of(AllBlocks.MILLSTONE.get()));
		registry.addWorkstation(CRUSHING, EmiStack.of(AllBlocks.CRUSHING_WHEEL.get()));
		registry.addWorkstation(SANDPAPER_POLISHING, EmiStack.of(AllItems.SAND_PAPER.get()));
		registry.addWorkstation(SANDPAPER_POLISHING, EmiStack.of(AllItems.RED_SAND_PAPER.get()));
		registry.addWorkstation(PRESSING, EmiStack.of(AllBlocks.MECHANICAL_PRESS.get()));
		registry.addWorkstation(FAN_WASHING, FanEmiRecipe.getFan("fan_washing"));
		registry.addWorkstation(FAN_SMOKING, FanEmiRecipe.getFan("fan_smoking"));
		registry.addWorkstation(FAN_BLASTING, FanEmiRecipe.getFan("fan_blasting"));
		registry.addWorkstation(FAN_HAUNTING, FanEmiRecipe.getFan("fan_haunting"));
		registry.addWorkstation(MIXING, EmiStack.of(AllBlocks.MECHANICAL_MIXER.get()));
		registry.addWorkstation(MIXING, EmiStack.of(AllBlocks.BASIN.get()));
		registry.addWorkstation(AUTOMATIC_SHAPELESS, EmiStack.of(AllBlocks.MECHANICAL_MIXER.get()));
		registry.addWorkstation(AUTOMATIC_SHAPELESS, EmiStack.of(AllBlocks.BASIN.get()));
		registry.addWorkstation(AUTOMATIC_BREWING, EmiStack.of(AllBlocks.MECHANICAL_MIXER.get()));
		registry.addWorkstation(AUTOMATIC_BREWING, EmiStack.of(AllBlocks.BASIN.get()));
		registry.addWorkstation(SAWING, EmiStack.of(AllBlocks.MECHANICAL_SAW.get()));
		registry.addWorkstation(BLOCK_CUTTING, EmiStack.of(AllBlocks.MECHANICAL_SAW.get()));
		registry.addWorkstation(WOOD_CUTTING, EmiStack.of(AllBlocks.MECHANICAL_SAW.get()));
		registry.addWorkstation(PACKING, EmiStack.of(AllBlocks.MECHANICAL_PRESS.get()));
		registry.addWorkstation(PACKING, EmiStack.of(AllBlocks.BASIN.get()));
		registry.addWorkstation(AUTOMATIC_PACKING, EmiStack.of(AllBlocks.MECHANICAL_PRESS.get()));
		registry.addWorkstation(AUTOMATIC_PACKING, EmiStack.of(AllBlocks.BASIN.get()));
		registry.addWorkstation(DEPLOYING, EmiStack.of(AllBlocks.DEPLOYER.get()));
		registry.addWorkstation(SPOUT_FILLING, EmiStack.of(AllBlocks.SPOUT.get()));
		registry.addWorkstation(DRAINING, EmiStack.of(AllBlocks.ITEM_DRAIN.get()));
		registry.addWorkstation(AUTOMATIC_SHAPED, EmiStack.of(AllBlocks.MECHANICAL_CRAFTER.get()));
		registry.addWorkstation(MECHANICAL_CRAFTING, EmiStack.of(AllBlocks.MECHANICAL_CRAFTER.get()));

		RecipeManager manager = registry.getRecipeManager();

		List<MillingRecipe> millingRecipes = (List<MillingRecipe>) (List) manager.getAllRecipesFor(AllRecipeTypes.MILLING.getType());
		List<CrushingRecipe> crushingRecipes = (List<CrushingRecipe>) (List) manager.getAllRecipesFor(AllRecipeTypes.CRUSHING.getType());
		List<SmokingRecipe> smokingRecipes = manager.getAllRecipesFor(RecipeType.SMOKING);
		List<BlastingRecipe> blastingRecipes = manager.getAllRecipesFor(RecipeType.BLASTING);
		for (CrushingRecipe recipe : crushingRecipes) {
			registry.addRecipe(new CrushingEmiRecipe(recipe));
		}
		outer:
		for (MillingRecipe recipe : millingRecipes) {
			registry.addRecipe(new MillingEmiRecipe(recipe));
			for (CrushingRecipe crush : crushingRecipes) {
				if (doInputsMatch(recipe, crush)) {
					continue outer;
				}
			}
			registry.addRecipe(new CrushingEmiRecipe(recipe));
		}
		addAll(registry, AllRecipeTypes.SANDPAPER_POLISHING, PolishingEmiRecipe::new);
		addAll(registry, AllRecipeTypes.PRESSING, PressingEmiRecipe::new);
		for (SmokingRecipe recipe : smokingRecipes) {
			registry.addRecipe(new FanSmokingEmiRecipe(recipe));
		}
		outer:
		for (AbstractCookingRecipe recipe : manager.getAllRecipesFor(RecipeType.SMELTING)) {
			for (AbstractCookingRecipe smoking : smokingRecipes) {
				if (doInputsMatch(recipe, smoking)) {
					continue outer;
				}
			}
			for (AbstractCookingRecipe blasting : blastingRecipes) {
				if (doInputsMatch(recipe, blasting)) {
					continue outer;
				}
			}
			registry.addRecipe(new FanBlastingEmiRecipe(recipe));
		}
		for (AbstractCookingRecipe recipe : blastingRecipes) {
			registry.addRecipe(new FanBlastingEmiRecipe(recipe));
		}
		addAll(registry, AllRecipeTypes.SPLASHING, FanWashingEmiRecipe::new);
		addAll(registry, AllRecipeTypes.HAUNTING, FanHauntingEmiRecipe::new);
		addAll(registry, AllRecipeTypes.MIXING, MIXING, MixingEmiRecipe::new);
		for (CraftingRecipe recipe : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
			if (recipe instanceof ShapelessRecipe && !MechanicalPressBlockEntity.canCompress(recipe)
					&& !AllRecipeTypes.shouldIgnoreInAutomation(recipe) && recipe.getIngredients().size() > 1) {
				registry.addRecipe(new ShapelessEmiRecipe(AUTOMATIC_SHAPELESS, BasinRecipe.convertShapeless(recipe)));
			}
		}
		for (MixingRecipe recipe : PotionMixingRecipes.ALL) {
			registry.addRecipe(new MixingEmiRecipe(AUTOMATIC_BREWING, recipe));
		}
		addAll(registry, AllRecipeTypes.CUTTING, SawingEmiRecipe::new);
		for (CondensedBlockCuttingRecipe recipe : CondensedBlockCuttingRecipe
				.condenseRecipes(manager.getAllRecipesFor(RecipeType.STONECUTTING).stream()
				.filter(r -> !AllRecipeTypes.shouldIgnoreInAutomation(r)).toList(), "block_cutting")) {
			registry.addRecipe(new BlockCuttingEmiRecipe(BLOCK_CUTTING, recipe));
		}
		addAll(registry, AllRecipeTypes.COMPACTING, PackingEmiRecipe::new);
		for (CraftingRecipe recipe : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
			if (!(recipe instanceof MechanicalCraftingRecipe)
					&& MechanicalPressBlockEntity.canCompress(recipe)
					&& !AllRecipeTypes.shouldIgnoreInAutomation(recipe)) {
				registry.addRecipe(new AutomaticPackingEmiRecipe(BasinRecipe.convertShapeless(recipe)));
			}
		}
		addAll(registry, AllRecipeTypes.DEPLOYING, DeployingEmiRecipe::new);
		addAll(registry, AllRecipeTypes.SANDPAPER_POLISHING, DeployingEmiRecipe::fromSandpaper);
		addAll(registry, AllRecipeTypes.ITEM_APPLICATION, DeployingEmiRecipe::fromItemApplication);

		for (ConversionRecipe recipe : MysteriousConversionEmiRecipe.RECIPES) {
			registry.addRecipe(new MysteriousConversionEmiRecipe(recipe));
		}
		addAll(registry, AllRecipeTypes.FILLING, SpoutEmiRecipe::new);
		addAll(registry, AllRecipeTypes.EMPTYING, DrainEmiRecipe::new);
		addAll(registry, AllRecipeTypes.SEQUENCED_ASSEMBLY, SequencedAssemblyEmiRecipe::new);
		addAll(registry, AllRecipeTypes.ITEM_APPLICATION, ManualItemApplicationEmiRecipe::new);
		addAll(registry, AllRecipeTypes.MECHANICAL_CRAFTING, MECHANICAL_CRAFTING, MechanicalCraftingEmiRecipe::new);

		// In World Interaction recipes
		addLavaCollision(registry, AllFluids.HONEY, AllPaletteStoneTypes.LIMESTONE);
		addLavaCollision(registry, AllFluids.CHOCOLATE, AllPaletteStoneTypes.SCORIA);

		// Introspective recipes based on present stacks need to make sure
		// all stacks are populated by other plugins
		registry.addDeferredRecipes(this::addDeferredRecipes);
	}

	@SuppressWarnings("unchecked")
	private <T extends Recipe<?>> void addAll(EmiRegistry registry, AllRecipeTypes type, Function<T, EmiRecipe> constructor) {
		for (T recipe : (List<T>) registry.getRecipeManager().getAllRecipesFor(type.getType())) {
			registry.addRecipe(constructor.apply(recipe));
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Recipe<?>> void addAll(EmiRegistry registry, AllRecipeTypes type, EmiRecipeCategory category,
			BiFunction<EmiRecipeCategory, T, EmiRecipe> constructor) {
		for (T recipe : (List<T>) registry.getRecipeManager().getAllRecipesFor(type.getType())) {
			registry.addRecipe(constructor.apply(category, recipe));
		}
	}

	/**
	 * Register an In World Interaction recipe for a fluid colliding with lava.
	 */
	private void addLavaCollision(EmiRegistry registry, FluidEntry<?> fluid, AllPaletteStoneTypes outputType) {
		EmiStack lava = EmiStack.of(Fluids.LAVA, FluidConstants.BUCKET);
		lava = lava.setRemainder(lava);

		EmiStack fluidStack = EmiStack.of(fluid.get().getSource(), FluidConstants.BUCKET);
		fluidStack = fluidStack.setRemainder(fluidStack);

		Block block = outputType.getBaseBlock().get();
		EmiStack output = EmiStack.of(block);
		String blockName = BuiltInRegistries.BLOCK.getKey(block).getPath();

		registry.addRecipe(
				EmiWorldInteractionRecipe.builder()
						.id(synthetic("fluid_interaction/" + blockName))
						.leftInput(fluidStack)
						.rightInput(lava, false)
						.output(output)
						.build()
		);
	}

	private void addDeferredRecipes(Consumer<EmiRecipe> consumer) {
		List<Fluid> fluids = EmiApi.getIndexStacks().stream()
			.filter(s -> s.getKey() instanceof Fluid)
			.map(s -> (Fluid) s.getKey())
			.distinct()
			.toList();
		for (EmiStack stack : EmiApi.getIndexStacks()) {
			if (stack.getKey() instanceof Item i) {
				ItemStack is = stack.getItemStack();
				if (i instanceof PotionItem) {
					FluidStack potion = PotionFluidHandler.getFluidFromPotionItem(is);
					Ingredient bottle = Ingredient.of(Items.GLASS_BOTTLE);
					ResourceLocation iid = BuiltInRegistries.ITEM.getKey(i);
					ResourceLocation pid = BuiltInRegistries.POTION.getKey(PotionUtils.getPotion(is));
					consumer.accept(new SpoutEmiRecipe(new ProcessingRecipeBuilder<>(FillingRecipe::new,
						synthetic("potion_filling/" + pid.getNamespace() + "/" + pid.getPath()
							+ "/from/" + iid.getNamespace() + "/" + iid.getPath()))
								.withItemIngredients(bottle)
								.withFluidIngredients(FluidIngredient.fromFluidStack(potion))
								.withSingleItemOutput(is.copy())
								.build()));
					consumer.accept(new DrainEmiRecipe(new ProcessingRecipeBuilder<>(EmptyingRecipe::new,
						synthetic("potion_draining/" + pid.getNamespace() + "/" + pid.getPath()
							+ "/from/" + iid.getNamespace() + "/" + iid.getPath()))
								.withItemIngredients(Ingredient.of(is))
								.withFluidOutputs(potion)
								.withSingleItemOutput(new ItemStack(Items.GLASS_BOTTLE))
								.build()));
					continue;
				}
				for (Fluid fluid : fluids) {
					if (i == Items.GLASS_BOTTLE && fluid == Fluids.WATER) {
						continue;
					}
					// This will miss fluid containers that hold a minimum of over 1000 L, but perhaps that's preferable.
					FluidStack fs = new FluidStack(fluid, FluidConstants.BUCKET);
					ItemStack copy = is.copy();
					MutableContainerItemContext ctx = new MutableContainerItemContext(copy);
					Storage<FluidVariant> storage = ctx.find(FluidStorage.ITEM);
					if (storage != null && GenericItemFilling.isFluidHandlerValid(copy, storage)) {
						long inserted = 0;
						try (Transaction t = TransferUtil.getTransaction()) {
							inserted = storage.insert(fs.getType(), fs.getAmount(), t);
							t.commit();
						}
						fs.setAmount(inserted);
						ItemStack container = ctx.getItemVariant().toStack(ItemHelper.truncateLong(ctx.getAmount()));
						if (inserted != 0 && !ItemStack.isSameItemSameTags(container, copy) && !container.isEmpty()) {
							Ingredient bucket = Ingredient.of(is);
							ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(is.getItem());
							ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fs.getFluid());
							consumer.accept(new SpoutEmiRecipe(new ProcessingRecipeBuilder<>(FillingRecipe::new,
							synthetic("filling/" + itemId.getNamespace() + "/" + itemId.getPath()
									+ "/with/" + fluidId.getNamespace() + "/" + fluidId.getPath()))
								.withItemIngredients(bucket)
								.withFluidIngredients(FluidIngredient.fromFluidStack(fs))
								.withSingleItemOutput(container)
								.build()));
						}
					}
				}
				ItemStack copy = is.copy();
				MutableContainerItemContext ctx = new MutableContainerItemContext(copy);
				Storage<FluidVariant> storage = ctx.find(FluidStorage.ITEM);
				if (storage != null) {
					FluidStack extracted = TransferUtil.extractAnyFluid(storage, FluidConstants.BUCKET);
					ItemStack result = ctx.getItemVariant().toStack(ItemHelper.truncateLong(ctx.getAmount()));
					if (!extracted.isEmpty() && !result.isEmpty()) {
						ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(is.getItem());
						ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(extracted.getFluid());
						consumer.accept(new DrainEmiRecipe(new ProcessingRecipeBuilder<>(EmptyingRecipe::new,
							synthetic("draining/" + itemId.getNamespace() + "/" + itemId.getPath()
								+ "/from/" + fluidId.getNamespace() + "/" + fluidId.getPath()))
							.withItemIngredients(Ingredient.of(is))
							.withFluidOutputs(extracted)
							.withSingleItemOutput(result)
							.build()));
					}
				}
			}
		}
	}

	public void registerGeneratedRecipes(EmiRegistry registry) {
		ToolboxColoringRecipeMaker.createRecipes().forEach(r -> {
			ItemStack toolbox = null;
			ItemStack dye = null;
			for (Ingredient ingredient : r.getIngredients()) {
				for (ItemStack stack : ingredient.getItems()) {
					if (toolbox == null && stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ToolboxBlock) {
						toolbox = stack;
					} else if (dye == null && stack.getItem() instanceof DyeItem) {
						dye = stack;
					}
					if (toolbox != null && dye != null) break;
				}
			}
			if (toolbox == null || dye == null) return;
			ResourceLocation toolboxId = BuiltInRegistries.ITEM.getKey(toolbox.getItem());
			ResourceLocation dyeId = BuiltInRegistries.ITEM.getKey(dye.getItem());
			String recipeName = "toolboxes/%s/%s/%s/%s"
					.formatted(toolboxId.getNamespace(), toolboxId.getPath(), dyeId.getNamespace(), dyeId.getPath());
			registry.addRecipe(new EmiCraftingRecipe(
					r.getIngredients().stream().map(EmiIngredient::of).toList(),
					CreateEmiRecipe.getResultEmi(r), synthetic(recipeName)));
		});
		// for EMI we don't do this since it already has a category, World Interaction
//		LogStrippingFakeRecipes.createRecipes().forEach(r -> {
//			registry.addRecipe(new ItemApplicationEmiRecipe(r));
//		});
	}

	public static boolean doInputsMatch(Recipe<?> a, Recipe<?> b) {
		if (!a.getIngredients().isEmpty() && !b.getIngredients().isEmpty()) {
			ItemStack[] matchingStacks = a.getIngredients().get(0).getItems();
			if (matchingStacks.length != 0) {
				if (b.getIngredients().get(0).test(matchingStacks[0])) {
					return true;
				}
			}
		}
		return false;
	}

	public static ResourceLocation synthetic(String path) {
		if (path.startsWith("/"))
			throw new IllegalArgumentException("Starting slash is added automatically");
		// EMI recommends starting synthetic IDs with a slash so that they can't possibly conflict with data packs.
		return Create.asResource('/' + path);
	}

	public static ResourceLocation synthetic(ResourceLocation id) {
		return id.withPrefix("/");
	}

	public static ResourceLocation syntheticOf(String prefix, ResourceLocation base) {
		return synthetic(prefix + '/' + base.getNamespace() + '/' + base.getPath());
	}

	private static EmiRecipeCategory register(String name, EmiRenderable icon) {
		ResourceLocation id = Create.asResource(name);
		EmiRecipeCategory category = new EmiRecipeCategory(id, icon);
		ALL.put(id, category);
		return category;
	}
}
