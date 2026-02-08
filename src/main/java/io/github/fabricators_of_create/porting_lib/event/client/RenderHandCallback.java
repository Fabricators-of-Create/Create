package io.github.fabricators_of_create.porting_lib.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class RenderHandCallback {
	public interface Listener {
		void onRenderHand(RenderHandEvent event);
	}

	public static final Event EVENT = new Event();

	public static class Event {
		public void register(Listener listener) {
		}
	}

	public static class RenderHandEvent {
		private final ItemStack itemStack;
		private final PoseStack poseStack;
		private final MultiBufferSource multiBufferSource;
		private final int packedLight;
		private final float partialTicks;
		private final InteractionHand hand;
		private final float equipProgress;
		private final float swingProgress;
		private boolean canceled;

		public RenderHandEvent(ItemStack itemStack, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight,
			float partialTicks, InteractionHand hand, float equipProgress, float swingProgress) {
			this.itemStack = itemStack;
			this.poseStack = poseStack;
			this.multiBufferSource = multiBufferSource;
			this.packedLight = packedLight;
			this.partialTicks = partialTicks;
			this.hand = hand;
			this.equipProgress = equipProgress;
			this.swingProgress = swingProgress;
		}

		public ItemStack getItemStack() {
			return itemStack;
		}

		public PoseStack getPoseStack() {
			return poseStack;
		}

		public MultiBufferSource getMultiBufferSource() {
			return multiBufferSource;
		}

		public int getPackedLight() {
			return packedLight;
		}

		public float getPartialTicks() {
			return partialTicks;
		}

		public InteractionHand getHand() {
			return hand;
		}

		public float getEquipProgress() {
			return equipProgress;
		}

		public float getSwingProgress() {
			return swingProgress;
		}

		public boolean isCanceled() {
			return canceled;
		}

		public void setCanceled(boolean canceled) {
			this.canceled = canceled;
		}
	}
}
