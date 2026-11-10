package com.unlikepaladin.pfest.mixin;

import com.unlikepaladin.pfest.client.screens.PumpkinCarvingScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPosition(Vec3 vec3);
    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "setup", at = @At("TAIL"))
    private void pfest$overrideCarvingCamera(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTicks, CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof PumpkinCarvingScreen screen) {
            Vec3 camPos = screen.getCustomCameraPos(partialTicks);
            if (camPos != null) {
                this.setPosition(camPos);
                this.setRotation(screen.getCustomCameraYaw(partialTicks), screen.getCustomCameraPitch(partialTicks));
            }
        }
    }
}
