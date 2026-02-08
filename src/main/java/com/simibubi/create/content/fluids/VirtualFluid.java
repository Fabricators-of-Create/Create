package com.simibubi.create.content.fluids;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import com.tterrag.registrate.fabric.SimpleFlowableFluid;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class VirtualFluid extends SimpleFlowableFluid {

	private static final Field STILL_SUPPLIER_FIELD = resolveSupplierField("still");
	private static final Field FLOWING_SUPPLIER_FIELD = resolveSupplierField("flowing");

	public static VirtualFluid createSource(Properties properties) {
		return new VirtualFluid(properties, true);
	}

	public static VirtualFluid createFlowing(Properties properties) {
		return new VirtualFluid(properties, false);
	}

	private final boolean source;
	private final Supplier<? extends Fluid> stillSupplier;
	private final Supplier<? extends Fluid> flowingSupplier;

	public VirtualFluid(Properties properties, boolean source) {
		super(properties);
		this.source = source;
		this.stillSupplier = readSupplier(STILL_SUPPLIER_FIELD, this);
		this.flowingSupplier = readSupplier(FLOWING_SUPPLIER_FIELD, this);
	}

	@Override
	public Fluid getSource() {
		if (source) {
			return this;
		}
		return stillSupplier.get();
	}

	@Override
	public Fluid getFlowing() {
		if (source) {
			return flowingSupplier.get();
		}
		return this;
	}

	@Override
	public Item getBucket() {
		return Items.AIR;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean isSource(FluidState p_207193_1_) {
		return source;
	}

	@Override
	public int getAmount(FluidState p_207192_1_) {
		return 0;
	}

	private static Field resolveSupplierField(String name) {
		try {
			Field field = SimpleFlowableFluid.class.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not resolve SimpleFlowableFluid supplier field: " + name, e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Supplier<? extends Fluid> readSupplier(Field field, VirtualFluid fluid) {
		try {
			return (Supplier<? extends Fluid>) field.get(fluid);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not read SimpleFlowableFluid supplier field: " + field.getName(), e);
		}
	}
}
