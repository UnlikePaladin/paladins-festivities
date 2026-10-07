package com.unlikepaladin.pfest.forge;

import net.minecraftforge.fml.common.Mod;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;

@Mod(PaladinFestivitiesMod.MOD_ID)
public final class PaladinFestivitiesModForge {
    public PaladinFestivitiesModForge() {
        // Run our common setup.
        PaladinFestivitiesMod.init();
    }
}
