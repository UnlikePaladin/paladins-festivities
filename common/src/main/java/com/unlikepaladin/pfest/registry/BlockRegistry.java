package com.unlikepaladin.pfest.registry;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class BlockRegistry {
    @ExpectPlatform
    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> supplier) {
        throw new AssertionError();
    }
}
