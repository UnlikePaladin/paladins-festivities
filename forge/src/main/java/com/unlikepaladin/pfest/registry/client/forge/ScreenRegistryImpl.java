package com.unlikepaladin.pfest.registry.client.forge;

import com.unlikepaladin.pfest.registry.TriFunc;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ScreenRegistryImpl {
    public static <T extends AbstractContainerMenu, J extends Screen & MenuAccess<T>> void registerScreen(MenuType<T> screenType, TriFunc<T, Inventory, Component, J> factory) {
        MenuScreens.register(screenType, factory::apply);
    }
}
