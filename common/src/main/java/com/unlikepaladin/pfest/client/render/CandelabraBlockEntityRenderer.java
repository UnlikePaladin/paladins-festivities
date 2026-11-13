package com.unlikepaladin.pfest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.unlikepaladin.pfest.blocks.CandelabraBlock;
import com.unlikepaladin.pfest.blocks.blockentities.CandelabraBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CandelabraBlockEntityRenderer implements BlockEntityRenderer<CandelabraBlockEntity> {
    private static final Map<ResourceLocation, ResourceLocation> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final ResourceLocation DEFAULT_CANDLE_TEXTURE = new ResourceLocation("minecraft", "textures/block/candle.png");

    public CandelabraBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static ResourceLocation getCandleTexture(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            ResourceLocation blockKey = Registry.BLOCK.getKey(blockItem.getBlock());
            return TEXTURE_CACHE.computeIfAbsent(blockKey, id -> new ResourceLocation(id.getNamespace(), "textures/block/" + id.getPath() + ".png"));
        }
        return DEFAULT_CANDLE_TEXTURE;
    }

    @Override
    public void render(CandelabraBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (!blockEntity.hasAnyCandles()) {
            return;
        }

        Direction facing = blockEntity.getBlockState().hasProperty(CandelabraBlock.FACING)
                ? blockEntity.getBlockState().getValue(CandelabraBlock.FACING)
                : Direction.NORTH;

        int light = blockEntity.getLevel() != null
                ? LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above())
                : combinedLight;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float yRot = switch (facing) {
            case NORTH -> 0.0F;
            case EAST -> -90.0F;
            case SOUTH -> -180.0F;
            case WEST -> -270.0F;
            default -> 0.0F;
        };
        poseStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
        poseStack.translate(-0.5, -0.5, -0.5);

        Matrix4f mat = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        for (int slot = 0; slot < CandelabraBlockEntity.SLOTS; slot++) {
            ItemStack candleStack = blockEntity.getItem(slot);
            if (candleStack.isEmpty()) continue;

            ResourceLocation texture = getCandleTexture(candleStack);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

            renderCandleSlot(consumer, mat, normal, slot, light, combinedOverlay);
        }

        poseStack.popPose();
    }

    private void renderCandleSlot(VertexConsumer consumer, Matrix4f mat, Matrix3f normal, int slot, int light, int overlay) {
        float x0, x1, y0, y1, z0, z1;
        float vSide1;
        float wickCx, wickCy, wickCz;

        switch (slot) {
            case 0 -> { // holder0
                x0 = 11.0F / 16.0F; x1 = 13.0F / 16.0F;
                y0 = 10.0F / 16.0F; y1 = 15.0F / 16.0F;
                z0 = 7.0F / 16.0F;  z1 = 9.0F / 16.0F;
                vSide1 = 13.0F / 16.0F;
                wickCx = 12.0F / 16.0F; wickCy = 15.0F / 16.0F; wickCz = 8.0F / 16.0F;
            }
            case 1 -> { // holder1 (middle)
                x0 = 7.0F / 16.0F; x1 = 9.0F / 16.0F;
                y0 = 11.0F / 16.0F; y1 = 17.0F / 16.0F;
                z0 = 7.0F / 16.0F;  z1 = 9.0F / 16.0F;
                vSide1 = 14.0F / 16.0F;
                wickCx = 8.0F / 16.0F; wickCy = 17.0F / 16.0F; wickCz = 8.0F / 16.0F;
            }
            case 2 -> { // holder2
                x0 = 3.0F / 16.0F; x1 = 5.0F / 16.0F;
                y0 = 10.0F / 16.0F; y1 = 14.0F / 16.0F;
                z0 = 7.0F / 16.0F;  z1 = 9.0F / 16.0F;
                vSide1 = 12.0F / 16.0F;
                wickCx = 4.0F / 16.0F; wickCy = 14.0F / 16.0F; wickCz = 8.0F / 16.0F;
            }
            default -> { return; }
        }

        float u0 = 0.0F;
        float u1 = 2.0F / 16.0F;
        float vSide0 = 8.0F / 16.0F;
        float vTop0 = 6.0F / 16.0F;
        float vTop1 = 8.0F / 16.0F;
        float vBottom0 = 14.0F / 16.0F;
        float vBottom1 = 16.0F / 16.0F;

        // Render candle body
        renderBox(consumer, mat, normal, x0, y0, z0, x1, y1, z1, u0, vSide0, u1, vSide1, vTop0, vTop1, vBottom0, vBottom1, light, overlay);

        // Render crossed wick quads
        float wickU0 = 0.0F;
        float wickU1 = 1.0F / 16.0F;
        float wickV0 = 5.0F / 16.0F;
        float wickV1 = 6.0F / 16.0F;
        float wickSize = 1.0F / 16.0F;

        renderCrossedQuad(consumer, mat, normal, wickCx, wickCy, wickCz, wickSize, wickSize, -45.0F, wickU0, wickV0, wickU1, wickV1, light, overlay);
        renderCrossedQuad(consumer, mat, normal, wickCx, wickCy, wickCz, wickSize, wickSize, 45.0F, wickU0, wickV0, wickU1, wickV1, light, overlay);
    }

    private void renderBox(VertexConsumer consumer, Matrix4f mat, Matrix3f normal,
                           float x0, float y0, float z0, float x1, float y1, float z1,
                           float u0, float vSide0, float u1, float vSide1,
                           float vTop0, float vTop1, float vBottom0, float vBottom1,
                           int light, int overlay) {
        // NORTH face (-Z)
        consumer.vertex(mat, x1, y1, z0).color(255, 255, 255, 255).uv(u0, vSide0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        consumer.vertex(mat, x1, y0, z0).color(255, 255, 255, 255).uv(u0, vSide1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        consumer.vertex(mat, x0, y0, z0).color(255, 255, 255, 255).uv(u1, vSide1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        consumer.vertex(mat, x0, y1, z0).color(255, 255, 255, 255).uv(u1, vSide0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();

        // SOUTH face (+Z)
        consumer.vertex(mat, x0, y1, z1).color(255, 255, 255, 255).uv(u0, vSide0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
        consumer.vertex(mat, x0, y0, z1).color(255, 255, 255, 255).uv(u0, vSide1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
        consumer.vertex(mat, x1, y0, z1).color(255, 255, 255, 255).uv(u1, vSide1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
        consumer.vertex(mat, x1, y1, z1).color(255, 255, 255, 255).uv(u1, vSide0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();

        // WEST face (-X)
        consumer.vertex(mat, x0, y1, z0).color(255, 255, 255, 255).uv(u0, vSide0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
        consumer.vertex(mat, x0, y0, z0).color(255, 255, 255, 255).uv(u0, vSide1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
        consumer.vertex(mat, x0, y0, z1).color(255, 255, 255, 255).uv(u1, vSide1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
        consumer.vertex(mat, x0, y1, z1).color(255, 255, 255, 255).uv(u1, vSide0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();

        // EAST face (+X)
        consumer.vertex(mat, x1, y1, z1).color(255, 255, 255, 255).uv(u0, vSide0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
        consumer.vertex(mat, x1, y0, z1).color(255, 255, 255, 255).uv(u0, vSide1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
        consumer.vertex(mat, x1, y0, z0).color(255, 255, 255, 255).uv(u1, vSide1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
        consumer.vertex(mat, x1, y1, z0).color(255, 255, 255, 255).uv(u1, vSide0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();

        // UP face (+Y)
        consumer.vertex(mat, x0, y1, z0).color(255, 255, 255, 255).uv(u0, vTop0).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(mat, x0, y1, z1).color(255, 255, 255, 255).uv(u0, vTop1).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(mat, x1, y1, z1).color(255, 255, 255, 255).uv(u1, vTop1).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(mat, x1, y1, z0).color(255, 255, 255, 255).uv(u1, vTop0).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();

        // DOWN face (-Y)
        consumer.vertex(mat, x0, y0, z1).color(255, 255, 255, 255).uv(u0, vBottom0).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();
        consumer.vertex(mat, x0, y0, z0).color(255, 255, 255, 255).uv(u0, vBottom1).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();
        consumer.vertex(mat, x1, y0, z0).color(255, 255, 255, 255).uv(u1, vBottom1).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();
        consumer.vertex(mat, x1, y0, z1).color(255, 255, 255, 255).uv(u1, vBottom0).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();
    }

    private void renderCrossedQuad(VertexConsumer consumer, Matrix4f mat, Matrix3f normal,
                                   float cx, float cy, float cz, float width, float height, float angleDeg,
                                   float u0, float v0, float u1, float v1,
                                   int light, int overlay) {
        float rad = (float) Math.toRadians(angleDeg);
        float hw = width / 2.0F;
        float dx = hw * (float) Math.cos(rad);
        float dz = hw * (float) Math.sin(rad);

        float xA = cx - dx;
        float zA = cz - dz;
        float xB = cx + dx;
        float zB = cz + dz;

        float y0 = cy;
        float y1 = cy + height;

        float nx = -(float) Math.sin(rad);
        float nz = (float) Math.cos(rad);

        // Front face
        consumer.vertex(mat, xA, y1, zA).color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, nx, 0, nz).endVertex();
        consumer.vertex(mat, xA, y0, zA).color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, nx, 0, nz).endVertex();
        consumer.vertex(mat, xB, y0, zB).color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, nx, 0, nz).endVertex();
        consumer.vertex(mat, xB, y1, zB).color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, nx, 0, nz).endVertex();

        // Back face
        consumer.vertex(mat, xB, y1, zB).color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, -nx, 0, -nz).endVertex();
        consumer.vertex(mat, xB, y0, zB).color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, -nx, 0, -nz).endVertex();
        consumer.vertex(mat, xA, y0, zA).color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, -nx, 0, -nz).endVertex();
        consumer.vertex(mat, xA, y1, zA).color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, -nx, 0, -nz).endVertex();
    }
}
