package com.unlikepaladin.pfest.menu.forge;

import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class MenuHelperImpl {
    public static void openPumpkinCarvingMenu(ServerPlayer player, PumpkinCarvingStationBlockEntity station) {
        NetworkHooks.openGui(player, station, buf -> buf.writeBlockPos(station.getBlockPos()));
    }
}
