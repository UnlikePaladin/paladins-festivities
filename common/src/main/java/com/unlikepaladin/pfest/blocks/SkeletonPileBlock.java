package com.unlikepaladin.pfest.blocks;

import com.unlikepaladin.pfest.utilities.PFMShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class SkeletonPileBlock extends HorizontalDirectionalBlock {
    public static final IntegerProperty MODEL = IntegerProperty.create("model", 1, 3);

    // Model 1: base + skull on the left (North-facing)
    private static final VoxelShape MODEL_1_NORTH = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(1.0D, 0.0D, 3.0D, 10.0D, 8.0D, 12.0D)
    );

    // Model 2: base + skull on the right (North-facing)
    private static final VoxelShape MODEL_2_NORTH = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(8.0D, 0.0D, 3.0D, 16.0D, 8.0D, 12.0D)
    );

    // Model 3: flat ribs and bones (height 5)
    private static final VoxelShape MODEL_3_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D);

    private static final Map<Direction, VoxelShape> MODEL_1_SHAPES = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> MODEL_2_SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            MODEL_1_SHAPES.put(direction, PFMShapeUtil.rotateShape(Direction.NORTH, direction, MODEL_1_NORTH));
            MODEL_2_SHAPES.put(direction, PFMShapeUtil.rotateShape(Direction.NORTH, direction, MODEL_2_NORTH));
        }
    }

    public SkeletonPileBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(MODEL, 1));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int model = state.getValue(MODEL);
        Direction facing = state.getValue(FACING);
        if (model == 1) {
            return MODEL_1_SHAPES.getOrDefault(facing, MODEL_1_NORTH);
        } else if (model == 2) {
            return MODEL_2_SHAPES.getOrDefault(facing, MODEL_2_NORTH);
        } else {
            return MODEL_3_SHAPE;
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int current = state.getValue(MODEL);
        int next = (current % 3) + 1; // 1 -> 2 -> 3 -> 1
        level.setBlock(pos, state.setValue(MODEL, next), 3);
        level.playSound(player, pos, SoundEvents.BONE_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
