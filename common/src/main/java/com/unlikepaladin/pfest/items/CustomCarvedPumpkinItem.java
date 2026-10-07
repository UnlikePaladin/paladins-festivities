package com.unlikepaladin.pfest.items;

import com.unlikepaladin.pfest.blocks.blockentities.CustomCarvedPumpkinBlockEntity;
import com.unlikepaladin.pfest.data.CarvedPumpkinData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CustomCarvedPumpkinItem extends BlockItem {
    public CustomCarvedPumpkinItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        boolean updated = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        CompoundTag beTag = stack.getTagElement("BlockEntityTag");
        if (beTag != null && level.getBlockEntity(pos) instanceof CustomCarvedPumpkinBlockEntity be) {
            be.getData().loadFromNbt(beTag);
            if (!level.isClientSide) {
                be.sync();
            }
        }
        return updated;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag beTag = stack.getTagElement("BlockEntityTag");
        if (beTag != null) {
            CarvedPumpkinData data = new CarvedPumpkinData();
            data.loadFromNbt(beTag);
            int res = data.getResolution();
            tooltip.add(new TextComponent("Resolution: " + res + "x" + res).withStyle(ChatFormatting.GRAY));
            int carvedFaces = 0;
            for (int i = 0; i < 4; i++) {
                if (data.hasFaceCarved(i)) carvedFaces++;
            }
            tooltip.add(new TextComponent("Carved Faces: " + carvedFaces + "/4").withStyle(ChatFormatting.GOLD));
        }
    }
}
