package dev.aabstractt.firstmod.common.mixins;

import dev.aabstractt.firstmod.common.ChestBlockEntityUsable;
import dev.aabstractt.firstmod.server.ExampleModServer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
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
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
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

    @Final protected @Shadow ServerPlayerEntity player;
    protected @Shadow ServerWorld world;

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
            ExampleModServer.LOGGER.error("Block entity is not an instance of UsableClass");

            return;
        }

        if (((ChestBlockEntityUsable) blockEntity).isUsed()) {
            ExampleModServer.LOGGER.warn("Block entity has already been used");

            player.sendMessage(MutableText.of(new LiteralTextContent("Este cofre ya ha sido usado")), false);

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

        ((ChestBlockEntityUsable) blockEntity).setUsed(true);
        blockEntity.markDirty();

        player.sendMessage(MutableText.of(new LiteralTextContent("Le otorgaste el loot a este cobre del bioma %s, el loot tiene id de %s".formatted(optional.get().getValue().getPath(), identifier))), false);
    }
}