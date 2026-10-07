package com.unlikepaladin.pfest.registry.fabric;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import com.unlikepaladin.pfest.registry.TriFunc;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class MenuRegistryImpl {
    public static <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerScreenHandlerExtended(String id, TriFunc<Integer, Inventory, FriendlyByteBuf, T> factory) {
        MenuType<T> menu = ScreenHandlerRegistry.registerExtended(new ResourceLocation(PaladinFestivitiesMod.MOD_ID, id), factory::apply);
        return () -> menu;
    }

    public static <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerScreenHandlerSimple(String id, BiFunction<Integer, Inventory, T> factory) {
        MenuType<T> menu = ScreenHandlerRegistry.registerSimple(new ResourceLocation(PaladinFestivitiesMod.MOD_ID, id), factory::apply);
        return () -> menu;
    }

}
