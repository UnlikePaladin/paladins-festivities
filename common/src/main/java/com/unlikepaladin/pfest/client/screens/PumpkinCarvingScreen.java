package com.unlikepaladin.pfest.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.unlikepaladin.pfest.data.CarvedPumpkinData;
import com.unlikepaladin.pfest.menu.PumpkinCarvingMenu;
import com.unlikepaladin.pfest.network.NetworkPlatform;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PumpkinCarvingScreen extends AbstractContainerScreen<PumpkinCarvingMenu> {
    private static final ResourceLocation PUMPKIN_TEXTURE = new ResourceLocation("minecraft", "textures/block/pumpkin_side.png");
    private static final String[] FACE_NAMES = new String[]{"Front Face", "Right Face", "Back Face", "Left Face"};

    private int currentFace = 0;
    private int brushSize = 1;
    private boolean eraseMode = false;
    private boolean previewLit = false;
    private boolean isDragging = false;
    private boolean faceDirty = false;
    private long lastPacketTime = 0;

    private Button toolButton;
    private Button brushButton;
    private Button litButton;

    public PumpkinCarvingScreen(PumpkinCarvingMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 250;
        this.imageHeight = 210;
    }

    @Override
    protected void init() {
        super.init();
        NetworkPlatform.sendOccupiedToServer(this.menu.getPos(), true);

        int canvasX = this.leftPos + 58;
        int canvasY = this.topPos + 38;

        // Face navigation arrows
        this.addRenderableWidget(new Button(canvasX + 6, this.topPos + 18, 16, 16, new TextComponent("<"), b -> {
            flushCarvePacket();
            this.currentFace = (this.currentFace + 3) % 4;
        }));

        this.addRenderableWidget(new Button(canvasX + 128 - 22, this.topPos + 18, 16, 16, new TextComponent(">"), b -> {
            flushCarvePacket();
            this.currentFace = (this.currentFace + 1) % 4;
        }));

        // Left sidebar tools
        int buttonX = this.leftPos + 6;
        this.toolButton = this.addRenderableWidget(new Button(buttonX, canvasY, 46, 20, new TextComponent("Carve"), b -> {
            this.eraseMode = !this.eraseMode;
            this.toolButton.setMessage(new TextComponent(this.eraseMode ? "Erase" : "Carve"));
        }));

        this.brushButton = this.addRenderableWidget(new Button(buttonX, canvasY + 24, 46, 20, new TextComponent("1x1"), b -> {
            this.brushSize = (this.brushSize % 3) + 1;
            this.brushButton.setMessage(new TextComponent(this.brushSize + "x" + this.brushSize));
        }));

        this.litButton = this.addRenderableWidget(new Button(buttonX, canvasY + 48, 46, 20, new TextComponent("Unlit"), b -> {
            this.previewLit = !this.previewLit;
            this.litButton.setMessage(new TextComponent(this.previewLit ? "Lit" : "Unlit"));
        }));

        this.addRenderableWidget(new Button(buttonX, canvasY + 72, 46, 20, new TextComponent("Clear"), b -> {
            this.menu.getData().clearFace(this.currentFace);
            this.faceDirty = true;
            flushCarvePacket();
        }));

        // Done button
        this.addRenderableWidget(new Button(this.leftPos + 192, this.topPos + 182, 50, 20, new TextComponent("Done"), b -> {
            flushCarvePacket();
            NetworkPlatform.sendFinishCarveToServer(this.menu.getPos());
            this.onClose();
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        int canvasX = this.leftPos + 58;
        int canvasY = this.topPos + 38;
        int canvasSize = 128;

        // Background studio container
        fill(poseStack, this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF231C16);
        drawBorder(poseStack, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFF4A3B2C);

        // Canvas frame
        fill(poseStack, canvasX - 2, canvasY - 2, canvasX + canvasSize + 2, canvasY + canvasSize + 2, 0xFF14100C);

        // Render actual vanilla/resource pack pumpkin side texture
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PUMPKIN_TEXTURE);
        blit(poseStack, canvasX, canvasY, 0.0F, 0.0F, canvasSize, canvasSize, canvasSize, canvasSize);

        CarvedPumpkinData data = this.menu.getData();
        int res = data.getResolution();
        int cellSize = canvasSize / res;

        // Render carved pixels on top of the pumpkin texture with top/left shadow bevel
        int unlitBase = 0xFF441300;
        int unlitShadow = 0xFF2D0003;
        int litBase = 0xFFFFE240;
        int litShadow = 0xFFE5881C;

        for (int y = 0; y < res; y++) {
            for (int x = 0; x < res; x++) {
                if (data.isCarved(this.currentFace, x, y)) {
                    boolean topUncarved = (y == 0 || !data.isCarved(this.currentFace, x, y - 1));
                    boolean leftUncarved = (x == 0 || !data.isCarved(this.currentFace, x - 1, y));
                    int pixelColor = this.previewLit
                            ? (topUncarved || leftUncarved ? litShadow : litBase)
                            : (topUncarved || leftUncarved ? unlitShadow : unlitBase);
                    fill(poseStack, canvasX + x * cellSize, canvasY + y * cellSize, canvasX + (x + 1) * cellSize, canvasY + (y + 1) * cellSize, pixelColor);
                }
            }
        }

        // Grid lines for precision carving
        for (int i = 1; i < res; i++) {
            fill(poseStack, canvasX, canvasY + i * cellSize, canvasX + canvasSize, canvasY + i * cellSize + 1, 0x2A000000);
            fill(poseStack, canvasX + i * cellSize, canvasY, canvasX + i * cellSize + 1, canvasY + canvasSize, 0x2A000000);
        }

        // Hover cursor on canvas
        if (mouseX >= canvasX && mouseX < canvasX + canvasSize && mouseY >= canvasY && mouseY < canvasY + canvasSize) {
            int hx = (mouseX - canvasX) / cellSize;
            int hy = (mouseY - canvasY) / cellSize;
            drawBrushHover(poseStack, canvasX, canvasY, cellSize, hx, hy, res);
        }
    }

    private void drawBrushHover(PoseStack poseStack, int canvasX, int canvasY, int cellSize, int hx, int hy, int res) {
        int color = 0x55FFFFFF;
        if (this.brushSize <= 1) {
            fill(poseStack, canvasX + hx * cellSize, canvasY + hy * cellSize, canvasX + (hx + 1) * cellSize, canvasY + (hy + 1) * cellSize, color);
        } else if (this.brushSize == 2) {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    int px = hx + dx, py = hy + dy;
                    if (px < res && py < res) {
                        fill(poseStack, canvasX + px * cellSize, canvasY + py * cellSize, canvasX + (px + 1) * cellSize, canvasY + (py + 1) * cellSize, color);
                    }
                }
            }
        } else {
            int radius = this.brushSize / 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int px = hx + dx, py = hy + dy;
                    if (px >= 0 && px < res && py >= 0 && py < res) {
                        fill(poseStack, canvasX + px * cellSize, canvasY + py * cellSize, canvasX + (px + 1) * cellSize, canvasY + (py + 1) * cellSize, color);
                    }
                }
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        drawCenteredString(poseStack, this.font, new TextComponent("Pumpkin Carving Studio"), this.imageWidth / 2, 6, 0xFFE0B0);
        drawCenteredString(poseStack, this.font, new TextComponent(FACE_NAMES[this.currentFace]), this.imageWidth / 2, 22, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInsideCanvas(mouseX, mouseY)) {
            this.isDragging = true;
            handleCanvasClick(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.isDragging && isInsideCanvas(mouseX, mouseY)) {
            handleCanvasClick(mouseX, mouseY);
            if (System.currentTimeMillis() - this.lastPacketTime > 120) {
                flushCarvePacket();
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isDragging) {
            this.isDragging = false;
            flushCarvePacket();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isInsideCanvas(double mouseX, double mouseY) {
        int canvasX = this.leftPos + 58;
        int canvasY = this.topPos + 38;
        return mouseX >= canvasX && mouseX < canvasX + 128 && mouseY >= canvasY && mouseY < canvasY + 128;
    }

    private void handleCanvasClick(double mouseX, double mouseY) {
        int canvasX = this.leftPos + 58;
        int canvasY = this.topPos + 38;
        CarvedPumpkinData data = this.menu.getData();
        int res = data.getResolution();
        int cellSize = 128 / res;

        int cx = (int) (mouseX - canvasX) / cellSize;
        int cy = (int) (mouseY - canvasY) / cellSize;

        data.carveBrush(this.currentFace, cx, cy, this.brushSize, !this.eraseMode);
        this.faceDirty = true;
    }

    private void flushCarvePacket() {
        if (!this.faceDirty) return;
        byte[] faceBytes = this.menu.getData().getFaceBytes(this.currentFace);
        NetworkPlatform.sendCarveToServer(this.menu.getPos(), this.currentFace, faceBytes);
        this.faceDirty = false;
        this.lastPacketTime = System.currentTimeMillis();
    }

    private void drawBorder(PoseStack poseStack, int x, int y, int width, int height, int color) {
        fill(poseStack, x, y, x + width, y + 1, color);
        fill(poseStack, x, y + height - 1, x + width, y + height, color);
        fill(poseStack, x, y + 1, x + 1, y + height - 1, color);
        fill(poseStack, x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    @Override
    public void onClose() {
        flushCarvePacket();
        NetworkPlatform.sendOccupiedToServer(this.menu.getPos(), false);
        super.onClose();
    }
}
