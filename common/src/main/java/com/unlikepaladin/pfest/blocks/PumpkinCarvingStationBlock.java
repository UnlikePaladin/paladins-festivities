package com.unlikepaladin.pfest.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.unlikepaladin.pfest.blocks.blockentities.PumpkinCarvingStationBlockEntity;
import com.unlikepaladin.pfest.items.CustomCarvedPumpkinItem;
import com.unlikepaladin.pfest.menu.MenuHelper;
import com.unlikepaladin.pfest.utilities.PFMShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.unlikepaladin.pfest.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PumpkinCarvingStationBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public PumpkinCarvingStationBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BlockStateProperties.OCCUPIED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(BlockStateProperties.OCCUPIED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.defaultBlockState()
                .setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.OCCUPIED, false);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.getBlockEntity(blockPos) instanceof PumpkinCarvingStationBlockEntity carvingStation) {
            ItemStack stack = player.getItemInHand(interactionHand);

            // Pop out pumpkin: sneak + right click with empty hand
            if (player.isShiftKeyDown() && stack.isEmpty() && carvingStation.hasPumpkin()) {
                if (!level.isClientSide) {
                    carvingStation.popPumpkin(player, interactionHand);
                    level.playSound(null, blockPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            // Place pumpkin onto station
            if (!carvingStation.hasPumpkin() && (stack.is(Items.PUMPKIN) || stack.getItem() instanceof CustomCarvedPumpkinItem)) {
                if (!level.isClientSide) {
                    ItemStack placed = stack.split(1);
                    carvingStation.setHeldPumpkin(placed);
                    level.playSound(null, blockPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            // Open screen if occupied by pumpkin
            if (carvingStation.hasPumpkin()) {
                if (blockState.getValue(BlockStateProperties.OCCUPIED)) {
                    // Already in use by another player
                    return InteractionResult.CONSUME;
                }
                if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                    level.setBlock(blockPos, blockState.setValue(BlockStateProperties.OCCUPIED, true), 3);
                    MenuHelper.openPumpkinCarvingMenu(serverPlayer, carvingStation);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof PumpkinCarvingStationBlockEntity station) {
                if (station.hasPumpkin()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), station.getHeldPumpkin());
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new PumpkinCarvingStationBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide && blockEntityType == ModBlockEntities.PUMPKIN_CARVING_STATION.get()
                ? (lvl, pos, st, be) -> {
                    if (be instanceof PumpkinCarvingStationBlockEntity station) {
                        station.clientTick();
                    }
                }
                : null;
    }

    protected static final Map<Direction, VoxelShape> SHAPES = Maps.newHashMap(ImmutableMap.of(
            Direction.NORTH, Shapes.or(box(1, 0, 1, 15, 13, 15), box(0, 13, 0, 16, 16, 16))
    ));

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES.computeIfAbsent(blockState.getValue(FACING), direction ->
                PFMShapeUtil.rotateShape(Direction.NORTH, direction, SHAPES.get(Direction.NORTH))
        );
    }
}
