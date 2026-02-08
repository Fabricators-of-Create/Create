package com.simibubi.create.infrastructure.fabric.transfer.fluid;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Mutable combination of a fluid and an amount, paralleling {@link ItemStack}.
 */
public final class FluidStack implements DataComponentHolder {
	private static final Logger logger = LogUtils.getLogger();
	private static final Codec<Fluid> FLUID_NON_EMPTY_CODEC = BuiltInRegistries.FLUID.byNameCodec()
		.validate(fluid -> fluid == Fluids.EMPTY
			? DataResult.error(() -> "Fluid must not be minecraft:empty")
			: DataResult.success(fluid));

	public static final FluidStack EMPTY = new FluidStack(FluidVariant.blank(), 0);

	public static final Codec<FluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		FLUID_NON_EMPTY_CODEC.fieldOf("fluid").forGetter(FluidStack::getFluid),
		DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStack::getComponentsPatch),
		Codec.LONG.fieldOf("amount").forGetter(FluidStack::getAmount)
	).apply(instance, FluidStack::fromCodecData));

	public static final Codec<FluidStack> OPTIONAL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fluid").forGetter(stack -> stack.isEmpty()
			? Optional.empty()
			: Optional.of(stack.getFluid())),
		DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStack::getComponentsPatch),
		Codec.LONG.optionalFieldOf("amount", 0L).forGetter(FluidStack::getAmount)
	).apply(instance, (fluid, components, amount) -> fluid
		.filter($ -> amount > 0)
		.map(value -> fromCodecData(value, components, amount))
		.orElse(EMPTY)));

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.registry(Registries.FLUID), FluidStack::getFluid,
		DataComponentPatch.STREAM_CODEC, FluidStack::getComponentsPatch,
		ByteBufCodecs.VAR_LONG, FluidStack::getAmount,
		FluidStack::fromNetworkData
	);

	private final FluidVariant variant;
	private long amount;

	public FluidStack(FluidVariant variant, long amount) {
		this.variant = variant;
		this.setAmount(amount);
	}

	public FluidStack(Fluid fluid, long amount) {
		this(TransferUtil.fluidVariantOf(fluid), amount);
	}

	public FluidStack(Holder<Fluid> fluid, long amount, DataComponentPatch components) {
		this(TransferUtil.fluidVariantOf(fluid.value(), components), amount);
	}

	public FluidStack(StorageView<FluidVariant> view) {
		this(view.getResource(), view.getAmount());
	}

	public FluidStack(ResourceAmount<FluidVariant> resource) {
		this(resource.resource(), resource.amount());
	}

	private static FluidStack fromCodecData(Fluid fluid, DataComponentPatch components, long amount) {
		return components.isEmpty()
			? new FluidStack(fluid, amount)
			: new FluidStack(BuiltInRegistries.FLUID.wrapAsHolder(fluid), amount, components);
	}

	private static FluidStack fromNetworkData(Fluid fluid, DataComponentPatch components, long amount) {
		if (fluid == Fluids.EMPTY || amount <= 0) {
			return EMPTY;
		}
		return fromCodecData(fluid, components, amount);
	}

	public FluidVariant getVariant() {
		return this.variant;
	}

	public Fluid getFluid() {
		return this.variant.getFluid();
	}

	@Override
	public DataComponentMap getComponents() {
		return !this.isEmpty() ? this.variant.getComponentMap() : DataComponentMap.EMPTY;
	}

	public DataComponentPatch getComponentsPatch() {
		return !this.isEmpty() ? this.variant.getComponents() : DataComponentPatch.EMPTY;
	}

	public long getAmount() {
		return this.amount;
	}

	public void setAmount(long amount) {
		this.amount = Math.max(amount, 0);
	}

	public void shrink(long amount) {
		this.setAmount(this.amount - amount);
	}

	public Component getHoverName() {
		return FluidVariantAttributes.getName(this.variant);
	}

	public boolean isEmpty() {
		return this.variant.isBlank() || this.amount <= 0;
	}

	public FluidStack copy() {
		return this.isEmpty() ? EMPTY : new FluidStack(this.variant, this.amount);
	}

	public FluidStack copyWithAmount(long amount) {
		FluidStack copy = this.copy();
		if (!copy.isEmpty()) {
			copy.setAmount(amount);
		}
		return copy;
	}

	public Tag save(Provider registries, Tag output) {
		if (this.isEmpty()) {
			throw new IllegalStateException("Cannot encode empty FluidStack");
		} else {
			RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
			return CODEC.encode(this, ops, output).getOrThrow();
		}
	}

	public Tag save(Provider registries) {
		if (this.isEmpty()) {
			throw new IllegalStateException("Cannot encode empty FluidStack");
		} else {
			RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
			return CODEC.encodeStart(ops, this).getOrThrow();
		}
	}

	public Tag saveOptional(Provider registries) {
		return this.isEmpty() ? new CompoundTag() : this.save(registries, new CompoundTag());
	}

	public boolean isComponentsPatchEmpty() {
		return !this.variant.hasComponents();
	}

	public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
		if (!first.variant.isOf(second.variant.getFluid()))
			return false;

		return first.variant.componentsMatch(second.variant.getComponents());
	}

	public static Optional<FluidStack> parse(HolderLookup.Provider registries, Tag tag) {
		RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
		return CODEC.parse(ops, tag).resultOrPartial(
			error -> logger.error("Failed to read invalid fluid: {}", error)
		);
	}

	public static FluidStack parseOptional(HolderLookup.Provider registries, CompoundTag tag) {
		return tag.isEmpty() ? EMPTY : parse(registries, tag).orElse(EMPTY);
	}

	public static FluidStack of(@Nullable ResourceAmount<FluidVariant> resource) {
		return resource == null ? EMPTY : new FluidStack(resource);
	}
}
