package com.unlikepaladin.pfest.registry;

import com.unlikepaladin.pfest.blocks.CandelabraBlock;
import com.unlikepaladin.pfest.blocks.CoffinBlock;
import com.unlikepaladin.pfest.blocks.CustomCarvedPumpkinBlock;
import com.unlikepaladin.pfest.blocks.PotionCauldronBlock;
import com.unlikepaladin.pfest.blocks.PumpkinCarvingStationBlock;
import com.unlikepaladin.pfest.blocks.SkeletonPileBlock;
import com.unlikepaladin.pfest.items.CustomCarvedPumpkinItem;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import java.util.function.Supplier;

public class ModBlocks {
    public static void init() {}

    public static final Supplier<Block> PUMPKIN_CARVING_STATION =
            RegistryHelper.registerBlockAndItem("pumpkin_carving_station",
                    () -> new PumpkinCarvingStationBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).noOcclusion().strength(2.0F, 3.0F)));

    public static final Supplier<Block> CUSTOM_CARVED_PUMPKIN =
            RegistryHelper.registerBlockAndCustomItem("custom_carved_pumpkin",
                    () -> new CustomCarvedPumpkinBlock(BlockBehaviour.Properties.copy(Blocks.CARVED_PUMPKIN)
                            .lightLevel(state -> state.getValue(CustomCarvedPumpkinBlock.LIT) ? 15 : 0)),
                    block -> new CustomCarvedPumpkinItem(block, new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static final Supplier<Block> COFFIN =
            RegistryHelper.registerBlockAndCustomItem("coffin",
                    () -> new CoffinBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                            .strength(2.0F, 3.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()),
                    block -> new BedItem(block, new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static final Supplier<Block> GOLD_CANDELABRA =
            RegistryHelper.registerBlockAndItem("gold_candelabra",
                    () -> new CandelabraBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.GOLD)
                            .strength(2.0F, 3.0F)
                            .sound(SoundType.LANTERN)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(CandelabraBlock.LIT) ? 5 * state.getValue(CandelabraBlock.CANDLES) : 0)));

    public static final Supplier<Block> IRON_CANDELABRA =
            RegistryHelper.registerBlockAndItem("iron_candelabra",
                    () -> new CandelabraBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                            .strength(2.0F, 3.0F)
                            .sound(SoundType.LANTERN)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(CandelabraBlock.LIT) ? 5 * state.getValue(CandelabraBlock.CANDLES) : 0)));

    public static final Supplier<Block> SKELETON_PILE =
            RegistryHelper.registerBlockAndItem("skeleton_pile",
                    () -> new SkeletonPileBlock(BlockBehaviour.Properties.of(Material.DECORATION, MaterialColor.SAND)
                            .strength(1.0F)
                            .sound(SoundType.BONE_BLOCK)
                            .noOcclusion()));

    public static final Supplier<Block> POTION_CAULDRON =
            RegistryHelper.registerBlockAndItem("potion_cauldron",
                    () -> new PotionCauldronBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON)));
}
