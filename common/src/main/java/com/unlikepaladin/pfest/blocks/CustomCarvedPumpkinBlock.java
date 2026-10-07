package com.unlikepaladin.pfest.blocks;

import com.unlikepaladin.pfest.blocks.blockentities.CustomCarvedPumpkinBlockEntity;
import com.unlikepaladin.pfest.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CustomCarvedPumpkinBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public CustomCarvedPumpkinBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CompoundTag beTag = stack.getTagElement("BlockEntityTag");
        if (beTag != null && level.getBlockEntity(pos) instanceof CustomCarvedPumpkinBlockEntity be) {
            be.getData().loadFromNbt(beTag);
            be.sync();
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        boolean isLit = state.getValue(LIT);

        if (!isLit && (held.is(Items.TORCH) || held.is(Items.FLINT_AND_STEEL))) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LIT, true), 3);
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.isCreative() && held.is(Items.FLINT_AND_STEEL)) {
                    held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (isLit && (held.isEmpty() || held.is(Items.SHEARS))) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LIT, false), 3);
                level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            if (level.getBlockEntity(pos) instanceof CustomCarvedPumpkinBlockEntity be) {
                ItemStack drop = new ItemStack(ModBlocks.CUSTOM_CARVED_PUMPKIN.get());
                CompoundTag beTag = new CompoundTag();
                be.getData().saveToNbt(beTag);
                drop.addTagElement("BlockEntityTag", beTag);
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack itemStack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof CustomCarvedPumpkinBlockEntity be) {
            CompoundTag beTag = new CompoundTag();
            be.getData().saveToNbt(beTag);
            itemStack.addTagElement("BlockEntityTag", beTag);
        }
        return itemStack;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomCarvedPumpkinBlockEntity(pos, state);
    }
}
