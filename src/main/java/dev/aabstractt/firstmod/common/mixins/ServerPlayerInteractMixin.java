package dev.aabstractt.firstmod.common.mixins;

import dev.aabstractt.firstmod.common.ChestBlockEntityUsable;
import dev.aabstractt.firstmod.server.ExampleModServer;
import net.minecraft.advancement.criterion.PlacedBlockCriterion;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(ServerPlayerInteractionManager.class)
public final class ServerPlayerInteractMixin {

    @Final
    protected @Shadow ServerPlayerEntity player;

    @Shadow protected ServerWorld world;

    @Shadow @Final private static Logger LOGGER;

    @Inject(at = @At("HEAD"), method = "interactBlock")
    public void onInteractBlock(
            ServerPlayerEntity player,
            World world,
            ItemStack stack,
            Hand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        if (player.getStackInHand(hand).getItem() == Items.STICK) {
            player.sendMessage(MutableText.of(new LiteralTextContent("You have interacted with a block!")), false);
        }

        BlockState blockState = world.getBlockState(hitResult.getBlockPos());
        if (!blockState.isOf(Blocks.CHEST)) {
            System.out.println("No soy, yo soy de " + blockState.getBlock().getName());

            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
        if (!(blockEntity instanceof ChestBlockEntity)) {
            return;
        }

        if (!(blockEntity instanceof ChestBlockEntityUsable)) {
            LOGGER.error("Block entity is not an instance of UsableClass");

            return;
        }

        if (((ChestBlockEntityUsable) blockEntity).isUsed()) {
            LOGGER.warn("Block entity has already been used");

            return;
        }

        RegistryEntry<Biome> registryEntry = world.getBiome(hitResult.getBlockPos());
        if (registryEntry == null) {
            return;
        }

        Optional<RegistryKey<Biome>> optional = registryEntry.getKey();
        if (optional.isEmpty()) {
            return;
        }

        String identifier = ExampleModServer.getRandomLootTable(optional.get().getValue().getPath());
        if (identifier == null) {
            ExampleModServer.LOGGER.error("No loot table found for biome " + optional.get().getValue().getPath());

            return;
        }

        ((ChestBlockEntity) blockEntity).setLootTable(
                Identifier.splitOn(identifier, Identifier.NAMESPACE_SEPARATOR),
                ThreadLocalRandom.current().nextLong()
        );
        ((ChestBlockEntity) blockEntity).checkLootInteraction(player);

        if (blockEntity instanceof ChestBlockEntityUsable) {
            ((ChestBlockEntityUsable) blockEntity).setUsed(true);

            System.out.println("Usable!!!");
        }

        player.sendMessage(MutableText.of(new LiteralTextContent("You have interacted with a chest!")), false);
    }
}