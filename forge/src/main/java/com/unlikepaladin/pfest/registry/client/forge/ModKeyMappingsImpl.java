package com.unlikepaladin.pfest.registry.client.forge;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

public class ModKeyMappingsImpl {
    public static KeyMapping registerKeyMapping(KeyMapping mapping) {
        mapping.setKeyConflictContext(KeyConflictContext.GUI);
        ClientRegistry.registerKeyBinding(mapping);
        return mapping;
    }
}
