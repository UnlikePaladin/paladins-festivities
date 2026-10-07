package com.unlikepaladin.pfest.registry.fabric;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class BlockEntityRegistryImpl {
    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlock(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Block[] blocks) {
        BlockEntityType<T> blockEntityType = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new ResourceLocation(PaladinFestivitiesMod.MOD_ID, name),
                BlockEntityType.Builder.of(supplier, blocks).build(null)
        );
        return () -> blockEntityType;
    }
}
