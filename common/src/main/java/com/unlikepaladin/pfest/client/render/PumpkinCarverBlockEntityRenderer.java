package com.unlikepaladin.pfest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.unlikepaladin.pfest.blocks.PumpkinCarvingStationBlock;
import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import com.unlikepaladin.pfest.data.CarvedPumpkinData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class PumpkinCarverBlockEntityRenderer implements BlockEntityRenderer<PumpkinCarvingStationBlockEntity> {

    public PumpkinCarverBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PumpkinCarvingStationBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLight, int combinedOverlay) {
        if (!blockEntity.hasPumpkin()) {
            return;
        }

        ItemStack heldPumpkin = blockEntity.getHeldPumpkin();
        Direction facing = blockEntity.getBlockState().getValue(PumpkinCarvingStationBlock.FACING);

        int renderLight = blockEntity.getLevel() != null
                ? LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above())
                : combinedLight;

        poseStack.pushPose();
        // Position pumpkin resting directly on top of the workstation table (Y=1.0)
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-facing.toYRot()));
        poseStack.scale(0.7F, 0.7F, 0.7F);
        poseStack.translate(-0.5, 0.0, -0.5);

        // Render base pumpkin block
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.PUMPKIN.defaultBlockState(),
                poseStack,
                multiBufferSource,
                renderLight,
                combinedOverlay
        );

        // Render live carved face overlays on the workstation pumpkin
        CarvedPumpkinData data = blockEntity.getData();
        if (data.isEmpty() && !heldPumpkin.isEmpty()) {
            CompoundTag beTag = heldPumpkin.getTagElement("BlockEntityTag");
            if (beTag != null) {
                data = new CarvedPumpkinData();
                data.loadFromNbt(beTag);
            }
        }

        if (!data.isEmpty()) {
            Direction[] faceDirs = new Direction[]{
                    Direction.SOUTH,
                    Direction.WEST,
                    Direction.NORTH,
                    Direction.EAST
            };

            for (int i = 0; i < 4; i++) {
                if (data.hasFaceCarved(i)) {
                    ResourceLocation tex = CarvedPumpkinTextureManager.getOrCreateTexture(data, i, false);
                    VertexConsumer buffer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
                    CarvedPumpkinTextureManager.renderFaceOverlay(poseStack, buffer, faceDirs[i], renderLight, combinedOverlay);
                }
            }
        }

        poseStack.popPose();
    }
}
