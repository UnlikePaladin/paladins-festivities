package com.unlikepaladin.pfest.registry;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ItemRegistry {
    @ExpectPlatform
    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> supplier) {
        throw new AssertionError();
    }
}
