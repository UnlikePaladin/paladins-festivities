package com.unlikepaladin.pfest.registry;

import com.unlikepaladin.pfest.menu.PumpkinCarvingMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class MenuTypes {

    public static void init() {

    }

    public static Supplier<MenuType<PumpkinCarvingMenu>> PUMPKIN_MENU =
            MenuRegistry.registerScreenHandlerSimple("pumpkin_carving_station", PumpkinCarvingMenu::new);

}
