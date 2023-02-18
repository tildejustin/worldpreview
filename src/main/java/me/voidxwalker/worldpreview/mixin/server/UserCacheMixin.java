package me.voidxwalker.worldpreview.mixin.server;

import com.google.gson.Gson;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

@Mixin(UserCache.class)
public class UserCacheMixin {
    private List globalList;

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Lcom/google/gson/Gson;fromJson(Ljava/io/Reader;Ljava/lang/reflect/Type;)Ljava/lang/Object;"))
    private Object getGlobalList(Gson instance, Reader json, Type typeOfT) {
        List list = instance.fromJson(json, typeOfT);
        if (list != null && !list.isEmpty()) {
            this.globalList = list;
        }
        return this.globalList;
    }
}
