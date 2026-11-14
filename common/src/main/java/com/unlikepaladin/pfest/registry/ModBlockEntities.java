package com.unlikepaladin.pfest.registry;

import com.unlikepaladin.pfest.blocks.blockentities.CandelabraBlockEntity;
import com.unlikepaladin.pfest.blocks.blockentities.CustomCarvedPumpkinBlockEntity;
import com.unlikepaladin.pfest.blocks.blockentities.PotionCauldronBlockEntity;
import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static void init() {}

    public static Supplier<BlockEntityType<PumpkinCarvingStationBlockEntity>> PUMPKIN_CARVING_STATION =
            BlockEntityRegistry.registerBlockEntity("pumpkin_carving_station", PumpkinCarvingStationBlockEntity::new, ModBlocks.PUMPKIN_CARVING_STATION);

    public static Supplier<BlockEntityType<CustomCarvedPumpkinBlockEntity>> CUSTOM_CARVED_PUMPKIN =
            BlockEntityRegistry.registerBlockEntity("custom_carved_pumpkin", CustomCarvedPumpkinBlockEntity::new, ModBlocks.CUSTOM_CARVED_PUMPKIN);

    public static Supplier<BlockEntityType<CandelabraBlockEntity>> CANDELABRA =
            BlockEntityRegistry.registerBlockEntity("candelabra", CandelabraBlockEntity::new, ModBlocks.GOLD_CANDELABRA, ModBlocks.IRON_CANDELABRA);

    public static Supplier<BlockEntityType<PotionCauldronBlockEntity>> POTION_CAULDRON =
            BlockEntityRegistry.registerBlockEntity("potion_cauldron", PotionCauldronBlockEntity::new, ModBlocks.POTION_CAULDRON);
}
