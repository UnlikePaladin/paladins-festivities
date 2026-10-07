package com.unlikepaladin.pfest.registry;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class MenuRegistry {
    @ExpectPlatform
    public static <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerScreenHandlerExtended(String id, TriFunc<Integer, Inventory, FriendlyByteBuf, T> factory) {
        throw new RuntimeException();
    }

    @ExpectPlatform
    public static <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerScreenHandlerSimple(String id, BiFunction<Integer, Inventory, T> factory) {
        throw new RuntimeException();
    }

}
