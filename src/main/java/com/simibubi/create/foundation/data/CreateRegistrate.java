package com.simibubi.create.foundation.data;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.CustomRenderedItems;
import com.simibubi.create.foundation.mixin.accessor.AbstractRegistrateAccessor;
import com.simibubi.create.impl.registrate.CreateRegistrateRegistrationCallbackImpl;
import com.simibubi.create.impl.registrate.CreateRegistrateRegistrationCallbackImpl.CallbackImpl;

import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;

import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.CreateClient;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.api.registry.registrate.SimpleBuilder;
import com.simibubi.create.content.decoration.encasing.CasingConnectivity;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.providers.RegistrateDataProvider;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import io.github.fabricators_of_create.porting_lib.util.DeferredHolder;

public class CreateRegistrate extends AbstractRegistrate<CreateRegistrate> {
	private static final Map<RegistryEntry<?, ?>, ResourceKey<CreativeModeTab>> TAB_LOOKUP = Collections.synchronizedMap(new IdentityHashMap<>());

	@Nullable
	protected Function<Item, TooltipModifier> currentTooltipModifierFactory;
	@Nullable
	protected ResourceKey<CreativeModeTab> currentTab;

	protected CreateRegistrate(String modid) {
		super(modid);
	}

	public static CreateRegistrate create(String modid) {
		return new CreateRegistrate(modid);
	}

	public static boolean isInCreativeTab(RegistryEntry<?, ?> entry, ResourceKey<CreativeModeTab> tab) {
		return TAB_LOOKUP.get(entry) == tab;
	}

	public CreateRegistrate setTooltipModifierFactory(@Nullable Function<Item, TooltipModifier> factory) {
		currentTooltipModifierFactory = factory;
		return self();
	}

	@Nullable
	public Function<Item, TooltipModifier> getTooltipModifierFactory() {
		return currentTooltipModifierFactory;
	}

	public CreateRegistrate setCreativeTab(ResourceKey<CreativeModeTab> tab) {
		this.currentTab = tab;
		return this;
	}

	@Nullable
	public ResourceKey<CreativeModeTab> getCreativeTab() {
		return currentTab;
	}

	@Override
	protected <R, T extends R> RegistryEntry<R, T> accept(String name, ResourceKey<? extends Registry<R>> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator, NonNullFunction<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory) {
		RegistryEntry<R, T> entry = super.accept(name, type, builder, creator, entryFactory);
		if (type.equals(Registries.ITEM) && currentTooltipModifierFactory != null) {
			// grab the factory here for the lambda, it can change between now and registration
			Function<Item, TooltipModifier> factory = currentTooltipModifierFactory;
			this.addRegisterCallback(name, Registries.ITEM, item -> {
				TooltipModifier modifier = factory.apply(item);
				TooltipModifier.REGISTRY.register(item, modifier);
			});
		}
		if (currentTab != null)
			TAB_LOOKUP.put(entry, currentTab);

		for (CallbackImpl<?> callback : CreateRegistrateRegistrationCallbackImpl.CALLBACKS_VIEW) {
			String modId = callback.id().getNamespace();
			String entryId = callback.id().getPath();
			if (callback.registry().equals(type) && getModid().equals(modId) && name.equals(entryId)) {
				//noinspection unchecked
				((Consumer<T>) callback.callback()).accept(entry.get());
			}
		}

		return entry;
	}

	@Override
	public <T extends BlockEntity> CreateBlockEntityBuilder<T, CreateRegistrate> blockEntity(String name,
																							 BlockEntityFactory<T> factory) {
		return blockEntity(self(), name, factory);
	}

	@Override
	public <T extends BlockEntity, P> CreateBlockEntityBuilder<T, P> blockEntity(P parent, String name,
																				 BlockEntityFactory<T> factory) {
		return (CreateBlockEntityBuilder<T, P>) entry(name,
			(callback) -> CreateBlockEntityBuilder.create(this, parent, name, callback, factory));
	}

	@Override
	public <T extends Entity> CreateEntityBuilder<T, CreateRegistrate> entity(String name,
																			  EntityType.EntityFactory<T> factory, MobCategory classification) {
		return this.entity(self(), name, factory, classification);
	}

	@Override
	public <T extends Entity, P> CreateEntityBuilder<T,  P> entity(P parent, String name,
																  EntityType.EntityFactory<T> factory, MobCategory classification) {
		return (CreateEntityBuilder<T, P>) this.entry(name, (callback) -> {
			return CreateEntityBuilder.create(this, parent, name, callback, factory, classification);
		});
	}

	// custom types

	public <T extends MountedItemStorageType<?>> SimpleBuilder<MountedItemStorageType<?>, T, CreateRegistrate> mountedItemStorage(String name, Supplier<T> supplier) {
		return this.entry(name, callback -> new SimpleBuilder<>(
			this, this, name, callback, CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE, supplier
		).byBlock(MountedItemStorageType.REGISTRY));
	}

	public <T extends MountedFluidStorageType<?>> SimpleBuilder<MountedFluidStorageType<?>, T, CreateRegistrate> mountedFluidStorage(String name, Supplier<T> supplier) {
		return this.entry(name, callback -> new SimpleBuilder<>(
			this, this, name, callback, CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE, supplier
		).byBlock(MountedFluidStorageType.REGISTRY));
	}

	public <T extends DisplaySource> SimpleBuilder<DisplaySource, T, CreateRegistrate> displaySource(String name, Supplier<T> supplier) {
		return this.entry(name, callback -> new SimpleBuilder<>(
			this, this, name, callback, CreateRegistries.DISPLAY_SOURCE, supplier
		).byBlock(DisplaySource.BY_BLOCK).byBlockEntity(DisplaySource.BY_BLOCK_ENTITY));
	}

	public <T extends DisplayTarget> SimpleBuilder<DisplayTarget, T, CreateRegistrate> displayTarget(String name, Supplier<T> supplier) {
		return this.entry(name, callback -> new SimpleBuilder<>(
			this, this, name, callback, CreateRegistries.DISPLAY_TARGET, supplier
		).byBlock(DisplayTarget.BY_BLOCK).byBlockEntity(DisplayTarget.BY_BLOCK_ENTITY));
	}

	/* Palettes */

	public <T extends Block> BlockBuilder<T, CreateRegistrate> paletteStoneBlock(String name,
																				 NonNullFunction<Properties, T> factory, NonNullSupplier<Block> propertiesFrom, boolean worldGenStone,
																				 boolean hasNaturalVariants) {
		BlockBuilder<T, CreateRegistrate> builder = super.block(name, factory).initialProperties(propertiesFrom)
			.transform(pickaxeOnly())
			.blockstate(hasNaturalVariants ? BlockStateGen.naturalStoneTypeBlock(name) : (c, p) -> {
				final String location = "block/palettes/stone_types/" + c.getName();
				p.simpleBlock(c.get(), p.models()
					.cubeAll(c.getName(), p.modLoc(location)));
			})
			.tag(BlockTags.DRIPSTONE_REPLACEABLE)
			.tag(BlockTags.AZALEA_ROOT_REPLACEABLE)
			.tag(BlockTags.MOSS_REPLACEABLE)
			.tag(BlockTags.LUSH_GROUND_REPLACEABLE)
			.item()
			.model((c, p) -> p.cubeAll(c.getName(),
				p.modLoc(hasNaturalVariants ? "block/palettes/stone_types/natural/" + name + "_1"
					: "block/palettes/stone_types/" + c.getName())))
			.build();
		return builder;
	}

	public BlockBuilder<Block, CreateRegistrate> paletteStoneBlock(String name, NonNullSupplier<Block> propertiesFrom,
																   boolean worldGenStone, boolean hasNaturalVariants) {
		return paletteStoneBlock(name, Block::new, propertiesFrom, worldGenStone, hasNaturalVariants);
	}

	/* Fluids */

	public <T extends SimpleFlowableFluid> FluidBuilder<T, CreateRegistrate> virtualFluid(String name,
//		BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory,
																						  NonNullFunction<SimpleFlowableFluid.Properties, T> sourceFactory,
																						  NonNullFunction<SimpleFlowableFluid.Properties, T> flowingFactory) {
		return entry(name,
			c -> new VirtualFluidBuilder<>(self(), self(), name, c, ResourceLocation.fromNamespaceAndPath(getModid(), "fluid/" + name + "_still"),
				ResourceLocation.fromNamespaceAndPath(getModid(), "fluid/" + name + "_flow"), sourceFactory, flowingFactory));
	}

	public <T extends SimpleFlowableFluid> FluidBuilder<T, CreateRegistrate> virtualFluid(String name,
																						ResourceLocation still, ResourceLocation flow,
																						NonNullFunction<SimpleFlowableFluid.Properties, T> sourceFactory, NonNullFunction<SimpleFlowableFluid.Properties, T> flowingFactory) {
		return entry(name, c -> new VirtualFluidBuilder<>(self(), self(), name, c, still, flow, sourceFactory, flowingFactory));
	}

	public FluidBuilder<VirtualFluid, CreateRegistrate> virtualFluid(String name) {
		return entry(name,
			c -> new VirtualFluidBuilder<>(self(), self(), name, c, ResourceLocation.fromNamespaceAndPath(getModid(), "fluid/" + name + "_still"),
				ResourceLocation.fromNamespaceAndPath(getModid(), "fluid/" + name + "_flow"), /*null, */VirtualFluid::createSource, VirtualFluid::createFlowing));
	}

	public FluidBuilder<VirtualFluid, CreateRegistrate> virtualFluid(String name, ResourceLocation still, ResourceLocation flow) {
		return entry(name,
				c -> new VirtualFluidBuilder<>(self(), self(), name, c, still,
						flow, VirtualFluid::createSource, VirtualFluid::createFlowing));
	}

	public FluidBuilder<SimpleFlowableFluid.Flowing, CreateRegistrate> standardFluid(String name) {
		return fluid(name, ResourceLocation.fromNamespaceAndPath(getModid(), "fluid/" + name + "_still"), ResourceLocation.fromNamespaceAndPath(getModid(), "fluid/" + name + "_flow"));
	}

/*
	public FluidBuilder<ForgeFlowingFluid.Flowing, CreateRegistrate> standardFluid(String name,
																				   FluidBuilder.FluidTypeFactory typeFactory) {
		return fluid(name, ResourceLocation.fromNamespaceAndPath(getModid(), "fluid/" + name + "_still"), ResourceLocation.fromNamespaceAndPath(getModid(), "fluid/" + name + "_flow"),
			typeFactory);
	}

	public static FluidType defaultFluidType(FluidType.Properties properties, ResourceLocation stillTexture,
											 ResourceLocation flowingTexture) {
		return new FluidType(properties) {
			@Override
			public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
				consumer.accept(new IClientFluidTypeExtensions() {
					@Override
					public ResourceLocation getStillTexture() {
						return stillTexture;
					}

					@Override
					public ResourceLocation getFlowingTexture() {
						return flowingTexture;
					}
				});
			}
		};
	}
*/
	/* Util */

	public static <T extends Block> NonNullConsumer<? super T> casingConnectivity(
		BiConsumer<T, CasingConnectivity> consumer) {
		return entry -> CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> ClientHooks.registerCasingConnectivity(entry, consumer));
	}

	public static <T extends Block, M> NonNullConsumer<? super T> blockModel(
		Supplier<? extends NonNullFunction<M, ? extends M>> func) {
		return entry -> CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> ClientHooks.registerBlockModel(entry, func));
	}

	public static <T extends Item, M> NonNullConsumer<? super T> itemModel(
		Supplier<? extends NonNullFunction<M, ? extends M>> func) {
		return entry -> CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> ClientHooks.registerItemModel(entry, func));
	}

	public static NonNullConsumer<? super Block> connectedTextures(
		Supplier<?> behavior) {
		return entry -> CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> ClientHooks.registerCTBehaviour(entry, behavior));
	}

	public static <T extends Item, P> NonNullUnaryOperator<ItemBuilder<T, P>> customRenderedItem(
		Supplier<? extends Supplier<?>> supplier) {
		return b -> {
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> ClientHooks.customRenderedItem(b, supplier));
			return b;
		};
	}

	@Environment(EnvType.CLIENT)
	private static final class ClientHooks {
		private ClientHooks() {
		}

		private static <T extends Block> void registerCasingConnectivity(T entry,
																		 BiConsumer<T, CasingConnectivity> consumer) {
			consumer.accept(entry, CreateClient.CASING_CONNECTIVITY);
		}

		@SuppressWarnings("unchecked")
		private static <M> void registerBlockModel(Block entry,
												   Supplier<? extends NonNullFunction<M, ? extends M>> func) {
			NonNullFunction<BakedModel, ? extends BakedModel> modelFunc =
				(NonNullFunction<BakedModel, ? extends BakedModel>) func.get();
			CreateClient.MODEL_SWAPPER.getCustomBlockModels()
				.register(RegisteredObjectsHelper.getKeyOrThrow(entry), modelFunc);
		}

		@SuppressWarnings("unchecked")
		private static <M> void registerItemModel(Item entry,
												  Supplier<? extends NonNullFunction<M, ? extends M>> func) {
			NonNullFunction<BakedModel, ? extends BakedModel> modelFunc =
				(NonNullFunction<BakedModel, ? extends BakedModel>) func.get();
			CreateClient.MODEL_SWAPPER.getCustomItemModels()
				.register(RegisteredObjectsHelper.getKeyOrThrow(entry), modelFunc);
		}

		private static void registerCTBehaviour(Block entry, Supplier<?> behaviorSupplier) {
			ConnectedTextureBehaviour behavior = (ConnectedTextureBehaviour) behaviorSupplier.get();
			CreateClient.MODEL_SWAPPER.getCustomBlockModels()
				.register(RegisteredObjectsHelper.getKeyOrThrow(entry), model -> new CTModel(model, behavior));
		}

		private static <T extends Item, P> void customRenderedItem(ItemBuilder<T, P> b,
																   Supplier<? extends Supplier<?>> supplier) {
			b.onRegister(new CustomRendererRegistrationHelper(supplier));
		}

		private record CustomRendererRegistrationHelper(Supplier<? extends Supplier<?>> supplier) implements NonNullConsumer<Item> {
			@Override
			public void accept(Item entry) {
				CustomRenderedItemModelRenderer renderer = (CustomRenderedItemModelRenderer) supplier.get().get();
				BuiltinItemRendererRegistry.INSTANCE.register(entry, renderer);
				CustomRenderedItems.register(entry);
			}
		}
	}

	@ApiStatus.Internal
	public RegistrateDataProvider setDataProvider(RegistrateDataProvider provider) {
		((AbstractRegistrateAccessor) this).create$setProvider(provider);
		return provider;
	}
}
