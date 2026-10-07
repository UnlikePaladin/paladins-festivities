package com.unlikepaladin.pfest.client.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unlikepaladin.pfest.menu.PumpkinCarvingMenu;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PumpkinCarvingScreen extends AbstractContainerScreen<PumpkinCarvingMenu> {
    public PumpkinCarvingScreen(PumpkinCarvingMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {

    }

    @Override
    protected void init() {}
}
