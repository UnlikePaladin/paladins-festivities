package com.unlikepaladin.pfest.registry.forge;

import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.function.Supplier;

public class BlockEntityRegistryImpl {
    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, PaladinFestivitiesMod.MOD_ID);

    @SafeVarargs
    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<? extends Block>... validBlocks) {
        return BLOCK_ENTITY_TYPES.register(name, () -> {
            Block[] blocks = Arrays.stream(validBlocks).map(Supplier::get).toArray(Block[]::new);
            return BlockEntityType.Builder.of(supplier, blocks).build(null);
        });
    }
}
