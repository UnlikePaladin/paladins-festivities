package com.unlikepaladin.pfest.menu;

import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.level.ServerPlayer;

public class MenuHelper {
    @ExpectPlatform
    public static void openPumpkinCarvingMenu(ServerPlayer player, PumpkinCarvingStationBlockEntity station) {
        throw new AssertionError();
    }
}
