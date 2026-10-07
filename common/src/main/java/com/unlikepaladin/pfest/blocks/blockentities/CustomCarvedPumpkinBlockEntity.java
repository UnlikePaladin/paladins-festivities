package com.unlikepaladin.pfest.blocks.blockentities;

import com.unlikepaladin.pfest.data.CarvedPumpkinData;
import com.unlikepaladin.pfest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CustomCarvedPumpkinBlockEntity extends BlockEntity {
    private final CarvedPumpkinData data = new CarvedPumpkinData();

    public CustomCarvedPumpkinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CUSTOM_CARVED_PUMPKIN.get(), pos, state);
    }

    public CarvedPumpkinData getData() {
        return this.data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        this.data.saveToNbt(tag);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
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

    public void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
