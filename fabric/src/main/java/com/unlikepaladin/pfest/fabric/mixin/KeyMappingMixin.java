package com.unlikepaladin.pfest.fabric.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.unlikepaladin.pfest.registry.client.ModKeyMappings;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {
    @Shadow @Final private static Map<String, KeyMapping> ALL;
    @Shadow @Final private static Map<InputConstants.Key, KeyMapping> MAP;
    @Shadow private String category;

    @Inject(method = "resetMapping", at = @At("HEAD"), cancellable = true)
    private static void pfest$resetMapping(CallbackInfo ci) {
        MAP.clear();
        for (KeyMapping keyMapping : ALL.values()) {
            if (!ModKeyMappings.CATEGORY.equals(keyMapping.getCategory())) {
                MAP.put(((KeyMappingAccessor) keyMapping).pfest$getKey(), keyMapping);
            }
        }
        ci.cancel();
    }

    @Inject(method = "same", at = @At("HEAD"), cancellable = true)
    private void pfest$same(KeyMapping other, CallbackInfoReturnable<Boolean> cir) {
        boolean thisIsScreen = ModKeyMappings.CATEGORY.equals(this.category);
        boolean otherIsScreen = other != null && ModKeyMappings.CATEGORY.equals(other.getCategory());
        if (thisIsScreen != otherIsScreen) {
            cir.setReturnValue(false);
        }
    }
}
