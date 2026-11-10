package com.unlikepaladin.pfest.network.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class NetworkPlatformImpl {
    public static void sendCarveToServer(BlockPos pos, int faceIndex, byte[] faceData) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeVarInt(faceIndex);
        buf.writeByteArray(faceData);
        ClientPlayNetworking.send(FabricNetworkHandler.CARVE_ID, buf);
    }

    public static void sendOccupiedToServer(BlockPos pos, boolean occupied) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeBoolean(occupied);
        ClientPlayNetworking.send(FabricNetworkHandler.OCCUPIED_ID, buf);
    }

    public static void sendFinishCarveToServer(BlockPos pos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(FabricNetworkHandler.FINISH_CARVE_ID, buf);
    }

    public static void sendRotationToServer(BlockPos pos, float angle) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeFloat(angle);
        ClientPlayNetworking.send(FabricNetworkHandler.ROTATION_ID, buf);
    }
}
