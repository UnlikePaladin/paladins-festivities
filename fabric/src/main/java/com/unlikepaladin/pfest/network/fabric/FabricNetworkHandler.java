package com.unlikepaladin.pfest.network.fabric;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import com.unlikepaladin.pfest.network.ServerPacketHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class FabricNetworkHandler {
    public static final ResourceLocation CARVE_ID = new ResourceLocation(PaladinFestivitiesMod.MOD_ID, "carve");
    public static final ResourceLocation OCCUPIED_ID = new ResourceLocation(PaladinFestivitiesMod.MOD_ID, "occupied");
    public static final ResourceLocation FINISH_CARVE_ID = new ResourceLocation(PaladinFestivitiesMod.MOD_ID, "finish_carve");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(CARVE_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int faceIndex = buf.readVarInt();
            byte[] faceData = buf.readByteArray();
            server.execute(() -> ServerPacketHandler.handleCarve(player, pos, faceIndex, faceData));
        });

        ServerPlayNetworking.registerGlobalReceiver(OCCUPIED_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean occupied = buf.readBoolean();
            server.execute(() -> ServerPacketHandler.handleOccupied(player, pos, occupied));
        });

        ServerPlayNetworking.registerGlobalReceiver(FINISH_CARVE_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> ServerPacketHandler.handleFinishCarve(player, pos));
        });
    }
}
