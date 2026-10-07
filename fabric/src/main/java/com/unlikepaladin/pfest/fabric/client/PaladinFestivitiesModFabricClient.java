package com.unlikepaladin.pfest.fabric.client;

import com.unlikepaladin.pfest.client.PaladinFestivitiesModClient;
import com.unlikepaladin.pfest.client.render.CarvedPumpkinItemRenderer;
import com.unlikepaladin.pfest.registry.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

public final class PaladinFestivitiesModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PaladinFestivitiesModClient.clientInit();
        BuiltinItemRendererRegistry.INSTANCE.register(
                ModBlocks.CUSTOM_CARVED_PUMPKIN.get(),
                CarvedPumpkinItemRenderer::render
        );
    }
}
