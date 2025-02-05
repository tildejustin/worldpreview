package me.voidxwalker.worldpreview.mixin.client.render.block;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.render.block.entity.MobSpawnerBlockEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobSpawnerBlockEntityRenderer.class)
public abstract class MobSpawnerBlockEntityRendererMixin {
    @WrapOperation(
            method = "render(Lnet/minecraft/block/entity/MobSpawnerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MobSpawnerLogic;getRenderedEntity()Lnet/minecraft/entity/Entity;")
    )
    private Entity cancelEntityRenderOnPreview(MobSpawnerLogic instance, Operation<Entity> original) {
        return MinecraftClient.getInstance().currentScreen instanceof LevelLoadingScreen ? null : original.call(instance);
    }
}
