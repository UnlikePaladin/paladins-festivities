package com.unlikepaladin.pfest;

import com.unlikepaladin.pfest.registry.MenuTypes;
import com.unlikepaladin.pfest.registry.ModBlockEntities;
import com.unlikepaladin.pfest.registry.ModBlocks;

public final class PaladinFestivitiesMod {
    public static final String MOD_ID = "paladinsfestivities";

    public static void init() {
        ModBlocks.init();
        ModBlockEntities.init();
        MenuTypes.init();
    }
}
