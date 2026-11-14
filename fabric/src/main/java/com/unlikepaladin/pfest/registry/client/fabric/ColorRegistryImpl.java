package com.unlikepaladin.pfest.registry.client.fabric;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ColorRegistryImpl {
    public static void registerBlockColor(Block block, BlockColor blockColor) {
        ColorProviderRegistry.BLOCK.register(blockColor, block);
    }

    public static void registerItemColor(Item item, ItemColor itemColor) {
        ColorProviderRegistry.ITEM.register(itemColor, item);
    }
}
