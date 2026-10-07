package com.unlikepaladin.pfest.blocks.blockentities;

import com.unlikepaladin.pfest.data.CarvedPumpkinData;
import com.unlikepaladin.pfest.items.CustomCarvedPumpkinItem;
import com.unlikepaladin.pfest.menu.PumpkinCarvingMenu;
import com.unlikepaladin.pfest.registry.ModBlockEntities;
import com.unlikepaladin.pfest.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PumpkinCarvingStationBlockEntity extends BlockEntity implements Clearable, MenuProvider {
    private ItemStack heldPumpkin = ItemStack.EMPTY;
    private final CarvedPumpkinData data = new CarvedPumpkinData();

    public PumpkinCarvingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PUMPKIN_CARVING_STATION.get(), pos, state);
    }

    public ItemStack getHeldPumpkin() {
        return this.heldPumpkin;
    }

    public CarvedPumpkinData getData() {
        return this.data;
    }

    public void setHeldPumpkin(ItemStack stack) {
        this.heldPumpkin = stack;
        if (stack.getItem() instanceof CustomCarvedPumpkinItem) {
            CompoundTag beTag = stack.getTagElement("BlockEntityTag");
            if (beTag != null) {
                this.data.loadFromNbt(beTag);
            } else {
                this.data.clearAll();
            }
        } else if (stack.is(Items.PUMPKIN)) {
            this.data.clearAll();
        }
        sync();
    }

    public boolean hasPumpkin() {
        return !this.heldPumpkin.isEmpty();
    }

    public void popPumpkin(Player player, InteractionHand hand) {
        if (!hasPumpkin()) return;

        ItemStack toDrop = this.heldPumpkin.copy();
        if (!this.data.isEmpty() && toDrop.is(Items.PUMPKIN)) {
            toDrop = new ItemStack(ModBlocks.CUSTOM_CARVED_PUMPKIN.get());
            CompoundTag beTag = new CompoundTag();
            this.data.saveToNbt(beTag);
            toDrop.addTagElement("BlockEntityTag", beTag);
        }

        if (player.getItemInHand(hand).isEmpty()) {
            player.setItemInHand(hand, toDrop);
        } else if (!player.getInventory().add(toDrop)) {
            player.drop(toDrop, false);
        }

        this.heldPumpkin = ItemStack.EMPTY;
        this.data.clearAll();
        sync();
    }

    public void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.paladinsfestivities.pumpkin_carving_station");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PumpkinCarvingMenu(containerId, playerInventory, this.worldPosition, this);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        if (!this.heldPumpkin.isEmpty()) {
            tag.put("PumpkinItem", this.heldPumpkin.save(new CompoundTag()));
        }
        this.data.saveToNbt(tag);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("PumpkinItem", 10)) {
            this.heldPumpkin = ItemStack.of(tag.getCompound("PumpkinItem"));
        } else {
            this.heldPumpkin = ItemStack.EMPTY;
        }
        this.data.loadFromNbt(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.save(tag);
        return tag;
    }

    @Override
    public void clearContent() {
        this.heldPumpkin = ItemStack.EMPTY;
        this.data.clearAll();
        sync();
    }
}