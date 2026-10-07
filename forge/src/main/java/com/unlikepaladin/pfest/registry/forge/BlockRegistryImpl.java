package com.unlikepaladin.pfest.registry.forge;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

import static com.unlikepaladin.pfest.PaladinFestivitiesMod.MOD_ID;

public class BlockRegistryImpl {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> supplier) {
        return BLOCKS.register(name, supplier);
    }
}
