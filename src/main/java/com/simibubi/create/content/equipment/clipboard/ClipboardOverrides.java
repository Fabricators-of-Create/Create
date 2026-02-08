package com.simibubi.create.content.equipment.clipboard;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.Create;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.lang.Lang;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile.UncheckedModelFile;
import io.github.fabricators_of_create.porting_lib.models.generators.ItemModelBuilder;

public class ClipboardOverrides {

	public enum ClipboardType implements StringRepresentable {
		EMPTY("empty_clipboard"), WRITTEN("clipboard"), EDITING("clipboard_and_quill");

		public static final Codec<ClipboardType> CODEC = StringRepresentable.fromValues(ClipboardType::values);
		public static final StreamCodec<ByteBuf, ClipboardType> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(ClipboardType.class);

		public String file;
		public static ResourceLocation ID = Create.asResource("clipboard_type");

		ClipboardType(String file) {
			this.file = file;
		}

		@Override
		public @NotNull String getSerializedName() {
			return Lang.asId(name());
		}
	}

	public static void switchTo(ClipboardType type, ItemStack clipboardItem) {
		clipboardItem.set(AllDataComponents.CLIPBOARD_TYPE, type);
	}

	@Environment(EnvType.CLIENT)
	public static void registerModelOverridesClient(ClipboardBlockItem item) {
		ItemProperties.register(item, ClipboardType.ID, (pStack, pLevel, pEntity, pSeed) ->
				pStack.getOrDefault(AllDataComponents.CLIPBOARD_TYPE, ClipboardType.EMPTY).ordinal()
		);
	}

	public static ItemModelBuilder addOverrideModels(DataGenContext<Item, ClipboardBlockItem> c,
		RegistrateItemModelProvider p) {
		ItemModelBuilder builder = p.generated(c::get);
		for (ClipboardType type : ClipboardType.values()) {
			int i = type.ordinal();
			builder.override()
					.predicate(ClipboardType.ID, i)
					.model(p.getBuilder(c.getName() + "_" + i)
							.parent(new UncheckedModelFile("item/generated"))
							.texture("layer0", Create.asResource("item/" + type.file)))
					.end();
		}
		return builder;
	}

}
