package com.unlikepaladin.pfest.registry.forge;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class BlockEntityRegistryImpl {
    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, "paladinfestivities");
    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlock(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Block[] blocks) {
        return BLOCK_ENTITY_TYPES.register(name, () -> BlockEntityType.Builder.of(supplier, blocks).build(null));
    }
}
