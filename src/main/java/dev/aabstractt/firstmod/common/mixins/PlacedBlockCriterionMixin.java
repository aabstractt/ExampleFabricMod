package dev.aabstractt.firstmod.common.mixins;

import dev.aabstractt.firstmod.common.ChestBlockEntityUsable;
import dev.aabstractt.firstmod.server.ExampleModServer;
import net.minecraft.advancement.criterion.PlacedBlockCriterion;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlacedBlockCriterion.class)
public final class PlacedBlockCriterionMixin {

    @Inject(at = @At("RETURN"), method = "trigger")
    public void trigger(ServerPlayerEntity player, BlockPos blockPos, ItemStack stack, CallbackInfo ci) {
        if (!player.interactionManager.getGameMode().equals(GameMode.SURVIVAL)) {
            ExampleModServer.LOGGER.warn("Player is not in survival mode");
            return;
        }

        if (!(stack.getItem() instanceof BlockItem)) {
            ExampleModServer.LOGGER.warn("Item is not a block item");

            return;
        }

        BlockState blockState = player.getWorld().getBlockState(blockPos);
        if (blockState == null) {
            ExampleModServer.LOGGER.warn("Block state is null");
            return;
        }

        if (!blockState.isOf(Blocks.CHEST)) {
            ExampleModServer.LOGGER.warn("Block is not a chest");

            return;
        }

        BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
        if (!(blockEntity instanceof ChestBlockEntityUsable)) {
            ExampleModServer.LOGGER.warn("Block entity is not a chest");

            return;
        }

        ExampleModServer.LOGGER.info("Player {} has placed a chest at {}", player.getName().getString(), blockPos);

        ((ChestBlockEntityUsable) blockEntity).setUsed(true);
    }
}