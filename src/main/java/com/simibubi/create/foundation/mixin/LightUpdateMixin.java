package com.simibubi.create.foundation.mixin;

import java.util.Map;

import com.simibubi.create.foundation.render.backend.light.LightListener;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
@Mixin(ClientChunkManager.class)
public abstract class LightUpdateMixin extends ChunkManager {

    /**
     * JUSTIFICATION: This method is called after a lighting tick once per subchunk where a
     * lighting change occurred that tick. On the client, Minecraft uses this method to inform
     * the rendering system that it needs to redraw a chunk. It does all that work asynchronously,
     * and we should too.
     */
    @Inject(at = @At("HEAD"), method = "onLightUpdate")
    private void onLightUpdate(LightType type, ChunkSectionPos pos, CallbackInfo ci) {
        ClientChunkManager thi = ((ClientChunkManager) (Object) this);

        WorldChunk chunk = thi.getWorldChunk(pos.getSectionX(), pos.getSectionZ(), false);

        int sectionY = pos.getSectionY();

        if (chunk != null) {
            chunk.getBlockEntities()
                 .entrySet()
                 .stream()
                 .filter(entry -> ChunkSectionPos.getSectionCoord(entry.getKey().getY()) == sectionY)
                 .map(Map.Entry::getValue)
                 .filter(tile -> tile instanceof LightListener)
                 .map(tile -> (LightListener) tile)
                 .forEach(LightListener::onChunkLightUpdate);
        }

        ContraptionRenderDispatcher.notifyLightUpdate((World) thi.getWorld(), type, pos);
    }
}