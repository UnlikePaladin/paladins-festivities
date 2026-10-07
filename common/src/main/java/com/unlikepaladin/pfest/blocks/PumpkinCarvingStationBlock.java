package com.unlikepaladin.pfest.blocks;

import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PumpkinCarvingStationBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public PumpkinCarvingStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(BlockStateProperties.OCCUPIED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack stack =  player.getItemInHand(interactionHand);
        PumpkinCarvingStationBlockEntity carvingStationBlock = level.getBlockEntity(blockPos)
                instanceof PumpkinCarvingStationBlockEntity bE ? bE : null;
        if (carvingStationBlock != null && carvingStationBlock.hasPumpkin() && player.getItemInHand(interactionHand).isEmpty()) {
            ItemStack pumpkin = carvingStationBlock.getHeldPumpkin();
            carvingStationBlock.setHeldPumpkin(ItemStack.EMPTY);
            player.setItemInHand(interactionHand, pumpkin);
            return InteractionResult.CONSUME;
        } else if (stack.getItem() == Items.PUMPKIN) {
            ItemStack placed = stack.split(1);
            if (carvingStationBlock != null) {
                carvingStationBlock.setHeldPumpkin(placed);
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide) {
            openCarvingScreen(pos, station.getHeldPumpkin());
        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof PumpkinCarvingStationBlockEntity station) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), station.getHeldPumpkin());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new PumpkinCarvingStationBlockEntity(blockPos, blockState);
    }
}
