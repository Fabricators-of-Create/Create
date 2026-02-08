package com.simibubi.create.content.contraptions.actors.psi;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;
import com.simibubi.create.foundation.utility.fabric.ListeningStorageView;
import com.simibubi.create.infrastructure.fabric.ProcessingIterator;
import com.simibubi.create.infrastructure.fabric.transfer.TransactionSuccessCallback;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PortableItemInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity implements SidedStorageBlockEntity {

	protected InterfaceItemHandler capability;

	public PortableItemInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		capability = createEmptyHandler();
	}

	public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
				net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
				AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE.get(),
				(be, context) -> be.capability
		);
	}

	@Override
	public void startTransferringTo(Contraption contraption, float distance) {
		capability.setWrapped(contraption.getStorage().getAllItems());
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void stopTransferring() {
		capability.setWrapped(Storage.empty());
		super.stopTransferring();
	}

	private InterfaceItemHandler createEmptyHandler() {
		return new InterfaceItemHandler(Storage.empty());
	}

	@Override
	protected void invalidateCapability() {
		capability.setWrapped(Storage.empty());
	}

	@Nullable
	@Override
	public Storage<ItemVariant> getItemStorage(@Nullable Direction face) {
		return capability;
	}

	class InterfaceItemHandler extends ItemHandlerWrapper {

		public InterfaceItemHandler(Storage<ItemVariant> wrapped) {
			super(wrapped);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;
			long extracted = super.extract(resource, maxAmount, transaction);
			if (extracted != 0) {
				TransactionSuccessCallback.register(transaction, PortableItemInterfaceBlockEntity.this::onContentTransferred);
			}
			return extracted;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;
			long inserted = super.insert(resource, maxAmount, transaction);
			if (inserted != 0) {
				TransactionSuccessCallback.register(transaction, PortableItemInterfaceBlockEntity.this::onContentTransferred);
			}
			return inserted;
		}

		@Override
		public Iterator<StorageView<ItemVariant>> iterator() {
			return new ProcessingIterator<>(super.iterator(), this::listen);
		}

		public <T> StorageView<T> listen(StorageView<T> view) {
			return new ListeningStorageView<>(view, PortableItemInterfaceBlockEntity.this::onContentTransferred);
		}

		private void setWrapped(Storage<ItemVariant> wrapped) {
			this.wrapped = wrapped;
		}
	}
}
