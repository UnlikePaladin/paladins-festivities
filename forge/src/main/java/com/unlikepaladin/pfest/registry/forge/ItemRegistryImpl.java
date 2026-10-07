package com.unlikepaladin.pfest.registry.forge;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ItemRegistryImpl {
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PaladinFestivitiesMod.MOD_ID);

    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> supplier) {
        return ITEMS.register(name, supplier);
    }
}
