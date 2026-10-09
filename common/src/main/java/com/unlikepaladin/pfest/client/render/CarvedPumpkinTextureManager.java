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
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarvedPumpkinTextureManager {
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, DynamicTexture> DYNAMIC_TEXTURES = new ConcurrentHashMap<>();

    private static final ResourceLocation LIT_TEMPLATE_LOCATION = new ResourceLocation(PaladinFestivitiesMod.MOD_ID, "textures/block/lit-carved-pumpkin.png");
    private static NativeImage litTemplate = null;

    // Unlit tones: #441300 cavity base, #2D0003 deep top/left shadow (matching vanilla carved_pumpkin)
    // Format in NativeImage.combine is (alpha, blue, green, red)
    private static final int UNLIT_BASE_COLOR   = NativeImage.combine(255, 0x00, 0x13, 0x44);
    private static final int UNLIT_SHADOW_COLOR = NativeImage.combine(255, 0x03, 0x00, 0x2D);

    // Fallback lit tone: #FFE240 luminous yellow
    private static final int LIT_FALLBACK_COLOR = NativeImage.combine(255, 0x40, 0xE2, 0xFF);

    public static synchronized NativeImage getLitTemplate() {
        if (litTemplate == null) {
            try {
                if (Minecraft.getInstance().getResourceManager().hasResource(LIT_TEMPLATE_LOCATION)) {
                    Resource resource = Minecraft.getInstance().getResourceManager().getResource(LIT_TEMPLATE_LOCATION);
                    litTemplate = NativeImage.read(resource.getInputStream());
                }
            } catch (Exception ignored) {
            }
            if (litTemplate == null) {
                try (InputStream stream = CarvedPumpkinTextureManager.class.getResourceAsStream("/assets/paladinsfestivities/textures/block/lit-carved-pumpkin.png")) {
                    if (stream != null) {
                        litTemplate = NativeImage.read(stream);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return litTemplate;
    }

    public static int getLitPixelArgb(int x, int y, int res) {
        NativeImage template = getLitTemplate();
        if (template != null) {
            int tx = (x * template.getWidth()) / res;
            int ty = (y * template.getHeight()) / res;
            tx = Math.min(Math.max(0, tx), template.getWidth() - 1);
            ty = Math.min(Math.max(0, ty), template.getHeight() - 1);
            int raw = template.getPixelRGBA(tx, ty);
            int a = NativeImage.getA(raw);
            int b = NativeImage.getB(raw);
            int g = NativeImage.getG(raw);
            int r = NativeImage.getR(raw);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
        return 0xFFFFE240;
    }

    public static ResourceLocation getOrCreateTexture(CarvedPumpkinData data, int faceIndex, boolean isLit) {
        byte[] faceBytes = data.getFaceBytes(faceIndex);
        int res = data.getResolution();
        int hash = Arrays.hashCode(faceBytes) ^ (isLit ? 0x55555555 : 0xAAAAAAAA) ^ (res << 16);
        String key = "carved_face_" + faceIndex + "_" + Integer.toHexString(hash);

        return TEXTURE_CACHE.computeIfAbsent(key, k -> {
            NativeImage image = new NativeImage(NativeImage.Format.RGBA, res, res, false);
            NativeImage template = isLit ? getLitTemplate() : null;

            for (int y = 0; y < res; y++) {
                for (int x = 0; x < res; x++) {
                    if (data.isCarved(faceIndex, x, y)) {
                        int pixelColor;
                        if (isLit) {
                            if (template != null) {
                                int tx = (x * template.getWidth()) / res;
                                int ty = (y * template.getHeight()) / res;
                                tx = Math.min(Math.max(0, tx), template.getWidth() - 1);
                                ty = Math.min(Math.max(0, ty), template.getHeight() - 1);
                                pixelColor = template.getPixelRGBA(tx, ty);
                            } else {
                                pixelColor = LIT_FALLBACK_COLOR;
                            }
                        } else {
                            boolean topUncarved = (y == 0 || !data.isCarved(faceIndex, x, y - 1));
                            boolean leftUncarved = (x == 0 || !data.isCarved(faceIndex, x - 1, y));
                            pixelColor = (topUncarved || leftUncarved) ? UNLIT_SHADOW_COLOR : UNLIT_BASE_COLOR;
                        }
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
