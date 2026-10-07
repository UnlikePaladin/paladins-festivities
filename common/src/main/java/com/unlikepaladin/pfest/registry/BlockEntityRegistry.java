package com.unlikepaladin.pfest.registry;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class BlockEntityRegistry {
    @ExpectPlatform
    @SafeVarargs
    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<? extends Block>... validBlocks) {
        throw new AssertionError();
    }
}
