package com.simibubi.create.compat.recipeViewerCommon;

import java.util.function.Supplier;

public record SequencedAssemblySubCategoryType(Supplier<Supplier<?>> jei,
											   Supplier<Supplier<?>> rei,
											   Supplier<Supplier<?>> emi) {

	private static Supplier<Supplier<?>> missingProvider() {
		return () -> () -> null;
	}

	public static final SequencedAssemblySubCategoryType PRESSING = new SequencedAssemblySubCategoryType(
		missingProvider(),
		missingProvider(),
		missingProvider()
	);
	public static final SequencedAssemblySubCategoryType SPOUTING = new SequencedAssemblySubCategoryType(
		missingProvider(),
		missingProvider(),
		missingProvider()
	);
	public static final SequencedAssemblySubCategoryType DEPLOYING = new SequencedAssemblySubCategoryType(
		missingProvider(),
		missingProvider(),
		missingProvider()
	);
	public static final SequencedAssemblySubCategoryType CUTTING = new SequencedAssemblySubCategoryType(
		missingProvider(),
		missingProvider(),
		missingProvider()
	);
}
