package com.unlikepaladin.pfest.registry.client.forge;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class ColorRegistryImpl {
    public static final Map<Block, BlockColor> BLOCK_COLORS = new HashMap<>();
    public static final Map<Item, ItemColor> ITEM_COLORS = new HashMap<>();

    public static void registerBlockColor(Block block, BlockColor blockColor) {
        BLOCK_COLORS.put(block, blockColor);
    }

    public static void registerItemColor(Item item, ItemColor itemColor) {
        ITEM_COLORS.put(item, itemColor);
    }
}
