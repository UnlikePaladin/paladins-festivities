package com.unlikepaladin.pfest.registry.client;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import com.unlikepaladin.pfest.client.screens.PumpkinCarvingScreen;
import com.unlikepaladin.pfest.registry.MenuTypes;
import com.unlikepaladin.pfest.registry.TriFunc;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ScreenRegistry {

    public static void init() {
        registerScreen(MenuTypes.PUMPKIN_MENU.get(), PumpkinCarvingScreen::new);
    }

    @ExpectPlatform
    public static <T extends AbstractContainerMenu, J extends Screen & MenuAccess<T>> void registerScreen(MenuType<T> screenType, TriFunc<T, Inventory, Component, J> factory) {
    }
}
