package com.unlikepaladin.pfest.client;

import com.unlikepaladin.pfest.client.render.CustomCarvedPumpkinBlockEntityRenderer;
import com.unlikepaladin.pfest.client.render.PumpkinCarverBlockEntityRenderer;
import com.unlikepaladin.pfest.registry.ModBlockEntities;
import com.unlikepaladin.pfest.registry.client.BlockEntityRendererRegistry;
import com.unlikepaladin.pfest.registry.client.ModKeyMappings;
import com.unlikepaladin.pfest.registry.client.RenderLayerRegistry;
import com.unlikepaladin.pfest.registry.client.ScreenRegistry;

public class PaladinFestivitiesModClient {
    public static void clientInit() {
        ScreenRegistry.init();
        ModKeyMappings.init();
        net.minecraft.client.KeyMapping.resetMapping();
        BlockEntityRendererRegistry.register(ModBlockEntities.PUMPKIN_CARVING_STATION, PumpkinCarverBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.CUSTOM_CARVED_PUMPKIN, CustomCarvedPumpkinBlockEntityRenderer::new);
        RenderLayerRegistry.init();
    }
}
