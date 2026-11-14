package com.unlikepaladin.pfest.blocks;

import com.unlikepaladin.pfest.blocks.blockentities.PotionCauldronBlockEntity;
import com.unlikepaladin.pfest.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Map;

public class PotionCauldronInteractions {
    public static final Map<Item, CauldronInteraction> POTION_INTERACTIONS = CauldronInteraction.newInteractionMap();

    public static void init() {
        // --- 1. Interactions on Potion Cauldron (POTION_INTERACTIONS) ---

        // Dipping arrows
        POTION_INTERACTIONS.put(Items.ARROW, (state, level, pos, player, hand, stack) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof PotionCauldronBlockEntity potionBe)) {
                return InteractionResult.PASS;
            }

            int curLevel = state.getValue(PotionCauldronBlock.LEVEL);
            int count = stack.getCount();

            int arrowsToTip;
            int levelsToConsume;

            if (curLevel == 3 && count >= 33) {
                arrowsToTip = Math.min(count, 64);
                levelsToConsume = 3;
            } else if (curLevel >= 2 && count >= 17) {
                arrowsToTip = Math.min(count, 32);
                levelsToConsume = 2;
            } else {
                arrowsToTip = Math.min(count, 16);
                levelsToConsume = 1;
            }

            if (!level.isClientSide) {
                ItemStack tipped = new ItemStack(Items.TIPPED_ARROW, arrowsToTip);
                PotionUtils.setPotion(tipped, potionBe.getPotion());
                PotionUtils.setCustomEffects(tipped, potionBe.getCustomEffects());
                if (potionBe.hasCustomColor()) {
                    tipped.getOrCreateTag().putInt("CustomPotionColor", potionBe.getCustomColor());
                }

                if (!player.getAbilities().instabuild) {
                    stack.shrink(arrowsToTip);
                }

                if (stack.isEmpty()) {
                    player.setItemInHand(hand, tipped);
                } else if (!player.getInventory().add(tipped)) {
                    player.drop(tipped, false);
                } else {
                    player.inventoryMenu.sendAllDataToRemote();
                }

                int newLevel = curLevel - levelsToConsume;
                if (newLevel <= 0) {
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                } else {
                    level.setBlockAndUpdate(pos, state.setValue(PotionCauldronBlock.LEVEL, newLevel));
                }

                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(Items.ARROW));
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        // Glass bottle (retrieve potion)
        POTION_INTERACTIONS.put(Items.GLASS_BOTTLE, (state, level, pos, player, hand, stack) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof PotionCauldronBlockEntity potionBe)) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                ItemStack filledBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), potionBe.getPotion());
                PotionUtils.setCustomEffects(filledBottle, potionBe.getCustomEffects());
                if (potionBe.hasCustomColor()) {
                    filledBottle.getOrCreateTag().putInt("CustomPotionColor", potionBe.getCustomColor());
                }

                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filledBottle));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));

                int newLevel = state.getValue(PotionCauldronBlock.LEVEL) - 1;
                if (newLevel <= 0) {
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                } else {
                    level.setBlockAndUpdate(pos, state.setValue(PotionCauldronBlock.LEVEL, newLevel));
                }

                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        // Adding potion (normal, splash, lingering)
        CauldronInteraction addPotionInteraction = (state, level, pos, player, hand, stack) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof PotionCauldronBlockEntity potionBe)) {
                return InteractionResult.PASS;
            }

            if (potionBe.matchesPotion(stack)) {
                int curLevel = state.getValue(PotionCauldronBlock.LEVEL);
                if (curLevel < 3) {
                    if (!level.isClientSide) {
                        if (stack.is(Items.POTION)) {
                            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                        } else if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        player.awardStat(Stats.USE_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                        level.setBlockAndUpdate(pos, state.setValue(PotionCauldronBlock.LEVEL, curLevel + 1));
                        level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    return InteractionResult.PASS;
                }
            } else {
                // Different potion: explode and clear (Bedrock behavior)
                if (!level.isClientSide) {
                    if (stack.is(Items.POTION)) {
                        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    } else if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    triggerCauldronExplosion(level, pos);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        };

        POTION_INTERACTIONS.put(Items.POTION, addPotionInteraction);
        POTION_INTERACTIONS.put(Items.SPLASH_POTION, addPotionInteraction);
        POTION_INTERACTIONS.put(Items.LINGERING_POTION, addPotionInteraction);

        // Water bucket on potion cauldron -> explodes and empties
        POTION_INTERACTIONS.put(Items.WATER_BUCKET, (state, level, pos, player, hand, stack) -> {
            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(Items.WATER_BUCKET));
                triggerCauldronExplosion(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        // Lava bucket on potion cauldron -> becomes lava cauldron
        POTION_INTERACTIONS.put(Items.LAVA_BUCKET, (state, level, pos, player, hand, stack) -> {
            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(Items.LAVA_BUCKET));
                level.setBlockAndUpdate(pos, Blocks.LAVA_CAULDRON.defaultBlockState());
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });


        // --- 2. Wrap CauldronInteraction.EMPTY for vanilla Blocks.CAULDRON ---

        CauldronInteraction originalEmptyPotion = CauldronInteraction.EMPTY.get(Items.POTION);
        CauldronInteraction.EMPTY.put(Items.POTION, (state, level, pos, player, hand, stack) -> {
            Potion potion = PotionUtils.getPotion(stack);
            if (potion == Potions.WATER) {
                return originalEmptyPotion != null ? originalEmptyPotion.interact(state, level, pos, player, hand, stack) : InteractionResult.PASS;
            }
            if (potion == Potions.EMPTY) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                Item item = stack.getItem();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.setBlockAndUpdate(pos, ModBlocks.POTION_CAULDRON.get().defaultBlockState().setValue(PotionCauldronBlock.LEVEL, 1));
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof PotionCauldronBlockEntity potionBe) {
                    potionBe.setPotionFromStack(stack);
                }
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        CauldronInteraction emptySplashOrLingering = (state, level, pos, player, hand, stack) -> {
            Potion potion = PotionUtils.getPotion(stack);
            if (potion == Potions.EMPTY) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                Item item = stack.getItem();
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));

                if (potion == Potions.WATER) {
                    level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
                } else {
                    level.setBlockAndUpdate(pos, ModBlocks.POTION_CAULDRON.get().defaultBlockState().setValue(PotionCauldronBlock.LEVEL, 1));
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof PotionCauldronBlockEntity potionBe) {
                        potionBe.setPotionFromStack(stack);
                    }
                }
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };

        CauldronInteraction.EMPTY.put(Items.SPLASH_POTION, emptySplashOrLingering);
        CauldronInteraction.EMPTY.put(Items.LINGERING_POTION, emptySplashOrLingering);


        // --- 3. Wrap CauldronInteraction.WATER for non-water potion mix ---

        CauldronInteraction originalWaterPotion = CauldronInteraction.WATER.get(Items.POTION);
        CauldronInteraction.WATER.put(Items.POTION, (state, level, pos, player, hand, stack) -> {
            Potion potion = PotionUtils.getPotion(stack);
            if (potion == Potions.WATER) {
                return originalWaterPotion != null ? originalWaterPotion.interact(state, level, pos, player, hand, stack) : InteractionResult.PASS;
            }
            if (potion == Potions.EMPTY) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                triggerCauldronExplosion(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        CauldronInteraction waterSplashOrLingering = (state, level, pos, player, hand, stack) -> {
            Potion potion = PotionUtils.getPotion(stack);
            if (potion == Potions.WATER) {
                if (state.getValue(LayeredCauldronBlock.LEVEL) < 3) {
                    if (!level.isClientSide) {
                        if (!player.getAbilities().instabuild) stack.shrink(1);
                        player.awardStat(Stats.USE_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                        level.setBlockAndUpdate(pos, state.cycle(LayeredCauldronBlock.LEVEL));
                        level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                return InteractionResult.PASS;
            }
            if (potion == Potions.EMPTY) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                triggerCauldronExplosion(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };

        CauldronInteraction.WATER.put(Items.SPLASH_POTION, waterSplashOrLingering);
        CauldronInteraction.WATER.put(Items.LINGERING_POTION, waterSplashOrLingering);
    }

    private static void triggerCauldronExplosion(Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.2F);
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.05);
        }
        level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
    }
}
