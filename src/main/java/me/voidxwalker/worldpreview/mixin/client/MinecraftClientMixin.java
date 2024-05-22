package me.voidxwalker.worldpreview.mixin.client;

import me.voidxwalker.worldpreview.*;
import me.voidxwalker.worldpreview.mixin.access.WorldRendererMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.resource.*;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow protected abstract void render(boolean tick);

    @Shadow private @Nullable IntegratedServer server;

    @Shadow @Nullable public Entity cameraEntity;
    @Shadow private @Nullable ClientConnection connection;
    @Shadow @Final private SoundManager soundManager;

    @Shadow protected abstract void reset(Screen screen);

    @Shadow public abstract LevelStorage getLevelStorage();

    @Shadow public WorldRenderer worldRenderer;
    @Shadow @Nullable public Screen currentScreen;

    @Shadow public abstract boolean isDemo();

    @Shadow @Final private LevelStorage levelStorage;
    private int worldpreview_cycleCooldown;

    @Inject(method = "startIntegratedServer",at=@At(value = "INVOKE",shift = At.Shift.AFTER,target = "Lnet/minecraft/server/integrated/IntegratedServer;isLoading()Z"),cancellable = true)
    public void worldpreview_onHotKeyPressed( CallbackInfo ci){
        if(WorldPreview.inPreview){
            worldpreview_cycleCooldown++;
            if(WorldPreview.cycleChunkMapKey.wasPressed()&&worldpreview_cycleCooldown>10&&!WorldPreview.freezePreview){
                worldpreview_cycleCooldown=0;
                WorldPreview.chunkMapPos= WorldPreview.chunkMapPos<5? WorldPreview.chunkMapPos+1:1;
            }
            if(WorldPreview.resetKey.wasPressed()|| WorldPreview.kill==-1){
                WorldPreview.log(Level.INFO,"Leaving world generation");
                WorldPreview.kill = 1;
                while(WorldPreview.inPreview){
                    LockSupport.park(); // I am at a loss to emphasize how bad of an idea Thread.yield() here is.
                }
                this.server.shutdown();
                MinecraftClient.getInstance().disconnect();
                WorldPreview.kill=0;
                MinecraftClient.getInstance().openScreen(new TitleScreen());
                ci.cancel();
            }
            if(WorldPreview.freezeKey.wasPressed()){
                WorldPreview.freezePreview=!WorldPreview.freezePreview;
                if(WorldPreview.freezePreview){
                    WorldPreview.log(Level.INFO,"Freezing Preview"); // insert anchiale joke
                }
                else {
                    WorldPreview.log(Level.INFO,"Unfreezing Preview");
                }
            }
        }
    }

    @Inject(method="startIntegratedServer",at=@At(value = "HEAD"))
    public void isExistingWorld(String name, String displayName, LevelInfo levelInfo, CallbackInfo ci){
        WorldPreview.existingWorld=this.getLevelStorage().levelExists(name);
    }

    @Inject(method="startIntegratedServer", at = @At("HEAD"))
    private void isExistingDemoWorld(String name, String displayName, LevelInfo levelInfo, CallbackInfo ci) {
        if (this.isDemo() && "Demo_World".equals(name)) {
            if (this.levelStorage.levelExists("Demo_World")) {
                WorldPreview.existingWorld = true;
            }
        }
    }

    @Redirect(method="reset",at=@At(value="INVOKE",target="Lnet/minecraft/client/MinecraftClient;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    public void worldpreview_smoothTransition(MinecraftClient instance, Screen screen){
        if(this.currentScreen instanceof LevelLoadingScreen &&  ((WorldRendererMixin)WorldPreview.worldRenderer).getWorld()!=null&&WorldPreview.world!=null&& WorldPreview.clientWord!=null&&WorldPreview.player!=null){
            return;
        }
        instance.openScreen(screen);

    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ReloadableResourceManager;registerListener(Lnet/minecraft/resource/ResourceReloadListener;)V", ordinal = 11))
    public void worldpreview_createWorldRenderer(ReloadableResourceManager instance, ResourceReloadListener resourceReloadListener) {
        WorldPreview.worldRenderer = new WorldRenderer(MinecraftClient.getInstance());
        ((ChunkSetter) WorldPreview.worldRenderer).setPreviewRenderer();
        this.worldRenderer = new WorldRenderer((MinecraftClient) (Object) this);
        instance.registerListener(worldRenderer);

    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V",at=@At(value = "HEAD"))
    public void reset(Screen screen, CallbackInfo ci){
        synchronized (WorldPreview.lock){
            WorldPreview.world=null;
            WorldPreview.player=null;
            WorldPreview.clientWord=null;
            WorldPreview.camera=null;
            if(WorldPreview.worldRenderer!=null){
                WorldPreview.worldRenderer.setWorld(null);
            }
            worldpreview_cycleCooldown=0;
        }
    }

    @Inject(method = "updateDisplay", at = @At(value = "INVOKE", target="Lnet/minecraft/util/profiler/DisableableProfiler;pop()V", ordinal = 0, shift = At.Shift.AFTER))
    private void worldpreview_actuallyInPreview(boolean tick, CallbackInfo ci) {
        if (WorldPreview.inPreview && !WorldPreview.renderingPreview) {
            WorldPreview.renderingPreview = true;
            if (WorldPreview.stateOutputLoaded) {
                StateOutputInterface.outputPreviewing();
            }
            WorldPreview.log(Level.INFO, "Starting Preview at (" + WorldPreview.player.x + ", " + Math.floor(WorldPreview.player.y) + ", " + WorldPreview.player.z + ")");
        }
    }

}
