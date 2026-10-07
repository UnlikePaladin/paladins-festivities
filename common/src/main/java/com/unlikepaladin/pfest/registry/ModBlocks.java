package com.unlikepaladin.pfest.registry;

import com.unlikepaladin.pfest.blocks.PumpkinCarvingStationBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import java.util.function.Supplier;

public class ModBlocks {
    public static void init() {}

    public static final Supplier<Block> PUMPKIN_CARVING_STATION =
            RegistryHelper.registerBlockAndItem("pumpkin_carving_station",
                    () -> new PumpkinCarvingStationBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)));
}
