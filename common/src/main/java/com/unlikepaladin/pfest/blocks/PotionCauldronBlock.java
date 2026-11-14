package com.unlikepaladin.pfest.blocks;

import com.unlikepaladin.pfest.blocks.blockentities.PotionCauldronBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class PotionCauldronBlock extends AbstractCauldronBlock implements EntityBlock {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;

    public PotionCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, PotionCauldronInteractions.POTION_INTERACTIONS);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 1));
    }

    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == 3;
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return (6.0 + state.getValue(LEVEL) * 3.0) / 16.0;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotionCauldronBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        if (random.nextFloat() < 0.25F) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PotionCauldronBlockEntity potionBe) {
                int color = potionBe.getColor();
                double r = ((color >> 16) & 0xFF) / 255.0;
                double g = ((color >> 8) & 0xFF) / 255.0;
                double b = (color & 0xFF) / 255.0;
                double px = pos.getX() + 0.25 + random.nextDouble() * 0.5;
                double py = pos.getY() + getContentHeight(state);
                double pz = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
                level.addParticle(ParticleTypes.ENTITY_EFFECT, px, py, pz, r, g, b);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(Items.CAULDRON);
    }
}
