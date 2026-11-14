package com.unlikepaladin.pfest.blocks.blockentities;

import com.unlikepaladin.pfest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class PotionCauldronBlockEntity extends BlockEntity {
    private Potion potion = Potions.WATER;
    private final List<MobEffectInstance> customEffects = new ArrayList<>();
    private int customColor = -1;

    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POTION_CAULDRON.get(), pos, state);
    }

    public Potion getPotion() {
        return this.potion;
    }

    public void setPotion(Potion potion) {
        this.potion = potion;
        this.sync();
    }

    public List<MobEffectInstance> getCustomEffects() {
        return this.customEffects;
    }

    public boolean hasCustomColor() {
        return this.customColor != -1;
    }

    public int getCustomColor() {
        return this.customColor;
    }

    public int getColor() {
        if (this.customColor != -1) {
            return this.customColor;
        }
        return PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.customEffects));
    }

    public boolean matchesPotion(ItemStack stack) {
        Potion otherPotion = PotionUtils.getPotion(stack);
        if (this.potion != otherPotion) {
            return false;
        }
        int otherColor = -1;
        if (stack.hasTag() && stack.getTag().contains("CustomPotionColor", Tag.TAG_INT)) {
            otherColor = stack.getTag().getInt("CustomPotionColor");
        }
        if (this.customColor != otherColor) {
            return false;
        }
        List<MobEffectInstance> otherEffects = PotionUtils.getCustomEffects(stack);
        return this.customEffects.equals(otherEffects);
    }

    public void setPotionFromStack(ItemStack stack) {
        this.potion = PotionUtils.getPotion(stack);
        this.customEffects.clear();
        this.customEffects.addAll(PotionUtils.getCustomEffects(stack));
        if (stack.hasTag() && stack.getTag().contains("CustomPotionColor", Tag.TAG_INT)) {
            this.customColor = stack.getTag().getInt("CustomPotionColor");
        } else {
            this.customColor = -1;
        }
        this.sync();
    }

    public void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        tag.putString("Potion", Registry.POTION.getKey(this.potion).toString());
        if (this.customColor != -1) {
            tag.putInt("CustomPotionColor", this.customColor);
        }
        if (!this.customEffects.isEmpty()) {
            ListTag listTag = new ListTag();
            for (MobEffectInstance effect : this.customEffects) {
                listTag.add(effect.save(new CompoundTag()));
            }
            tag.put("CustomPotionEffects", listTag);
        }
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Potion", Tag.TAG_STRING)) {
            Potion loadedPotion = Registry.POTION.get(ResourceLocation.tryParse(tag.getString("Potion")));
            if (loadedPotion != null) {
                this.potion = loadedPotion;
            }
        }
        if (tag.contains("CustomPotionColor", Tag.TAG_INT)) {
            this.customColor = tag.getInt("CustomPotionColor");
        } else {
            this.customColor = -1;
        }
        this.customEffects.clear();
        if (tag.contains("CustomPotionEffects", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("CustomPotionEffects", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                MobEffectInstance effect = MobEffectInstance.load(listTag.getCompound(i));
                if (effect != null) {
                    this.customEffects.add(effect);
                }
            }
        }
        if (this.level != null && this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.save(tag);
        return tag;
    }
}
