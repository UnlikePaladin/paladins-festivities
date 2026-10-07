package com.unlikepaladin.pfest.mixin;

import com.unlikepaladin.pfest.blocks.blockentities.CustomCarvedPumpkinBlockEntity;
import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "handleBlockEntityData", at = @At("TAIL"))
    private void pfest$handleBlockEntityData(ClientboundBlockEntityDataPacket packet, CallbackInfo ci) {
        if (this.minecraft.level != null && packet.getTag() != null) {
            BlockPos pos = packet.getPos();
            BlockEntity be = this.minecraft.level.getBlockEntity(pos);
            if (be instanceof PumpkinCarvingStationBlockEntity station) {
                station.load(packet.getTag());
            } else if (be instanceof CustomCarvedPumpkinBlockEntity carved) {
                carved.load(packet.getTag());
            }
        }
    }
}
