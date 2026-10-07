package com.unlikepaladin.pfest.registry.client.forge;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class BlockEntityRendererRegistryImpl {
    private static final Map<Supplier<? extends BlockEntityType<?>>, BlockEntityRendererProvider<?>> PENDING = new ConcurrentHashMap<>();

    public static <T extends BlockEntity> void register(Supplier<BlockEntityType<T>> type, BlockEntityRendererProvider<T> provider) {
        PENDING.put(type, provider);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        PENDING.forEach((typeSupplier, provider) -> {
            event.registerBlockEntityRenderer((BlockEntityType) typeSupplier.get(), provider);
        });
    }
}
