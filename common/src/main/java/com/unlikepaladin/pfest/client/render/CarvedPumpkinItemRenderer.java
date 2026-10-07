package com.unlikepaladin.pfest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.unlikepaladin.pfest.data.CarvedPumpkinData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class CarvedPumpkinItemRenderer {
    public static void render(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        // 1. Render base pumpkin block model
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.PUMPKIN.defaultBlockState(),
                poseStack,
                multiBufferSource,
                light,
                overlay
        );

        // 2. Render carved overlays from itemstack's BlockEntityTag
        CompoundTag beTag = itemStack.getTagElement("BlockEntityTag");
        if (beTag != null) {
            CarvedPumpkinData data = new CarvedPumpkinData();
            data.loadFromNbt(beTag);
            if (!data.isEmpty()) {
                Direction[] faceDirs = new Direction[]{
                        Direction.NORTH,
                        Direction.EAST,
                        Direction.SOUTH,
                        Direction.WEST
                };

                for (int i = 0; i < 4; i++) {
                    if (data.hasFaceCarved(i)) {
                        ResourceLocation tex = CarvedPumpkinTextureManager.getOrCreateTexture(data, i, false);
                        VertexConsumer buffer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
                        CarvedPumpkinTextureManager.renderFaceOverlay(poseStack, buffer, faceDirs[i], light, overlay);
                    }
                }
            }
        }
    }
}
