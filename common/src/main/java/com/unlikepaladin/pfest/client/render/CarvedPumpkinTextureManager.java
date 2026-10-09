package com.unlikepaladin.pfest.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.unlikepaladin.pfest.PaladinFestivitiesMod;
import com.unlikepaladin.pfest.data.CarvedPumpkinData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarvedPumpkinTextureManager {
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, DynamicTexture> DYNAMIC_TEXTURES = new ConcurrentHashMap<>();

    // Unlit tones: #441300 cavity base, #2D0003 deep top/left shadow (matching vanilla carved_pumpkin)
    // Format in NativeImage.combine is (alpha, blue, green, red)
    private static final int UNLIT_BASE_COLOR   = NativeImage.combine(255, 0x00, 0x13, 0x44);
    private static final int UNLIT_SHADOW_COLOR = NativeImage.combine(255, 0x03, 0x00, 0x2D);

    // Lit tones: #FFE240 luminous yellow base, #E5881C warm shaded amber top/left shadow
    private static final int LIT_BASE_COLOR     = NativeImage.combine(255, 0x40, 0xE2, 0xFF);
    private static final int LIT_SHADOW_COLOR   = NativeImage.combine(255, 0x1C, 0x88, 0xE5);

    public static ResourceLocation getOrCreateTexture(CarvedPumpkinData data, int faceIndex, boolean isLit) {
        byte[] faceBytes = data.getFaceBytes(faceIndex);
        int res = data.getResolution();
        int hash = Arrays.hashCode(faceBytes) ^ (isLit ? 0x55555555 : 0xAAAAAAAA) ^ (res << 16);
        String key = "carved_face_" + faceIndex + "_" + Integer.toHexString(hash);

        return TEXTURE_CACHE.computeIfAbsent(key, k -> {
            NativeImage image = new NativeImage(NativeImage.Format.RGBA, res, res, false);

            for (int y = 0; y < res; y++) {
                for (int x = 0; x < res; x++) {
                    if (data.isCarved(faceIndex, x, y)) {
                        boolean topUncarved = (y == 0 || !data.isCarved(faceIndex, x, y - 1));
                        boolean leftUncarved = (x == 0 || !data.isCarved(faceIndex, x - 1, y));
                        int pixelColor = isLit
                                ? (topUncarved || leftUncarved ? LIT_SHADOW_COLOR : LIT_BASE_COLOR)
                                : (topUncarved || leftUncarved ? UNLIT_SHADOW_COLOR : UNLIT_BASE_COLOR);
                        image.setPixelRGBA(x, y, pixelColor);
                    } else {
                        image.setPixelRGBA(x, y, 0); // transparent
                    }
                }
            }

            DynamicTexture dynamicTexture = new DynamicTexture(image);
            ResourceLocation loc = new ResourceLocation(PaladinFestivitiesMod.MOD_ID, "dynamic/" + k);
            Minecraft.getInstance().getTextureManager().register(loc, dynamicTexture);
            DYNAMIC_TEXTURES.put(loc, dynamicTexture);
            return loc;
        });
    }

    public static void renderFaceOverlay(PoseStack poseStack, VertexConsumer buffer, Direction faceDir, int light, int overlay) {
        poseStack.pushPose();
        // Translate to center of block
        poseStack.translate(0.5, 0.5, 0.5);
        // Rotate around Y to match face direction (South is base 0 rotation)
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-faceDir.toYRot()));
        poseStack.translate(-0.5, -0.5, -0.5);

        Matrix4f mat = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Offset slightly forward (+0.0005f) to avoid Z-fighting with the base block quad
        float z = 1.0005f;
        float nx = 0.0f, ny = 0.0f, nz = 1.0f;

        // Quad vertices with normal UV coordinates
        // Facing South:
        // Top-Left: x = 0, y = 1, z
        buffer.vertex(mat, 0.0f, 1.0f, z).color(255, 255, 255, 255).uv(0.0f, 0.0f).overlayCoords(overlay).uv2(light).normal(normal, nx, ny, nz).endVertex();
        // Bottom-Left: x = 0, y = 0, z
        buffer.vertex(mat, 0.0f, 0.0f, z).color(255, 255, 255, 255).uv(0.0f, 1.0f).overlayCoords(overlay).uv2(light).normal(normal, nx, ny, nz).endVertex();
        // Bottom-Right: x = 1, y = 0, z
        buffer.vertex(mat, 1.0f, 0.0f, z).color(255, 255, 255, 255).uv(1.0f, 1.0f).overlayCoords(overlay).uv2(light).normal(normal, nx, ny, nz).endVertex();
        // Top-Right: x = 1, y = 1, z
        buffer.vertex(mat, 1.0f, 1.0f, z).color(255, 255, 255, 255).uv(1.0f, 0.0f).overlayCoords(overlay).uv2(light).normal(normal, nx, ny, nz).endVertex();

        poseStack.popPose();
    }
}
