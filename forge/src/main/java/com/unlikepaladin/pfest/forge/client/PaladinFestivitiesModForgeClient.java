package com.unlikepaladin.pfest.forge.client;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import com.unlikepaladin.pfest.client.PaladinFestivitiesModClient;
import com.unlikepaladin.pfest.registry.client.ColorRegistry;
import com.unlikepaladin.pfest.registry.client.forge.BlockEntityRendererRegistryImpl;
import com.unlikepaladin.pfest.registry.client.forge.ColorRegistryImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = PaladinFestivitiesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PaladinFestivitiesModForgeClient {
    public PaladinFestivitiesModForgeClient() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(PaladinFestivitiesModClient::clientInit);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BlockEntityRendererRegistryImpl.registerRenderers(event);
    }

    @SubscribeEvent
    public static void registerBlockColors(ColorHandlerEvent.Block event) {
        ColorRegistry.init();
        ColorRegistryImpl.BLOCK_COLORS.forEach((block, color) -> event.getBlockColors().register(color, block));
    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        ColorRegistry.init();
        ColorRegistryImpl.ITEM_COLORS.forEach((item, color) -> event.getItemColors().register(color, item));
    }
}
