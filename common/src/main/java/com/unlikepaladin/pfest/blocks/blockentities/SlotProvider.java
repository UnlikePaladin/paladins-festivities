package com.unlikepaladin.pfest.blocks.blockentities;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

// referenced https://github.com/ToastTextures/cook-it/blob/1.20.1/src/main/java/dev/toasttextures/cookit/block/entity/SlotProvider.java
// by ToastTextures, thanks bestie
public interface SlotProvider<T extends Slot<?>> {
    List<T> getSlots();

    <E extends BlockEntity> T createSlot(Vec3 pos, E entity, int index);

    default Optional<T> getSlotAt(Vec3 clickPos) {
        return getSlots().stream()
                .filter(slot -> clickPos.distanceToSqr(slot.pos) < 0.25)
                .min(Comparator.comparingDouble(slot -> clickPos.distanceToSqr(slot.pos)));
    }

    default boolean fillAt(Player player, @NotNull Vec3 interactionPos, ItemStack stack) {
        Optional<T> slotOpt = getSlotAt(interactionPos);

        if (slotOpt.isEmpty()) return false;

        T slot = slotOpt.get();

        if (!(slot.attachedEntity instanceof Container container)) return false;
        if (!container.getItem(slot.index).isEmpty()) return false;
        ItemStack inserted = stack.copy();
        inserted.setCount(1);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        container.setItem(slot.index, inserted);
        container.setChanged();
        return true;
    }

    default ItemStack retrieve(@NotNull Vec3 interactionPos, Predicate<Item> exclusions) {
        Optional<T> slotOpt = getSlotAt(interactionPos);

        if (slotOpt.isEmpty()) return ItemStack.EMPTY;

        T slot = slotOpt.get();

        if (!(slot.attachedEntity instanceof Container container)) return ItemStack.EMPTY;
        ItemStack retrieved = container.getItem(slot.index);
        if (retrieved.isEmpty() || !exclusions.test(retrieved.getItem())) return ItemStack.EMPTY;
        ItemStack taken = retrieved.copy();
        taken.setCount(1);
        retrieved.shrink(1);
        if (retrieved.isEmpty()) {
            container.setItem(slot.index, ItemStack.EMPTY);
        }
        container.setChanged();
        return taken;
    }
}
