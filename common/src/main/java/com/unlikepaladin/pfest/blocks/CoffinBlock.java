package com.unlikepaladin.pfest.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CoffinBlock extends BedBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    public CoffinBlock(BlockBehaviour.Properties properties) {
        super(DyeColor.BLACK, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PART, BedPart.FOOT)
                .setValue(OCCUPIED, false)
                .setValue(OPEN, false));
    }

    public static Direction getNeighbourDirection(BedPart bedPart, Direction direction) {
        return bedPart == BedPart.FOOT ? direction : direction.getOpposite();
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        // Shift-click toggles open/closed
        if (player.isSecondaryUseActive()) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            if (blockState.getValue(OCCUPIED)) {
                return InteractionResult.CONSUME;
            }

            BlockPos otherPos = blockPos.relative(getNeighbourDirection(blockState.getValue(PART), blockState.getValue(FACING)));
            BlockState otherState = level.getBlockState(otherPos);

            // If either half is open, close both. If both are closed, open both.
            boolean anyOpen = blockState.getValue(OPEN) || (otherState.is(this) && otherState.getValue(OPEN));
            boolean newOpen = !anyOpen;

            level.setBlock(blockPos, blockState.setValue(OPEN, newOpen), 3);
            if (otherState.is(this)) {
                level.setBlock(otherPos, otherState.setValue(OPEN, newOpen), 3);
            }

            SoundEvent sound = newOpen ? SoundEvents.WOODEN_TRAPDOOR_OPEN : SoundEvents.WOODEN_TRAPDOOR_CLOSE;
            level.playSound(null, blockPos, sound, SoundSource.BLOCKS, 1.0F, 0.75F);
            return InteractionResult.SUCCESS;
        }

        // Normal right-click: Sleeping behavior
        if (!level.isClientSide) {
            // When sleeping to avoid clipping, ensure head is open and foot is closed like an open casket
            BlockPos headPos = blockState.getValue(PART) == BedPart.HEAD ? blockPos : blockPos.relative(blockState.getValue(FACING));
            BlockPos footPos = headPos.relative(blockState.getValue(FACING).getOpposite());
            BlockState headState = level.getBlockState(headPos);
            BlockState footState = level.getBlockState(footPos);

            if (headState.is(this)) {
                level.setBlock(headPos, headState.setValue(OPEN, true), 3);
            }
            if (footState.is(this)) {
                level.setBlock(footPos, footState.setValue(OPEN, false), 3);
            }
        }

        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }

    @Override
    public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, 1.0F, DamageSource.FALL);
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter blockGetter, Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
    }

    @Override
    public long getSeed(BlockState blockState, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.relative(blockState.getValue(FACING), blockState.getValue(PART) == BedPart.HEAD ? 0 : 1);
        return Mth.getSeed(blockPos2.getX(), blockPos.getY(), blockPos2.getZ());
    }
}
