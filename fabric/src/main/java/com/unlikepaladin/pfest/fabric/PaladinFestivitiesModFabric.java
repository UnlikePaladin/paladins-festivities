package com.unlikepaladin.pfest.fabric;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import com.unlikepaladin.pfest.network.fabric.FabricNetworkHandler;
import net.fabricmc.api.ModInitializer;

public final class PaladinFestivitiesModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricNetworkHandler.init();
        PaladinFestivitiesMod.init();
    }
}
