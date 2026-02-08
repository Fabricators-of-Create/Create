package com.simibubi.create.content.equipment.wrench;

import javax.annotation.Nonnull;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.world.phys.EntityHitResult;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WrenchItem extends Item {

	public WrenchItem(Properties properties) {
		super(properties);
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || !player.mayBuild())
			return super.useOn(context);

		BlockState state = context.getLevel()
				.getBlockState(context.getClickedPos());
		Block block = state.getBlock();

		if (!(block instanceof IWrenchable actor)) {
			if (player.isShiftKeyDown() && canWrenchPickup(state))
				return onItemUseOnOther(context);
			return super.useOn(context);
		}

		if (player.isShiftKeyDown())
			return actor.onSneakWrenched(state, context);
		return actor.onWrenched(state, context);
	}

	private static boolean canWrenchPickup(BlockState state) {
		return AllTags.AllBlockTags.WRENCH_PICKUP.matches(state);
	}

	private static InteractionResult onItemUseOnOther(UseOnContext context) {
		Player player = context.getPlayer();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);
		if (!(world instanceof ServerLevel))
			return InteractionResult.SUCCESS;
		if (player != null && !player.isCreative())
			Block.getDrops(state, (ServerLevel) world, pos, world.getBlockEntity(pos), player, context.getItemInHand())
				.forEach(itemStack -> player.getInventory().placeItemBackInInventory(itemStack));
		state.spawnAfterBreak((ServerLevel) world, pos, ItemStack.EMPTY, true);
		world.destroyBlock(pos, false);
		AllSoundEvents.WRENCH_REMOVE.playOnServer(world, pos, 1, Create.RANDOM.nextFloat() * .5f + .5f);
		return InteractionResult.SUCCESS;
	}

	public static InteractionResult wrenchInstaKillsMinecarts(Player player, Level world, InteractionHand hand, Entity target, @Nullable EntityHitResult entityRayTraceResult) {
		if (!(target instanceof AbstractMinecart minecart))
			return InteractionResult.PASS;
		ItemStack heldItem = player.getMainHandItem();
		if (!AllItems.WRENCH.isIn(heldItem))
			return InteractionResult.PASS;
		if (player.isCreative())
			return InteractionResult.PASS;
		minecart.hurt(minecart.damageSources().playerAttack(player), 100);
		return InteractionResult.SUCCESS;
	}

//	@Override
//	@Environment(EnvType.CLIENT)
//	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
//		consumer.accept(SimpleCustomRenderer.create(this, new WrenchItemRenderer()));
//	}

}
