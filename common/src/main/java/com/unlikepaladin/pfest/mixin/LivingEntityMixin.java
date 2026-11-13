package com.unlikepaladin.pfest.mixin;

import com.unlikepaladin.pfest.blocks.CoffinBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "setPosToBed", at = @At("HEAD"), cancellable = true)
    private void pfest$adjustSleepingPosForCoffin(BlockPos blockPos, CallbackInfo ci) {
        if (this.level != null && blockPos != null) {
            BlockState state = this.level.getBlockState(blockPos);
            if (state.getBlock() instanceof CoffinBlock) {
                // The coffin interior cavity floor is at 1/16 (0.0625D).
                // Setting entity Y to 3/16 (0.1875D) places the entity's back (which rests 2/16 below entity Y)
                // directly on the interior floor inside the coffin walls.
                this.setPos(blockPos.getX() + 0.5D, blockPos.getY() + 0.1875D, blockPos.getZ() + 0.5D);
                ci.cancel();
            }
        }
    }
}
