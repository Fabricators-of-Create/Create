package com.simibubi.create.content.logistics.packager.repackager;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.crate.BottomlessItemHandler;
import com.simibubi.create.content.logistics.packager.PackageDefragmenter;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import com.simibubi.create.content.logistics.packager.PackagingRequest;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.TransactionSuccessCallback;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RepackagerBlockEntity extends PackagerBlockEntity {

	public PackageDefragmenter defragmenter;

	public RepackagerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		defragmenter = new PackageDefragmenter();
	}

	public boolean unwrapBox(ItemStack box, TransactionContext ctx) {
		if (animationTicks > 0)
			return false;

		Storage<ItemVariant> targetInv = targetInventory.getInventory();
		if (targetInv == null || targetInv instanceof PackagerItemHandler)
			return false;

		boolean targetIsCreativeCrate = targetInv instanceof BottomlessItemHandler;

		long insertable = StorageUtil.simulateInsert(targetInv, ItemVariant.of(box), box.getCount(), null);
		boolean anySpace = insertable > 0;

		if (!targetIsCreativeCrate && !anySpace)
			return false;

		TransactionSuccessCallback.register(ctx, () -> {
			previouslyUnwrapped = box;
			animationInward = true;
			animationTicks = CYCLE;
			notifyUpdate();
		});

		return true;
	}

	@Override
	public void recheckIfLinksPresent() {}

	@Override
	public boolean redstoneModeActive() {
		return true;
	}

	public void attemptToSend(List<PackagingRequest> queuedRequests) {
		if (queuedRequests == null && (!heldBox.isEmpty() || animationTicks != 0))
			return;

		Storage<ItemVariant> targetInv = targetInventory.getInventory();
		if (targetInv == null || targetInv instanceof PackagerItemHandler)
			return;

		attemptToDefrag(targetInv);
		if (heldBox.isEmpty())
			return;

		updateSignAddress();
		if (!signBasedAddress.isBlank())
			PackageItem.addAddress(heldBox, signBasedAddress);
	}

	protected void attemptToDefrag(Storage<ItemVariant> targetInv) {
		defragmenter.clear();
		int completedOrderId = -1;

		for (StorageView<ItemVariant> view : targetInv.nonEmptyViews()) {
			ItemVariant resource = view.getResource();
			if (!PackageItem.isPackage(resource))
				continue;

			if (!defragmenter.isFragmented(resource.toStack(1))) {
				try (Transaction t = Transaction.openOuter()) {
					if (view.extract(resource, 1, t) == 1) {
						t.commit();
						heldBox = resource.toStack();
						animationInward = false;
						animationTicks = CYCLE;
						notifyUpdate();
					}
				}
				return;
			}

			ItemStack stack = resource.toStack(TransferUtil.truncateLong(view.getAmount()));
			completedOrderId = defragmenter.addPackageFragment(stack);
			if (completedOrderId != -1)
				break;
		}

		if (completedOrderId == -1)
			return;

		List<ItemStack> boxesToExport = defragmenter.repack(completedOrderId);

		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<ItemVariant> view : targetInv.nonEmptyViews()) {
				ItemVariant resource = view.getResource();
				if (!PackageItem.isPackage(resource))
					continue;
				if (PackageItem.getOrderId(resource) != completedOrderId)
					continue;
				view.extract(resource, view.getAmount(), t);
			}

			if (boxesToExport.isEmpty()) {
				t.commit();
				return;
			}

			heldBox = boxesToExport.get(0)
				.copy();
			animationInward = false;
			animationTicks = CYCLE;

			for (int i = 1; i < boxesToExport.size(); i++) {
				ItemStack stack = boxesToExport.get(i);
				if (targetInv.insert(ItemVariant.of(stack), stack.getCount(), t) != stack.getCount()) {
					return;
				}
			}

			t.commit();
		}

		notifyUpdate();
	}

	public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
			net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
			AllBlockEntityTypes.REPACKAGER.get(),
			(be, context) -> be.inventory
		);
	}

}
