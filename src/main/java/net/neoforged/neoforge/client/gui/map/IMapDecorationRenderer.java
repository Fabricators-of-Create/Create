package net.neoforged.neoforge.client.gui.map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public interface IMapDecorationRenderer {
	boolean render(MapDecoration decoration, PoseStack poseStack, MultiBufferSource bufferSource, MapItemSavedData mapData,
		MapDecorationTextureManager decorationTextures, boolean inItemFrame, int packedLight, int index);
}
