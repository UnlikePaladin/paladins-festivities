package com.unlikepaladin.pfest.registry.fabric;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ItemRegistryImpl {
    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> supplier) {
        T item = Registry.register(
                Registry.ITEM,
                new ResourceLocation(PaladinFestivitiesMod.MOD_ID, name),
                supplier.get()
        );
        return () -> item;
    }
}
