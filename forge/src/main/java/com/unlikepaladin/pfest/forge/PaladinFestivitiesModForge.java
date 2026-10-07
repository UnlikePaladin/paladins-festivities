package com.unlikepaladin.pfest.forge;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import com.unlikepaladin.pfest.network.forge.ForgeNetworkHandler;
import com.unlikepaladin.pfest.registry.forge.BlockEntityRegistryImpl;
import com.unlikepaladin.pfest.registry.forge.BlockRegistryImpl;
import com.unlikepaladin.pfest.registry.forge.ItemRegistryImpl;
import com.unlikepaladin.pfest.registry.forge.MenuRegistryImpl;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PaladinFestivitiesMod.MOD_ID)
public final class PaladinFestivitiesModForge {
    public PaladinFestivitiesModForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BlockRegistryImpl.BLOCKS.register(bus);
        ItemRegistryImpl.ITEMS.register(bus);
        BlockEntityRegistryImpl.BLOCK_ENTITY_TYPES.register(bus);
        MenuRegistryImpl.REGISTRY.register(bus);

        ForgeNetworkHandler.init();

        PaladinFestivitiesMod.init();
    }
}
