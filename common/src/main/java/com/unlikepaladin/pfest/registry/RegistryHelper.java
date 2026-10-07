package com.unlikepaladin.pfest.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class RegistryHelper {
    public static <T extends Block> Supplier<T> registerBlockAndItem(String name, Supplier<T> supplier) {
        return registerBlockAndItem(name, supplier, new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS));
    }

    public static <T extends Block> Supplier<T> registerBlockAndItem(String name, Supplier<T> supplier, Item.Properties properties) {
        Supplier<T> blockSupplier = BlockRegistry.registerBlock(name, supplier);
        ItemRegistry.registerItem(name, () -> new BlockItem(blockSupplier.get(), properties));
        return blockSupplier;
    }
}
