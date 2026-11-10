package com.unlikepaladin.pfest.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.unlikepaladin.pfest.blocks.PumpkinCarvingStationBlock;
import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import com.unlikepaladin.pfest.data.CarvedPumpkinData;
import com.unlikepaladin.pfest.menu.PumpkinCarvingMenu;
import com.unlikepaladin.pfest.network.NetworkPlatform;
import com.unlikepaladin.pfest.registry.client.ModKeyMappings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class PumpkinCarvingScreen extends AbstractContainerScreen<PumpkinCarvingMenu> {
    private static final ResourceLocation TOOLS_TEXTURE = new ResourceLocation(
            "paladinsfestivities",
            "textures/gui/tools_carving_table.png"
    );

    private static final String[] FACE_KEYS = new String[]{
            "gui.paladinsfestivities.pumpkin_carving.face.front",
            "gui.paladinsfestivities.pumpkin_carving.face.right",
            "gui.paladinsfestivities.pumpkin_carving.face.back",
            "gui.paladinsfestivities.pumpkin_carving.face.left"
    };

    private static final Component TITLE_TEXT = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.title")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    private static final Component ARROW_LEFT = new TextComponent("<").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
    private static final Component ARROW_RIGHT = new TextComponent(">").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);

    private static final Component TOOL_KNIFE = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.tool.knife")
            .withStyle(ChatFormatting.WHITE);
    private static final Component TOOL_SCOOP = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.tool.scoop")
            .withStyle(ChatFormatting.WHITE);
    private static final Component TOOL_LIT = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.tool.lit")
            .withStyle(ChatFormatting.WHITE);
    private static final Component TOOL_UNLIT = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.tool.unlit")
            .withStyle(ChatFormatting.WHITE);

    private static final Component FINISH_LABEL = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.finish")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    private static final Component FINISH_DESC = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.finish.desc")
            .withStyle(ChatFormatting.GRAY);

    private static final Component CONTROLS_TITLE = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.controls.title")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD);
    private static final Component CONTROLS_CARVE = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.controls.carve")
            .withStyle(ChatFormatting.DARK_GRAY);
    private static final Component CONTROLS_SPIN = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.controls.spin")
            .withStyle(ChatFormatting.DARK_GRAY);
    private static final Component CONTROLS_EXIT = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.controls.exit")
            .withStyle(ChatFormatting.DARK_GRAY);

    private static Component getKeyHint(KeyMapping mapping, ChatFormatting color) {
        return new TextComponent("[").append(mapping.getTranslatedKeyMessage()).append("]").withStyle(color);
    }

    private int brushSize = 1;
    private boolean eraseMode = false;
    private boolean previewLit = false;
    private boolean isCarving = false;
    private boolean isRightDragging = false;
    private final boolean[] dirtyFaces = new boolean[4];
    private boolean showControlsCard = true;
    private long lastPacketTime = 0;
    private long lastCarveSoundTime = 0;
    private boolean cursorHidden = false;

    // Smooth camera positioning without modifying player position or fov
    private Vec3 startCamPos = Vec3.ZERO;
    private float startYaw = 0.0F;
    private float startPitch = 0.0F;
    private long openTime = 0L;

    // Raycast hit result
    private boolean hasRayHit = false;
    private int rayHitFace = 0;
    private int rayHitPixelX = 0;
    private int rayHitPixelY = 0;
    private Vec3 rayHitWorldPos = null;

    public PumpkinCarvingScreen(PumpkinCarvingMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 0;
        this.imageHeight = 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        NetworkPlatform.sendOccupiedToServer(this.menu.getPos(), true);

        Player player = this.minecraft.player;
        if (player != null) {
            this.startCamPos = player.getEyePosition();
            this.startYaw = player.getViewYRot(1.0F);
            this.startPitch = player.getViewXRot(1.0F);
        }
        this.openTime = System.currentTimeMillis();

        PumpkinCarvingStationBlockEntity station = getStationEntity();
        if (station != null) {
            station.setPreviewLit(this.previewLit);
        }
    }

    public Vec3 getCustomCameraPos(float partialTicks) {
        BlockPos pos = this.menu.getPos();
        if (this.minecraft == null || this.minecraft.level == null || pos == null) {
            return null;
        }

        BlockState state = this.minecraft.level.getBlockState(pos);
        if (!state.is(com.unlikepaladin.pfest.registry.ModBlocks.PUMPKIN_CARVING_STATION.get())) {
            return null;
        }

        Direction facing = state.getValue(PumpkinCarvingStationBlock.FACING);
        // Ideal camera position: 1.35 blocks in front of the table, slightly elevated at eye level
        Vec3 targetCamPos = new Vec3(
                pos.getX() + 0.5D + facing.getStepX() * 1.35D,
                pos.getY() + 1.45D,
                pos.getZ() + 0.5D + facing.getStepZ() * 1.35D
        );

        float progress = Mth.clamp((System.currentTimeMillis() - this.openTime) / 300.0F, 0.0F, 1.0F);
        float t = progress * progress * (3.0F - 2.0F * progress); // smoothstep
        return this.startCamPos.lerp(targetCamPos, t);
    }

    public float getCustomCameraYaw(float partialTicks) {
        BlockPos pos = this.menu.getPos();
        if (this.minecraft == null || this.minecraft.level == null || pos == null) {
            return this.startYaw;
        }

        BlockState state = this.minecraft.level.getBlockState(pos);
        if (!state.is(com.unlikepaladin.pfest.registry.ModBlocks.PUMPKIN_CARVING_STATION.get())) {
            return this.startYaw;
        }

        Direction facing = state.getValue(PumpkinCarvingStationBlock.FACING);
        float targetYaw = facing.getOpposite().toYRot();

        float progress = Mth.clamp((System.currentTimeMillis() - this.openTime) / 300.0F, 0.0F, 1.0F);
        float t = progress * progress * (3.0F - 2.0F * progress);
        return Mth.rotLerp(t, this.startYaw, targetYaw);
    }

    public float getCustomCameraPitch(float partialTicks) {
        // Looking down slightly at the pumpkin from 1.45m height to 1.25m center over 1.35m distance (~8.5 degrees)
        float targetPitch = 8.5F;

        float progress = Mth.clamp((System.currentTimeMillis() - this.openTime) / 300.0F, 0.0F, 1.0F);
        float t = progress * progress * (3.0F - 2.0F * progress);
        return Mth.lerp(t, this.startPitch, targetPitch);
    }

    private PumpkinCarvingStationBlockEntity getStationEntity() {
        if (this.menu.getStation() != null) {
            return this.menu.getStation();
        }
        if (this.minecraft != null && this.minecraft.level != null && this.menu.getPos() != null) {
            if (this.minecraft.level.getBlockEntity(this.menu.getPos()) instanceof PumpkinCarvingStationBlockEntity s) {
                return s;
            }
        }
        return null;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // 1. Update OS cursor visibility based on whether mouse is over UI or in world
        updateCursorVisibility(mouseX, mouseY);

        // 2. Perform 3D raycast from mouse through the overridden camera to in-world pumpkin face
        performRaycast(mouseX, mouseY);

        // 3. Render screen UI elements
        super.render(poseStack, mouseX, mouseY, partialTicks);

        // 4. Render minimal in-world HUD overlays
        renderHud(poseStack, mouseX, mouseY);

        // 5. Render active tool icon at mouse cursor (when over world)
        renderMouseTool(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        // Subtle top and bottom cinema vignette gradients so the 3D world stays completely visible
        fillGradient(poseStack, 0, 0, this.width, 50, 0x88000000, 0x00000000);
        fillGradient(poseStack, 0, this.height - 65, this.width, this.height, 0x00000000, 0xAA000000);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Handled in renderHud
    }

    private void performRaycast(double mouseX, double mouseY) {
        this.hasRayHit = false;
        this.rayHitWorldPos = null;
        PumpkinCarvingStationBlockEntity station = getStationEntity();
        if (station == null) return;

        if (isMouseOverUi(mouseX, mouseY)) {
            station.clearHoverInfo();
            return;
        }

        BlockPos pos = this.menu.getPos();
        if (this.minecraft == null || pos == null || this.minecraft.level == null) {
            station.clearHoverInfo();
            return;
        }

        BlockState state = this.minecraft.level.getBlockState(pos);
        if (!state.is(com.unlikepaladin.pfest.registry.ModBlocks.PUMPKIN_CARVING_STATION.get())) {
            station.clearHoverInfo();
            return;
        }

        Direction facing = state.getValue(PumpkinCarvingStationBlock.FACING);
        float partialTicks = this.minecraft != null ? this.minecraft.getFrameTime() : 1.0F;
        float turntableAngle = station.getRenderTurntableAngle(partialTicks);

        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        double fov = this.minecraft.options.fov;
        double aspect = (double) this.minecraft.getWindow().getWidth() / (double) this.minecraft.getWindow().getHeight();
        double tanHalfFov = Math.tan(Math.toRadians(fov) / 2.0D);

        double ndcX = (mouseX / (double) this.width) * 2.0D - 1.0D;
        double ndcY = 1.0D - (mouseY / (double) this.height) * 2.0D;

        Vec3 forward = new Vec3(camera.getLookVector());
        Vec3 up = new Vec3(camera.getUpVector());
        Vec3 left = new Vec3(camera.getLeftVector());

        Vec3 rayOrigin = camera.getPosition();
        Vec3 rayDir = forward.subtract(left.scale(ndcX * aspect * tanHalfFov)).add(up.scale(ndcY * tanHalfFov)).normalize();

        // Pumpkin geometry directly on top of table surface
        Vec3 pumpkinCenter = new Vec3(pos.getX() + 0.5D, pos.getY() + 1.004D + 0.35D, pos.getZ() + 0.5D);
        double halfSize = 0.35D;

        // Local face directions matching PumpkinCarverBlockEntityRenderer
        Direction[] faceDirs = new Direction[]{
                Direction.SOUTH,
                Direction.WEST,
                Direction.NORTH,
                Direction.EAST
        };

        double bestT = Double.MAX_VALUE;
        int bestFace = -1;
        double bestU = 0, bestV = 0;

        for (int i = 0; i < 4; i++) {
            Vec3 localNormal = new Vec3(faceDirs[i].getStepX(), faceDirs[i].getStepY(), faceDirs[i].getStepZ());
            // Turntable rotates positively around Y matching poseStack.mulPose(Vector3f.YP.rotationDegrees(turntableAngle))
            Vec3 worldNormal = localNormal.yRot((float) Math.toRadians(-facing.toYRot() + turntableAngle));

            double denom = rayDir.dot(worldNormal);
            // Only test faces directed towards the camera
            if (denom >= -0.01D) continue;

            Vec3 faceCenter = pumpkinCenter.add(worldNormal.scale(halfSize));
            double t = faceCenter.subtract(rayOrigin).dot(worldNormal) / denom;
            if (t <= 0.0D || t >= bestT) continue;

            Vec3 hit = rayOrigin.add(rayDir.scale(t));
            Vec3 diff = hit.subtract(faceCenter);
            Vec3 rightVector = up.cross(worldNormal);

            double lx = diff.dot(rightVector);
            double ly = diff.y;

            if (Math.abs(lx) <= halfSize && Math.abs(ly) <= halfSize) {
                bestT = t;
                bestFace = i;
                bestU = (lx + halfSize) / (2.0D * halfSize);
                bestV = 1.0D - (ly + halfSize) / (2.0D * halfSize);
            }
        }

        if (bestFace >= 0) {
            this.hasRayHit = true;
            this.rayHitFace = bestFace;
            this.rayHitWorldPos = rayOrigin.add(rayDir.scale(bestT));
            int res = this.menu.getData().getResolution();
            this.rayHitPixelX = Mth.clamp((int) (bestU * res), 0, res - 1);
            this.rayHitPixelY = Mth.clamp((int) (bestV * res), 0, res - 1);
            station.setHoverInfo(this.rayHitFace, this.rayHitPixelX, this.rayHitPixelY, this.brushSize, this.eraseMode);
        } else {
            this.rayHitWorldPos = null;
            station.clearHoverInfo();
        }
    }

    private void updateCursorVisibility(double mouseX, double mouseY) {
        if (this.minecraft == null) return;
        long window = this.minecraft.getWindow().getWindow();
        boolean inWindow = mouseX >= 0 && mouseY >= 0 && mouseX <= this.width && mouseY <= this.height;
        boolean shouldHide = inWindow && !isMouseOverUi(mouseX, mouseY);

        if (shouldHide != this.cursorHidden) {
            this.cursorHidden = shouldHide;
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, shouldHide ? GLFW.GLFW_CURSOR_HIDDEN : GLFW.GLFW_CURSOR_NORMAL);
        }
    }

    private void restoreCursor() {
        if (this.cursorHidden && this.minecraft != null) {
            this.cursorHidden = false;
            long window = this.minecraft.getWindow().getWindow();
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
    }

    private boolean isMouseOverUi(double mouseX, double mouseY) {
        // Bottom toolbar shelf
        if (mouseY >= this.height - 48) return true;
        // Finish button
        if (mouseX >= this.width - 124 && mouseY >= this.height - 46) return true;
        // Turntable turn arrows
        int arrowY = this.height / 2 - 16;
        if (mouseY >= arrowY && mouseY < arrowY + 32) {
            if (mouseX >= 16 && mouseX < 52) return true;
            if (mouseX >= this.width - 52 && mouseX < this.width - 16) return true;
        }
        // Controls guide card
        if (this.showControlsCard && mouseX >= this.width - 142 && mouseY <= 96) return true;
        return false;
    }

    private void renderHud(PoseStack poseStack, int mouseX, int mouseY) {
        PumpkinCarvingStationBlockEntity station = getStationEntity();
        int activeFace = station != null ? station.getActiveFaceIndex() : 0;

        // 1. Top Title & Face Indicator
        drawCenteredString(poseStack, this.font, TITLE_TEXT, this.width / 2, 10, 0xFFFFFFFF);
        Component faceName = new TranslatableComponent(FACE_KEYS[activeFace]).withStyle(ChatFormatting.YELLOW);
        Component faceIndicator = new TranslatableComponent(
                "gui.paladinsfestivities.pumpkin_carving.face_indicator",
                faceName,
                activeFace + 1
        ).withStyle(ChatFormatting.GRAY);
        drawCenteredString(poseStack, this.font, faceIndicator, this.width / 2, 24, 0xFFFFFFFF);

        // 2. Turntable Side Arrows
        int arrowY = this.height / 2 - 16;
        renderArrowButton(poseStack, 16, arrowY, 36, 32, ARROW_LEFT, getKeyHint(ModKeyMappings.ROTATE_TURN_TABLE_LEFT, ChatFormatting.GRAY), mouseX, mouseY);
        renderArrowButton(poseStack, this.width - 52, arrowY, 36, 32, ARROW_RIGHT, getKeyHint(ModKeyMappings.ROTATE_TURN_TABLE_RIGHT, ChatFormatting.GRAY), mouseX, mouseY);

        // 3. Bottom Shelf Tool Rack
        renderToolbar(poseStack, mouseX, mouseY);

        // 4. "Finish Carving" Button
        renderDoneButton(poseStack, mouseX, mouseY);

        // 5. Controls Card
        if (this.showControlsCard) {
            renderControlsCard(poseStack);
        }
    }

    private void renderArrowButton(PoseStack poseStack, int x, int y, int w, int h, Component arrow, Component keyHint, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bgColor = hover ? 0xDD4A3525 : 0xAA2A1E14;
        int borderColor = hover ? 0xFFFFD700 : 0xFF5A442E;

        fill(poseStack, x, y, x + w, y + h, bgColor);
        drawBorder(poseStack, x, y, w, h, borderColor);

        drawCenteredString(poseStack, this.font, arrow, x + w / 2, y + 6, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, keyHint, x + w / 2, y + 19, 0xAAAAAA);
    }

    private int getToolbarStartX(int totalW) {
        int doneW = 104;
        int doneX = this.width - doneW - 16;
        int startX = Math.min((this.width - totalW) / 2, doneX - totalW - 16);
        return Math.max(8, startX);
    }

    private void renderToolbar(PoseStack poseStack, int mouseX, int mouseY) {
        int slotW = 44;
        int slotH = 34;
        int gap = 6;
        int totalW = 4 * slotW + 3 * gap;
        int startX = getToolbarStartX(totalW);
        int startY = this.height - 44;

        // Shelf backing
        fill(poseStack, startX - 8, startY - 4, startX + totalW + 8, startY + slotH + 4, 0xDD1C140E);
        drawBorder(poseStack, startX - 8, startY - 4, totalW + 16, slotH + 8, 0xFF4A3525);

        // Slot 1: Knife (Carve)
        renderToolSlot(poseStack, startX, startY, slotW, slotH, TOOL_KNIFE, getKeyHint(ModKeyMappings.CARVE_TOOL_KNIFE, ChatFormatting.GOLD), !this.eraseMode, mouseX, mouseY);
        // Slot 2: Spoon (Scoop/Erase)
        renderToolSlot(poseStack, startX + slotW + gap, startY, slotW, slotH, TOOL_SCOOP, getKeyHint(ModKeyMappings.CARVE_TOOL_SCOOP, ChatFormatting.GOLD), this.eraseMode, mouseX, mouseY);
        // Slot 3: Brush/Chisel Size
        Component brushLabel = new TranslatableComponent("gui.paladinsfestivities.pumpkin_carving.tool.brush", this.brushSize)
                .withStyle(ChatFormatting.WHITE);
        renderToolSlot(poseStack, startX + (slotW + gap) * 2, startY, slotW, slotH, brushLabel, getKeyHint(ModKeyMappings.CARVE_BRUSH_SIZE, ChatFormatting.GOLD), false, mouseX, mouseY);
        // Slot 4: Candle (Lit Preview)
        Component litLabel = this.previewLit ? TOOL_LIT : TOOL_UNLIT;
        renderToolSlot(poseStack, startX + (slotW + gap) * 3, startY, slotW, slotH, litLabel, getKeyHint(ModKeyMappings.CARVE_TOGGLE_LIT, ChatFormatting.GOLD), this.previewLit, mouseX, mouseY);
    }

    private void renderToolSlot(PoseStack poseStack, int x, int y, int w, int h, Component label, Component key, boolean active, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bgColor = active ? 0xDD5C3E1F : (hover ? 0xBB3B2817 : 0xAA241910);
        int borderColor = active ? 0xFFFFD700 : (hover ? 0xFFC0A060 : 0xFF503822);

        fill(poseStack, x, y, x + w, y + h, bgColor);
        drawBorder(poseStack, x, y, w, h, borderColor);

        drawCenteredString(poseStack, this.font, label, x + w / 2, y + 5, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, key, x + w / 2, y + 19, 0xFFE080);
    }

    private void renderDoneButton(PoseStack poseStack, int mouseX, int mouseY) {
        int w = 104;
        int h = 32;
        int x = this.width - w - 16;
        int y = this.height - h - 10;

        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bgColor = hover ? 0xEE7A380A : 0xDD562604;
        int borderColor = hover ? 0xFFFFE060 : 0xFFD87818;

        fill(poseStack, x, y, x + w, y + h, bgColor);
        drawBorder(poseStack, x, y, w, h, borderColor);

        drawCenteredString(poseStack, this.font, FINISH_LABEL, x + w / 2, y + 7, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, FINISH_DESC, x + w / 2, y + 18, 0xCCCCCC);
    }

    private void renderControlsCard(PoseStack poseStack) {
        int w = 126;
        int h = 80;
        int x = this.width - w - 12;
        int y = 12;

        fill(poseStack, x, y, x + w, y + h, 0xDDF3E5CE); // warm parchment
        drawBorder(poseStack, x, y, w, h, 0xFF6D5238);

        Component turnControls = new TranslatableComponent(
                "gui.paladinsfestivities.pumpkin_carving.controls.turn",
                ModKeyMappings.ROTATE_TURN_TABLE_LEFT.getTranslatedKeyMessage(),
                ModKeyMappings.ROTATE_TURN_TABLE_RIGHT.getTranslatedKeyMessage()
        ).withStyle(ChatFormatting.DARK_GRAY);

        Component toolControls = new TranslatableComponent(
                "gui.paladinsfestivities.pumpkin_carving.controls.tools",
                ModKeyMappings.CARVE_TOOL_KNIFE.getTranslatedKeyMessage(),
                ModKeyMappings.CARVE_TOGGLE_LIT.getTranslatedKeyMessage()
        ).withStyle(ChatFormatting.DARK_GRAY);

        this.font.draw(poseStack, CONTROLS_TITLE, x + 8, y + 6, 0x222222);
        this.font.draw(poseStack, CONTROLS_CARVE, x + 8, y + 20, 0x333333);
        this.font.draw(poseStack, CONTROLS_SPIN, x + 8, y + 32, 0x333333);
        this.font.draw(poseStack, turnControls, x + 8, y + 44, 0x333333);
        this.font.draw(poseStack, toolControls, x + 8, y + 56, 0x333333);
        this.font.draw(poseStack, CONTROLS_EXIT, x + 8, y + 68, 0x333333);
    }

    private void renderMouseTool(PoseStack poseStack, int mouseX, int mouseY) {
        if (isMouseOverUi(mouseX, mouseY)) {
            return;
        }
        if (mouseX < 0 || mouseY < 0 || mouseX > this.width || mouseY > this.height) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TOOLS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float u = this.eraseMode ? 16.0F : 0.0F;
        float v = 0.0F;

        // Render at larger, clear 32x32 size (2x integer scale)
        int toolSize = 32;

        // Position icon so the cutting tip is anchored near the cursor and naturally centered vertically
        // Knife tip in 16x16 is (10, 3) -> in 32x32 it is (20, 6)
        // Scoop tip in 16x16 is (9, 5) -> in 32x32 it is (18, 10)
        // By centering vertically (offsetting Y by 12-14px), the tool doesn't hang low below the cursor
        int tipOffsetX = this.eraseMode ? 18 : 20;
        int tipOffsetY = this.eraseMode ? 8 : 5;

        int iconX = mouseX - tipOffsetX;
        int iconY = mouseY - tipOffsetY;

        poseStack.pushPose();
        poseStack.translate(0, 0, 300);
        blit(poseStack, iconX, iconY, toolSize, toolSize, u, v, 16, 16, 32, 16);
        poseStack.popPose();

        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 1. Check side arrow buttons
        int arrowY = this.height / 2 - 16;
        if (mouseX >= 16 && mouseX < 52 && mouseY >= arrowY && mouseY < arrowY + 32) {
            rotateTurntableLeft();
            return true;
        }
        if (mouseX >= this.width - 52 && mouseX < this.width - 16 && mouseY >= arrowY && mouseY < arrowY + 32) {
            rotateTurntableRight();
            return true;
        }

        // 2. Check toolbar slots
        int slotW = 44;
        int slotH = 34;
        int gap = 6;
        int totalW = 4 * slotW + 3 * gap;
        int startX = getToolbarStartX(totalW);
        int startY = this.height - 44;

        if (mouseY >= startY && mouseY < startY + slotH) {
            if (mouseX >= startX && mouseX < startX + slotW) {
                this.eraseMode = false;
                playUiClick();
                return true;
            } else if (mouseX >= startX + slotW + gap && mouseX < startX + (slotW + gap) + slotW) {
                this.eraseMode = true;
                playUiClick();
                return true;
            } else if (mouseX >= startX + (slotW + gap) * 2 && mouseX < startX + (slotW + gap) * 2 + slotW) {
                this.brushSize = (this.brushSize % 3) + 1;
                playUiClick();
                return true;
            } else if (mouseX >= startX + (slotW + gap) * 3 && mouseX < startX + (slotW + gap) * 3 + slotW) {
                toggleLitPreview();
                return true;
            }
        }

        // 3. Check Finish button
        int doneW = 104;
        int doneH = 32;
        int doneX = this.width - doneW - 16;
        int doneY = this.height - doneH - 10;
        if (mouseX >= doneX && mouseX < doneX + doneW && mouseY >= doneY && mouseY < doneY + doneH) {
            finishCarving();
            return true;
        }

        // 4. In-world carving interaction
        if (button == 0 && this.hasRayHit) {
            this.isCarving = true;
            applyCarveAtCursor();
            return true;
        }

        // 5. Right-click or middle-click drag to turn turntable
        if (button == 1 || button == 2) {
            this.isRightDragging = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.isCarving) {
            performRaycast(mouseX, mouseY);
            if (this.hasRayHit) {
                applyCarveAtCursor();
                if (System.currentTimeMillis() - this.lastPacketTime > 100) {
                    flushCarvePackets();
                }
            }
            return true;
        }

        if ((button == 1 || button == 2) && this.isRightDragging) {
            PumpkinCarvingStationBlockEntity station = getStationEntity();
            if (station != null) {
                station.rotateTurntable((float) dragX * 0.9F);
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isCarving) {
            this.isCarving = false;
            flushCarvePackets();
            return true;
        }
        if ((button == 1 || button == 2) && this.isRightDragging) {
            this.isRightDragging = false;
            PumpkinCarvingStationBlockEntity station = getStationEntity();
            if (station != null) {
                NetworkPlatform.sendRotationToServer(this.menu.getPos(), station.getTargetTurntableAngle());
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeyMappings.ROTATE_TURN_TABLE_LEFT.matches(keyCode, scanCode) || keyCode == GLFW.GLFW_KEY_LEFT) {
            rotateTurntableLeft();
            return true;
        } else if (ModKeyMappings.ROTATE_TURN_TABLE_RIGHT.matches(keyCode, scanCode) || keyCode == GLFW.GLFW_KEY_RIGHT) {
            rotateTurntableRight();
            return true;
        } else if (ModKeyMappings.CARVE_TOOL_KNIFE.matches(keyCode, scanCode)) {
            this.eraseMode = false;
            playUiClick();
            return true;
        } else if (ModKeyMappings.CARVE_TOOL_SCOOP.matches(keyCode, scanCode)) {
            this.eraseMode = true;
            playUiClick();
            return true;
        } else if (ModKeyMappings.CARVE_BRUSH_SIZE.matches(keyCode, scanCode)) {
            this.brushSize = (this.brushSize % 3) + 1;
            playUiClick();
            return true;
        } else if (ModKeyMappings.CARVE_TOGGLE_LIT.matches(keyCode, scanCode)) {
            toggleLitPreview();
            return true;
        } else if (ModKeyMappings.CARVE_TOGGLE_CONTROLS.matches(keyCode, scanCode)) {
            this.showControlsCard = !this.showControlsCard;
            playUiClick();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void rotateTurntableLeft() {
        PumpkinCarvingStationBlockEntity station = getStationEntity();
        if (station != null) {
            station.rotateTurntableSnapped(false);
            NetworkPlatform.sendRotationToServer(this.menu.getPos(), station.getTargetTurntableAngle());
            playWoodClick(0.9F);
        }
    }

    private void rotateTurntableRight() {
        PumpkinCarvingStationBlockEntity station = getStationEntity();
        if (station != null) {
            station.rotateTurntableSnapped(true);
            NetworkPlatform.sendRotationToServer(this.menu.getPos(), station.getTargetTurntableAngle());
            playWoodClick(1.1F);
        }
    }

    private void toggleLitPreview() {
        this.previewLit = !this.previewLit;
        PumpkinCarvingStationBlockEntity station = getStationEntity();
        if (station != null) {
            station.setPreviewLit(this.previewLit);
        }
        playUiClick();
    }

    private void applyCarveAtCursor() {
        if (!this.hasRayHit) return;
        CarvedPumpkinData data = this.menu.getData();
        data.carveBrush(this.rayHitFace, this.rayHitPixelX, this.rayHitPixelY, this.brushSize, !this.eraseMode);
        this.dirtyFaces[this.rayHitFace] = true;

        long now = System.currentTimeMillis();
        if (now - this.lastCarveSoundTime > 140) {
            playCarveSound();
            this.lastCarveSoundTime = now;
        }

        spawnCarveParticles();
    }

    private void spawnCarveParticles() {
        if (this.minecraft == null || this.minecraft.particleEngine == null || this.rayHitWorldPos == null) {
            return;
        }
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PUMPKIN.defaultBlockState());
        int count = Math.min(3, Math.max(1, this.brushSize));
        for (int i = 0; i < count; i++) {
            double vx = (Math.random() - 0.5) * 0.06;
            double vy = Math.random() * 0.05 + 0.02;
            double vz = (Math.random() - 0.5) * 0.06;
            double ox = (Math.random() - 0.5) * 0.02;
            double oy = (Math.random() - 0.5) * 0.02;
            double oz = (Math.random() - 0.5) * 0.02;
            net.minecraft.client.particle.Particle p = this.minecraft.particleEngine.createParticle(
                    particle,
                    this.rayHitWorldPos.x + ox,
                    this.rayHitWorldPos.y + oy,
                    this.rayHitWorldPos.z + oz,
                    vx, vy, vz
            );
            if (p != null) {
                float scale = 0.15F + (float) Math.random() * 0.1F;
                p.scale(scale);
            }
        }
    }

    private void flushCarvePackets() {
        for (int f = 0; f < 4; f++) {
            if (this.dirtyFaces[f]) {
                byte[] faceBytes = this.menu.getData().getFaceBytes(f);
                NetworkPlatform.sendCarveToServer(this.menu.getPos(), f, faceBytes);
                this.dirtyFaces[f] = false;
            }
        }
        this.lastPacketTime = System.currentTimeMillis();
    }

    private void finishCarving() {
        PumpkinCarvingStationBlockEntity station = getStationEntity();
        if (station != null) {
            NetworkPlatform.sendRotationToServer(this.menu.getPos(), station.getTargetTurntableAngle());
        }
        // Send all 4 faces before finishing
        for (int f = 0; f < 4; f++) {
            byte[] faceBytes = this.menu.getData().getFaceBytes(f);
            NetworkPlatform.sendCarveToServer(this.menu.getPos(), f, faceBytes);
            this.dirtyFaces[f] = false;
        }
        NetworkPlatform.sendFinishCarveToServer(this.menu.getPos());
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PUMPKIN_CARVE, 1.0F));
        }
        this.onClose();
    }

    private void playWoodClick(float pitch) {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, pitch));
        }
    }

    private void playUiClick() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void playCarveSound() {
        if (this.minecraft != null) {
            float pitch = 1.2F + (float) (Math.random() * 0.4F);
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOOD_HIT, pitch));
        }
    }

    private void drawBorder(PoseStack poseStack, int x, int y, int width, int height, int color) {
        fill(poseStack, x, y, x + width, y + 1, color);
        fill(poseStack, x, y + height - 1, x + width, y + height, color);
        fill(poseStack, x, y + 1, x + 1, y + height - 1, color);
        fill(poseStack, x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    @Override
    public void onClose() {
        restoreCursor();
        PumpkinCarvingStationBlockEntity station = getStationEntity();
        if (station != null) {
            station.clearHoverInfo();
            NetworkPlatform.sendRotationToServer(this.menu.getPos(), station.getTargetTurntableAngle());
        }
        flushCarvePackets();
        NetworkPlatform.sendOccupiedToServer(this.menu.getPos(), false);
        super.onClose();
    }

    @Override
    public void removed() {
        restoreCursor();
        super.removed();
    }
}
