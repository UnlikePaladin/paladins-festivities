package com.unlikepaladin.pfest.registry.client;

import com.unlikepaladin.pfest.registry.ModBlocks;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class RenderLayerRegistry {

    public static void init() {
        registerBlockToRenderLayer(ModBlocks.PUMPKIN_CARVING_STATION, RenderType.cutout());
        registerBlockToRenderLayer(ModBlocks.COFFIN, RenderType.cutout());
        registerBlockToRenderLayer(ModBlocks.GOLD_CANDELABRA, RenderType.cutout());
        registerBlockToRenderLayer(ModBlocks.IRON_CANDELABRA, RenderType.cutout());
        registerBlockToRenderLayer(ModBlocks.SKELETON_PILE, RenderType.cutout());
    }

    public static void registerBlockToRenderLayer(Supplier<? extends Block> blockSupplier, RenderType renderType) {
        registerBlockToRenderLayer(blockSupplier.get(), renderType);
    }

    @ExpectPlatform
    public static void registerBlockToRenderLayer(Block block, RenderType renderType) {
        throw new AssertionError();
    }
}
