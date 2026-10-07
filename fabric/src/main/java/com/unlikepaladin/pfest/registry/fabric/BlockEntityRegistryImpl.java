package com.unlikepaladin.pfest.registry.fabric;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Arrays;
import java.util.function.Supplier;

public class BlockEntityRegistryImpl {
    @SafeVarargs
    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<? extends Block>... validBlocks) {
        Block[] blocks = Arrays.stream(validBlocks).map(Supplier::get).toArray(Block[]::new);
        BlockEntityType<T> blockEntityType = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new ResourceLocation(PaladinFestivitiesMod.MOD_ID, name),
                BlockEntityType.Builder.of(supplier, blocks).build(null)
        );
        return () -> blockEntityType;
    }
}
