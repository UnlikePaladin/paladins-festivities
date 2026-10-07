package com.unlikepaladin.pfest.registry.forge;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import com.unlikepaladin.pfest.registry.TriFunc;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class MenuRegistryImpl {

    public static DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.CONTAINERS, PaladinFestivitiesMod.MOD_ID);
    public static <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerScreenHandlerExtended(String id, TriFunc<Integer, Inventory, FriendlyByteBuf, T> factory) {
        return REGISTRY.register(id, () -> {
            MenuType<T> type = IForgeContainerType.create(factory::apply);
            type.setRegistryName(id);
            return type;
        });
    }

    public static <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerScreenHandlerSimple(String id, BiFunction<Integer, Inventory, T> factory) {
        return REGISTRY.register(id, () -> {
            MenuType<T> type = new MenuType<>(factory::apply);
            type.setRegistryName(id);
            return type;
        });
    }

}
