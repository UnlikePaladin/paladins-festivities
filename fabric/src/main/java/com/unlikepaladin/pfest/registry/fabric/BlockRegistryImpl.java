package com.unlikepaladin.pfest.registry.fabric;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class BlockRegistryImpl {

    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> supplier) {
        T block = Registry.register(
                Registry.BLOCK,
                new ResourceLocation(PaladinFestivitiesMod.MOD_ID, name),
                supplier.get()
        );
        return () -> block;
    }
}
