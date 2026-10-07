package com.unlikepaladin.pfest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.unlikepaladin.pfest.blocks.CustomCarvedPumpkinBlock;
import com.unlikepaladin.pfest.blocks.blockentities.CustomCarvedPumpkinBlockEntity;
import com.unlikepaladin.pfest.data.CarvedPumpkinData;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class CustomCarvedPumpkinBlockEntityRenderer implements BlockEntityRenderer<CustomCarvedPumpkinBlockEntity> {

    public CustomCarvedPumpkinBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CustomCarvedPumpkinBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLight, int combinedOverlay) {
        CarvedPumpkinData data = blockEntity.getData();
        if (data.isEmpty()) {
            return;
        }

        Direction facing = blockEntity.getBlockState().getValue(CustomCarvedPumpkinBlock.FACING);
        boolean isLit = blockEntity.getBlockState().getValue(CustomCarvedPumpkinBlock.LIT);
        int light = isLit ? LightTexture.FULL_BRIGHT : combinedLight;

        // 4 faces relative to block facing: 0 = Front, 1 = Right, 2 = Back, 3 = Left
        Direction[] faceDirs = new Direction[]{
                facing,
                facing.getClockWise(),
                facing.getOpposite(),
                facing.getCounterClockWise()
        };

        for (int i = 0; i < 4; i++) {
            if (data.hasFaceCarved(i)) {
                ResourceLocation tex = CarvedPumpkinTextureManager.getOrCreateTexture(data, i, isLit);
                VertexConsumer buffer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
                CarvedPumpkinTextureManager.renderFaceOverlay(poseStack, buffer, faceDirs[i], light, combinedOverlay);
            }
        }
    }
}
