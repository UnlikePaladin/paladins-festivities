package com.unlikepaladin.pfest.menu.fabric;

import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import com.unlikepaladin.pfest.menu.PumpkinCarvingMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MenuHelperImpl {
    public static void openPumpkinCarvingMenu(ServerPlayer player, PumpkinCarvingStationBlockEntity station) {
        player.openMenu(new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                buf.writeBlockPos(station.getBlockPos());
            }

            @Override
            public Component getDisplayName() {
                return station.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new PumpkinCarvingMenu(syncId, inv, station.getBlockPos(), station);
            }
        });
    }
}
