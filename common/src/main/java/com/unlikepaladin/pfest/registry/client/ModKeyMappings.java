package com.unlikepaladin.pfest.registry.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {
    public static final String CATEGORY = "keybindings.category.paladinsfestivities";

    public static KeyMapping ROTATE_TURN_TABLE_LEFT = new KeyMapping(
            "key.paladinsfestivities.rotateTurnTableLeft",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Q,
            CATEGORY
    );

    public static KeyMapping ROTATE_TURN_TABLE_RIGHT = new KeyMapping(
            "key.paladinsfestivities.rotateTurnTableRight",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            CATEGORY
    );

    public static KeyMapping CARVE_TOOL_KNIFE = new KeyMapping(
            "key.paladinsfestivities.carveToolKnife",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_1,
            CATEGORY
    );

    public static KeyMapping CARVE_TOOL_SCOOP = new KeyMapping(
            "key.paladinsfestivities.carveToolScoop",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_2,
            CATEGORY
    );

    public static KeyMapping CARVE_BRUSH_SIZE = new KeyMapping(
            "key.paladinsfestivities.carveBrushSize",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_3,
            CATEGORY
    );

    public static KeyMapping CARVE_TOGGLE_LIT = new KeyMapping(
            "key.paladinsfestivities.carveToggleLit",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_4,
            CATEGORY
    );

    public static KeyMapping CARVE_TOGGLE_CONTROLS = new KeyMapping(
            "key.paladinsfestivities.carveToggleControls",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    );

    public static void init() {
        registerKeyMapping(ROTATE_TURN_TABLE_LEFT);
        registerKeyMapping(ROTATE_TURN_TABLE_RIGHT);
        registerKeyMapping(CARVE_TOOL_KNIFE);
        registerKeyMapping(CARVE_TOOL_SCOOP);
        registerKeyMapping(CARVE_BRUSH_SIZE);
        registerKeyMapping(CARVE_TOGGLE_LIT);
        registerKeyMapping(CARVE_TOGGLE_CONTROLS);
    }

    @ExpectPlatform
    public static KeyMapping registerKeyMapping(KeyMapping mapping) {
        throw new AssertionError();
    }
}
