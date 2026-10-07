package com.unlikepaladin.pfest.menu;

import com.unlikepaladin.pfest.registry.MenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PumpkinCarvingMenu extends AbstractContainerMenu {

    public PumpkinCarvingMenu(int i, Inventory inventory) {
        super(MenuTypes.PUMPKIN_MENU.get(), i);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
