package com.unlikepaladin.pfest.blocks;

import com.unlikepaladin.pfest.blocks.blockentities.CandelabraBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class CandelabraBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final IntegerProperty CANDLES = IntegerProperty.create("candles", 0, 3);
    public static final BooleanProperty DUSTY = BooleanProperty.create("dusty");

    private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(3.0, 0.0, 6.0, 13.0, 18.0, 10.0);
    private static final VoxelShape SHAPE_EAST_WEST = Block.box(6.0, 0.0, 3.0, 10.0, 18.0, 13.0);

    public CandelabraBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(CANDLES, 0)
                .setValue(DUSTY, false));
    }

    public static boolean isCandle(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.CANDLES)) return true;
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CandleBlock;
    }

    public static Vec3 getSlotOffset(int index, Direction facing) {
        double dy = (index == 1) ? (11.0 / 16.0) : (10.0 / 16.0);
        double offsetLateral = (index == 0) ? 0.25 : (index == 2) ? -0.25 : 0.0;
        double dx = 0.0;
        double dz = 0.0;

        switch (facing) {
            case NORTH -> dx = offsetLateral;
            case SOUTH -> dx = -offsetLateral;
            case EAST -> dz = offsetLateral;
            case WEST -> dz = -offsetLateral;
            default -> dx = offsetLateral;
        }
        return new Vec3(0.5 + dx, dy, 0.5 + dz);
    }

    public static Vec3 getWickOffset(int index, Direction facing) {
        double dy = switch (index) {
            case 0 -> 1.0;
            case 1 -> 1.125;
            case 2 -> 0.9375;
            default -> 1.0;
        };
        double offsetLateral = (index == 0) ? 0.25 : (index == 2) ? -0.25 : 0.0;
        double dx = 0.0;
        double dz = 0.0;

        switch (facing) {
            case NORTH -> dx = offsetLateral;
            case SOUTH -> dx = -offsetLateral;
            case EAST -> dz = offsetLateral;
            case WEST -> dz = -offsetLateral;
            default -> dx = offsetLateral;
        }
        return new Vec3(0.5 + dx, dy, 0.5 + dz);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return (facing == Direction.NORTH || facing == Direction.SOUTH) ? SHAPE_NORTH_SOUTH : SHAPE_EAST_WEST;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, CANDLES, DUSTY);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CandelabraBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CandelabraBlockEntity candelabra)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        // 1. Cobweb application -> becomes dusty
        if (held.is(Items.COBWEB)) {
            if (!state.getValue(DUSTY)) {
                level.setBlock(pos, state.setValue(DUSTY, true), 3);
                level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                if (!player.isCreative()) {
                    held.shrink(1);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }

        // 2. Shears -> remove cobweb
        if (held.is(Items.SHEARS)) {
            if (state.getValue(DUSTY)) {
                level.setBlock(pos, state.setValue(DUSTY, false), 3);
                level.playSound(player, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                popResource(level, pos, new ItemStack(Items.COBWEB));
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        // 3. Light with flint and steel
        if (held.is(Items.FLINT_AND_STEEL)) {
            if (!state.getValue(LIT) && candelabra.hasAnyCandles()) {
                level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                level.setBlock(pos, state.setValue(LIT, true), 11);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }

        // 4. Light with fire charge
        if (held.is(Items.FIRE_CHARGE)) {
            if (!state.getValue(LIT) && candelabra.hasAnyCandles()) {
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
                level.setBlock(pos, state.setValue(LIT, true), 11);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                if (!player.isCreative()) {
                    held.shrink(1);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }

        // 5. Extinguish with empty hand when lit
        if (state.getValue(LIT)) {
            if (held.isEmpty()) {
                extinguish(player, state, level, pos);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }

        // 6. Candelabra is OFF: place or retrieve candles
        if (isCandle(held)) {
            if (candelabra.fillAt(player, hit.getLocation(), held)) {
                level.playSound(null, pos, SoundEvents.CANDLE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                candelabra.sync();
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        // If not placed, retrieve candle clicked on
        ItemStack retrieved = candelabra.retrieve(hit.getLocation(), item -> true);
        if (!retrieved.isEmpty()) {
            if (held.isEmpty()) {
                player.setItemInHand(hand, retrieved);
            } else if (!player.getInventory().add(retrieved)) {
                player.drop(retrieved, false);
            }
            level.playSound(null, pos, SoundEvents.CANDLE_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
            candelabra.sync();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    public static void extinguish(@Nullable Player player, BlockState state, LevelAccessor level, BlockPos pos) {
        level.setBlock(pos, state.setValue(LIT, false), 11);
        if (level.getBlockEntity(pos) instanceof CandelabraBlockEntity candelabra) {
            Direction facing = state.getValue(FACING);
            for (int i = 0; i < CandelabraBlockEntity.SLOTS; i++) {
                if (!candelabra.getItem(i).isEmpty()) {
                    Vec3 wick = getWickOffset(i, facing);
                    level.addParticle(ParticleTypes.SMOKE, pos.getX() + wick.x, pos.getY() + wick.y, pos.getZ() + wick.z, 0.0, 0.1, 0.0);
                }
            }
        }
        level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        if (!state.getValue(LIT)) return;

        if (level.getBlockEntity(pos) instanceof CandelabraBlockEntity candelabra) {
            Direction facing = state.getValue(FACING);
            for (int i = 0; i < CandelabraBlockEntity.SLOTS; i++) {
                if (!candelabra.getItem(i).isEmpty()) {
                    Vec3 wick = getWickOffset(i, facing);
                    double px = pos.getX() + wick.x;
                    double py = pos.getY() + wick.y;
                    double pz = pos.getZ() + wick.z;

                    float f = random.nextFloat();
                    if (f < 0.3F) {
                        level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0, 0.0, 0.0);
                        if (f < 0.17F) {
                            level.playLocalSound(px, py, pz, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
                        }
                    }
                    level.addParticle(ParticleTypes.SMALL_FLAME, px, py, pz, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CandelabraBlockEntity candelabra) {
                Containers.dropContents(level, pos, candelabra);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            if (state.getValue(DUSTY)) {
                popResource(level, pos, new ItemStack(Items.COBWEB));
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
