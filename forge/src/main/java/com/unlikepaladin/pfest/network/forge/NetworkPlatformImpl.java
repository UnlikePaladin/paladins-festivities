package com.unlikepaladin.pfest.network.forge;

import net.minecraft.core.BlockPos;

public class NetworkPlatformImpl {
    public static void sendCarveToServer(BlockPos pos, int faceIndex, byte[] faceData) {
        ForgeNetworkHandler.CHANNEL.sendToServer(new ForgeNetworkHandler.CarvePacket(pos, faceIndex, faceData));
    }

    public static void sendOccupiedToServer(BlockPos pos, boolean occupied) {
        ForgeNetworkHandler.CHANNEL.sendToServer(new ForgeNetworkHandler.OccupiedPacket(pos, occupied));
    }

    public static void sendFinishCarveToServer(BlockPos pos) {
        ForgeNetworkHandler.CHANNEL.sendToServer(new ForgeNetworkHandler.FinishCarvePacket(pos));
    }

    public static void sendRotationToServer(BlockPos pos, float angle) {
        ForgeNetworkHandler.CHANNEL.sendToServer(new ForgeNetworkHandler.RotationPacket(pos, angle));
    }
}
