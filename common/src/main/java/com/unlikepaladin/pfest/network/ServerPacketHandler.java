package com.unlikepaladin.pfest.network;

import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import com.unlikepaladin.pfest.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ServerPacketHandler {
    public static void handleCarve(ServerPlayer player, BlockPos pos, int faceIndex, byte[] faceData) {
        if (player == null || player.level == null) return;
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
            return;
        }
        if (player.level.getBlockEntity(pos) instanceof PumpkinCarvingStationBlockEntity station) {
            station.getData().setFaceBytes(faceIndex, faceData);
            station.sync();
        }
    }

    public static void handleOccupied(ServerPlayer player, BlockPos pos, boolean occupied) {
        if (player == null || player.level == null) return;
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
            return;
        }
        BlockState state = player.level.getBlockState(pos);
        if (state.is(ModBlocks.PUMPKIN_CARVING_STATION.get())) {
            player.level.setBlock(pos, state.setValue(BlockStateProperties.OCCUPIED, occupied), 3);
        }
    }

    public static void handleFinishCarve(ServerPlayer player, BlockPos pos) {
        if (player == null || player.level == null) return;
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
            return;
        }
        if (player.level.getBlockEntity(pos) instanceof PumpkinCarvingStationBlockEntity station) {
            if (station.hasPumpkin()) {
                ItemStack finished = new ItemStack(ModBlocks.CUSTOM_CARVED_PUMPKIN.get());
                CompoundTag beTag = new CompoundTag();
                station.getData().saveToNbt(beTag);
                finished.addTagElement("BlockEntityTag", beTag);
                station.setHeldPumpkin(finished);
                player.level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
                station.sync();
            }
        }
    }

    public static void handleRotation(ServerPlayer player, BlockPos pos, float angle) {
        if (player == null || player.level == null) return;
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
            return;
        }
        if (player.level.getBlockEntity(pos) instanceof PumpkinCarvingStationBlockEntity station) {
            station.setTurntableTarget(angle);
            station.setChanged();
        }
    }
}
