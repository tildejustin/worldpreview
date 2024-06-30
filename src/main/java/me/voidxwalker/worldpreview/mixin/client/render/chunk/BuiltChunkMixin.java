package me.voidxwalker.worldpreview.mixin.client.render.chunk;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import me.voidxwalker.worldpreview.WorldPreview;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBuilder.BuiltChunk.class)
public class BuiltChunkMixin {
    @ModifyExpressionValue(method = "getSquaredCameraDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;getCamera()Lnet/minecraft/client/render/Camera;"))
    public Camera getCorrectPos(Camera original, @Share("shouldCancelInner") LocalBooleanRef shouldCancel) {
        if (MinecraftClient.getInstance().currentScreen instanceof LevelLoadingScreen) {
            synchronized (WorldPreview.lock) {
                if (WorldPreview.camera == null) {
                    shouldCancel.set(true);
                }
                return WorldPreview.camera;
            }
        }
        return original;
    }

    @Inject(method = "getSquaredCameraDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;getCamera()Lnet/minecraft/client/render/Camera;", shift = At.Shift.AFTER), cancellable = true)
    private void cancelIfRequested(CallbackInfoReturnable<Double> cir, @Share("shouldCancelInner") LocalBooleanRef shouldCancel) {
        if (shouldCancel.get()) {
            cir.setReturnValue(Double.NaN);
        }
    }

    @ModifyExpressionValue(method = "shouldBuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;getSquaredCameraDistance()D"))
    private double checkInnerCanceled(double original, @Share("shouldCancelOuter") LocalBooleanRef shouldCancel) {
        if (Double.isNaN(original)) {
            shouldCancel.set(true);
        }
        return original;
    }

    @Inject(method = "shouldBuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;getSquaredCameraDistance()D", shift = At.Shift.AFTER), cancellable = true)
    private void applyOuterCancel(CallbackInfoReturnable<Boolean> cir, @Share("shouldCancelOuter") LocalBooleanRef shouldCancel) {
        if (shouldCancel.get()) {
            cir.setReturnValue(false);
        }
    }
}
