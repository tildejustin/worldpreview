package me.voidxwalker.worldpreview.mixin.client.render;

import me.voidxwalker.worldpreview.WorldPreview;
import net.minecraft.client.render.chunk.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ChunkRenderWorker.class)
public abstract class ChunkRenderWorkerMixin {
    @Redirect(method = "runTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkRenderer;shouldBuild()Z"))
    private boolean alwaysBuild(ChunkRenderer instance) {
        if (WorldPreview.inPreview) {
            return true;
        }
        return instance.shouldBuild();
    }
}
