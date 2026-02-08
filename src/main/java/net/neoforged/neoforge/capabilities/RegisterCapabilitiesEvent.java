package net.neoforged.neoforge.capabilities;

import java.util.function.BiFunction;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RegisterCapabilitiesEvent {
	public <T, C, BE extends BlockEntity> void registerBlockEntity(Object capability, BlockEntityType<BE> type,
		BiFunction<BE, C, T> provider) {
		// Fabric registrations are handled elsewhere; this is a compatibility no-op.
	}
}
