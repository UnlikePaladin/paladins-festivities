package com.unlikepaladin.pfest.registry;

import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static void init() {}

    public static Supplier<BlockEntityType<PumpkinCarvingStationBlockEntity>> PUMPKIN_CARVING_STATION =
            BlockEntityRegistry.registerBlock("pumpkin_carving_station", PumpkinCarvingStationBlockEntity::new);

}
