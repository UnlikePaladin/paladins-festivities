package com.unlikepaladin.pfest.registry.client;

import com.unlikepaladin.pfest.blocks.blockentities.PotionCauldronBlockEntity;
import com.unlikepaladin.pfest.registry.ModBlocks;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

public class ColorRegistry {
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        registerBlockColor(ModBlocks.POTION_CAULDRON, (state, level, pos, tintIndex) -> {
            if (tintIndex == 0 && level != null && pos != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof PotionCauldronBlockEntity potionBe) {
                    return potionBe.getColor();
                }
            }
            return -1;
        });

        registerItemColor(ModBlocks.POTION_CAULDRON, (stack, tintIndex) -> {
            if (tintIndex == 0) {
                CompoundTag beTag = stack.getTagElement("BlockEntityTag");
                if (beTag != null && beTag.contains("Potion", Tag.TAG_STRING)) {
                    Potion potion = Registry.POTION.get(ResourceLocation.tryParse(beTag.getString("Potion")));
                    if (potion != null) {
                        return PotionUtils.getColor(potion);
                    }
                }
            }
            return -1;
        });
    }

    public static void registerBlockColor(Supplier<? extends Block> blockSupplier, BlockColor blockColor) {
        registerBlockColor(blockSupplier.get(), blockColor);
    }

    public static void registerItemColor(Supplier<? extends ItemLike> itemSupplier, ItemColor itemColor) {
        registerItemColor(itemSupplier.get().asItem(), itemColor);
    }

    @ExpectPlatform
    public static void registerBlockColor(Block block, BlockColor blockColor) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerItemColor(Item item, ItemColor itemColor) {
        throw new AssertionError();
    }
}
