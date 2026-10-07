package com.unlikepaladin.pfest.fabric.client;

import com.unlikepaladin.pfest.client.PaladinFestivitiesModClient;
import net.fabricmc.api.ClientModInitializer;

public final class PaladinFestivitiesModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        PaladinFestivitiesModClient.clientInit();
    }
}
