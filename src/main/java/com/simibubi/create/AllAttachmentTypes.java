package com.simibubi.create;

import com.simibubi.create.content.contraptions.minecart.capability.MinecartController;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class AllAttachmentTypes {
	public static final AttachmentType<MinecartController> MINECART_CONTROLLER = AttachmentRegistry.createDefaulted(
		Create.asResource("minecart_controller"),
		() -> MinecartController.EMPTY
	);

	public static void register() {
	}
}
