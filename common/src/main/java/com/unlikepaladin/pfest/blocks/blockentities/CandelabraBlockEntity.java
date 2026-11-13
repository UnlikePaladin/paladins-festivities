package com.unlikepaladin.pfest.blocks.blockentities;

import com.unlikepaladin.pfest.blocks.CandelabraBlock;
import com.unlikepaladin.pfest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CandelabraBlockEntity extends BlockEntity implements Container, SlotProvider<Slot<CandelabraBlockEntity>> {
    public static final int SLOTS = 3;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

    public CandelabraBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CANDELABRA.get(), pos, state);
    }

    @Override
    public List<Slot<CandelabraBlockEntity>> getSlots() {
        Direction facing = getBlockState().hasProperty(CandelabraBlock.FACING)
                ? getBlockState().getValue(CandelabraBlock.FACING)
                : Direction.NORTH;
        return List.of(
                createSlot(CandelabraBlock.getSlotOffset(0, facing), this, 0),
                createSlot(CandelabraBlock.getSlotOffset(1, facing), this, 1),
                createSlot(CandelabraBlock.getSlotOffset(2, facing), this, 2)
        );
    }

    @Override
    public <E extends BlockEntity> Slot<CandelabraBlockEntity> createSlot(Vec3 pos, E entity, int index) {
        return new Slot<>(pos, this, index);
    }

    public boolean hasAnyCandles() {
        for (ItemStack item : this.items) {
            if (!item.isEmpty()) return true;
        }
        return false;
    }

    public int getCandleCount() {
        int count = 0;
        for (ItemStack item : this.items) {
            if (!item.isEmpty()) count++;
        }
        return count;
    }

    public void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(CandelabraBlock.CANDLES)) {
                int count = getCandleCount();
                boolean lit = state.getValue(CandelabraBlock.LIT) && count > 0;
                if (state.getValue(CandelabraBlock.CANDLES) != count || state.getValue(CandelabraBlock.LIT) != lit) {
                    this.level.setBlock(this.worldPosition, state.setValue(CandelabraBlock.CANDLES, count).setValue(CandelabraBlock.LIT, lit), 3);
                }
            }
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public int getContainerSize() {
        return SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return !hasAnyCandles();
    }

    @Override
    public ItemStack getItem(int slot) {
        return (slot >= 0 && slot < SLOTS) ? this.items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < SLOTS) {
            this.items.set(slot, stack);
            if (stack.getCount() > this.getMaxStackSize()) {
                stack.setCount(this.getMaxStackSize());
            }
            this.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return CandelabraBlock.isCandle(stack);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items);
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
}
