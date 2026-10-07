package com.unlikepaladin.pfest.blocks.blockentities;

import com.unlikepaladin.pfest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PumpkinCarvingStationBlockEntity extends BlockEntity implements Clearable {
    private ItemStack heldPumpkin = ItemStack.EMPTY;

    public PumpkinCarvingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PUMPKIN_CARVING_STATION.get(), pos, state);
    }

    public ItemStack getHeldPumpkin() {
        return this.heldPumpkin;
    }

    public void setHeldPumpkin(ItemStack stack) {
        this.heldPumpkin = stack;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            // Sync to clients so the item renders on the table
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public boolean hasPumpkin() {
        return !this.heldPumpkin.isEmpty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        if (!this.heldPumpkin.isEmpty()) {
            tag.put("PumpkinItem", this.heldPumpkin.save(new CompoundTag()));
        }
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
    }

    // Standard client synchronization packets
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
        this.setChanged();
    }
}