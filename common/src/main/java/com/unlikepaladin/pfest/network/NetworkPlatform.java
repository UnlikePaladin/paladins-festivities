package com.unlikepaladin.pfest.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;

public class NetworkPlatform {
    @ExpectPlatform
    public static void sendCarveToServer(BlockPos pos, int faceIndex, byte[] faceData) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendOccupiedToServer(BlockPos pos, boolean occupied) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendFinishCarveToServer(BlockPos pos) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendRotationToServer(BlockPos pos, float angle) {
        throw new AssertionError();
    }
}
