package com.unlikepaladin.pfest.menu;

import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import com.unlikepaladin.pfest.data.CarvedPumpkinData;
import com.unlikepaladin.pfest.registry.MenuTypes;
import com.unlikepaladin.pfest.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class PumpkinCarvingMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    @Nullable
    private final PumpkinCarvingStationBlockEntity station;
    private final CarvedPumpkinData localData = new CarvedPumpkinData();

    // Client-side constructor
    public PumpkinCarvingMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readBlockPos(), null);
    }

    // Server-side & internal constructor
    public PumpkinCarvingMenu(int containerId, Inventory playerInv, BlockPos pos, @Nullable PumpkinCarvingStationBlockEntity station) {
        super(MenuTypes.PUMPKIN_MENU.get(), containerId);
        this.pos = pos;
        if (station == null && playerInv.player.level != null && playerInv.player.level.getBlockEntity(pos) instanceof PumpkinCarvingStationBlockEntity s) {
            this.station = s;
        } else {
            this.station = station;
        }

        if (this.station != null) {
            this.localData.copyFrom(this.station.getData());
        }
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Nullable
    public PumpkinCarvingStationBlockEntity getStation() {
        return this.station;
    }

    public CarvedPumpkinData getData() {
        if (this.station != null) {
            return this.station.getData();
        }
        return this.localData;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.pos == null || player.level == null) {
            return false;
        }
        BlockState state = player.level.getBlockState(this.pos);
        if (!state.is(ModBlocks.PUMPKIN_CARVING_STATION.get())) {
            return false;
        }
        return player.distanceToSqr(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level != null && !player.level.isClientSide && this.pos != null) {
            BlockState state = player.level.getBlockState(this.pos);
            if (state.is(ModBlocks.PUMPKIN_CARVING_STATION.get())) {
                player.level.setBlock(this.pos, state.setValue(BlockStateProperties.OCCUPIED, false), 3);
            }
        }
    }
}
