package com.unlikepaladin.pfest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
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
        // Position at top center of the workstation table (table surface is at Y=1.004)
        poseStack.translate(0.5, 1.004, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-facing.toYRot()));

        // Smooth pumpkin rotation while carving
        float pumpkinAngle = blockEntity.getRenderTurntableAngle(partialTicks);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(pumpkinAngle));

        // Position pumpkin resting directly on the table
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

        Direction[] faceDirs = new Direction[]{
                Direction.SOUTH,
                Direction.WEST,
                Direction.NORTH,
                Direction.EAST
        };

        if (!data.isEmpty()) {
            boolean isLit = blockEntity.isPreviewLit();
            int overlayLight = isLit ? net.minecraft.client.renderer.LightTexture.FULL_BRIGHT : renderLight;

            for (int i = 0; i < 4; i++) {
                if (data.hasFaceCarved(i)) {
                    ResourceLocation tex = CarvedPumpkinTextureManager.getOrCreateTexture(data, i, isLit);
                    VertexConsumer buffer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
                    CarvedPumpkinTextureManager.renderFaceOverlay(poseStack, buffer, faceDirs[i], overlayLight, combinedOverlay);
                }
            }
        }

        // Flush solid & cutout buffers so base pumpkin block and carved face overlays are fully in the framebuffer
        if (multiBufferSource instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        // Render hover snapped highlight box
        if (blockEntity.hasHoverInfo()) {
            int hFace = blockEntity.getHoverFace();
            if (hFace >= 0 && hFace < 4) {
                int px = blockEntity.getHoverPixelX();
                int py = blockEntity.getHoverPixelY();
                int brushSize = blockEntity.getHoverBrushSize();
                boolean erase = blockEntity.isHoverErase();

                int minX, maxX, minY, maxY;
                if (brushSize <= 1) {
                    minX = px;
                    maxX = px + 1;
                    minY = py;
                    maxY = py + 1;
                } else if (brushSize == 2) {
                    minX = px;
                    maxX = px + 2;
                    minY = py;
                    maxY = py + 2;
                } else {
                    int radius = brushSize / 2;
                    minX = px - radius;
                    maxX = px + radius + 1;
                    minY = py - radius;
                    maxY = py + radius + 1;
                }

                minX = Math.max(0, Math.min(16, minX));
                maxX = Math.max(0, Math.min(16, maxX));
                minY = Math.max(0, Math.min(16, minY));
                maxY = Math.max(0, Math.min(16, maxY));

                if (maxX > minX && maxY > minY) {
                    float x0 = minX / 16.0F;
                    float x1 = maxX / 16.0F;
                    float y1 = 1.0F - (minY / 16.0F); // top
                    float y0 = 1.0F - (maxY / 16.0F); // bottom

                    // entityNoOutline uses TRANSLUCENT_TRANSPARENCY with COLOR_WRITE (does not write depth buffer),
                    // ensuring it blends directly over the already-rendered pumpkin block without culling it
                    VertexConsumer hoverBuffer = multiBufferSource.getBuffer(RenderType.entityNoOutline(CarvedPumpkinTextureManager.getWhiteTexture()));
                    int highlightLight = net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;
                    renderHoverHighlight(poseStack, hoverBuffer, faceDirs[hFace], x0, y0, x1, y1, erase, highlightLight, combinedOverlay);

                    if (multiBufferSource instanceof MultiBufferSource.BufferSource bs) {
                        bs.endBatch();
                    }
                }
            }
        }

        poseStack.popPose();
    }

    private void renderHoverHighlight(PoseStack poseStack, VertexConsumer buffer, Direction faceDir,
                                      float x0, float y0, float x1, float y1,
                                      boolean eraseMode, int light, int overlay) {
        poseStack.pushPose();
        // Translate to center of block
        poseStack.translate(0.5, 0.5, 0.5);
        // Rotate around Y to match face direction (South is base 0 rotation)
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-faceDir.toYRot()));
        poseStack.translate(-0.5, -0.5, -0.5);

        Matrix4f mat = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Offset slightly forward (+0.0015f) to avoid Z-fighting with carved overlay (+0.0005f) and block face
        float z = 1.0015F;
        float nx = 0.0F, ny = 0.0F, nz = 1.0F;

        // Visual colors: neutral translucent gray for carving, subtle red-tinted translucent gray for erasing
        int fillR = eraseMode ? 220 : 190;
        int fillG = eraseMode ? 90 : 190;
        int fillB = eraseMode ? 90 : 190;
        int fillA = 135;

        int borderR = eraseMode ? 255 : 255;
        int borderG = eraseMode ? 140 : 255;
        int borderB = eraseMode ? 140 : 255;
        int borderA = 220;

        float b = 0.0035F; // Thin border width in model units (around 1/18th of a 16x16 pixel)

        // 1. Semi-transparent snapped box fill
        drawQuad(buffer, mat, normal, x0, y0, x1, y1, z, fillR, fillG, fillB, fillA, light, overlay, nx, ny, nz);

        // 2. Crisp outer border to emphasize pixel grid snapping
        float bz = z + 0.0002F;
        drawQuad(buffer, mat, normal, x0, y1 - b, x1, y1, bz, borderR, borderG, borderB, borderA, light, overlay, nx, ny, nz);
        drawQuad(buffer, mat, normal, x0, y0, x1, y0 + b, bz, borderR, borderG, borderB, borderA, light, overlay, nx, ny, nz);
        drawQuad(buffer, mat, normal, x0, y0, x0 + b, y1, bz, borderR, borderG, borderB, borderA, light, overlay, nx, ny, nz);
        drawQuad(buffer, mat, normal, x1 - b, y0, x1, y1, bz, borderR, borderG, borderB, borderA, light, overlay, nx, ny, nz);

        poseStack.popPose();
    }

    private static void drawQuad(VertexConsumer buffer, Matrix4f mat, Matrix3f normal,
                                 float minX, float minY, float maxX, float maxY, float z,
                                 int r, int g, int b, int a,
                                 int light, int overlay, float nx, float ny, float nz) {
        buffer.vertex(mat, minX, maxY, z).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(overlay).uv2(light).normal(normal, nx, ny, nz).endVertex();
        buffer.vertex(mat, minX, minY, z).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(overlay).uv2(light).normal(normal, nx, ny, nz).endVertex();
        buffer.vertex(mat, maxX, minY, z).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(overlay).uv2(light).normal(normal, nx, ny, nz).endVertex();
        buffer.vertex(mat, maxX, maxY, z).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(overlay).uv2(light).normal(normal, nx, ny, nz).endVertex();
    }
}
