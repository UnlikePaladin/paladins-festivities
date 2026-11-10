package com.unlikepaladin.pfest.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unlikepaladin.pfest.client.screens.PumpkinCarvingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final protected Minecraft minecraft;

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void pfest$hideCrosshairInCarvingScreen(PoseStack poseStack, CallbackInfo ci) {
        if (this.minecraft != null && this.minecraft.screen instanceof PumpkinCarvingScreen) {
            ci.cancel();
        }
    }
}
