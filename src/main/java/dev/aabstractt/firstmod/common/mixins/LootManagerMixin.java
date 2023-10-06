package dev.aabstractt.firstmod.common.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import dev.aabstractt.firstmod.client.ExampleModClient;
import dev.aabstractt.firstmod.common.loot.LocalLootTable;
import dev.aabstractt.firstmod.server.ExampleModServer;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(LootManager.class)
public final class LootManagerMixin {

    @Shadow private Map<Identifier, LootTable> tables;

    @Inject(method = "apply*", at = @At("RETURN"))
    private void apply(Map<Identifier, JsonObject> jsonMap, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
        Map<Identifier, LootTable> tables = new HashMap<>(this.tables);

        for (LocalLootTable localLootTable : ExampleModServer.LOOT_TABLES.values()) {
            for (int i = 0; i < localLootTable.getIdentifiers().length; i++) {
                tables.put(
                        Identifier.splitOn(localLootTable.getIdentifiers()[i], ':'),
                        localLootTable.getTables()[i]
                );
            }
        }

        this.tables = ImmutableMap.copyOf(tables);

        ExampleModServer.LOGGER.info("Loot tables applied: " + this.tables.size());
    }
}