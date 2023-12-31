package dev.aabstractt.firstmod.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import dev.aabstractt.firstmod.common.loot.LocalLootTable;
import lombok.NonNull;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.loot.LootGsons;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class ExampleModServer implements ModInitializer {

    public final static @NonNull Logger LOGGER = LogUtils.getLogger();

    public static @NonNull Map<String, LocalLootTable> LOOT_TABLES = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        System.out.println("Hello Fabric world! (Client)");

        loadLootTables();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated, environment) -> dispatcher.register(CommandManager.literal("chestloot")
                        .then(CommandManager.literal("reload").executes(context -> {
                            context.getSource().sendFeedback(MutableText.of(new LiteralTextContent("Reloading loot tables...")), true);

                            loadLootTables();

                            return 1;
                        }))
                )
        );
    }

    private static void loadLootTables() {
        File file = new File(FabricLoader.getInstance().getConfigDir().resolve("config.json").toString());
        if (!file.exists()) {
            LOGGER.error("File doesn't exist");

            return;
        }

        Gson gson = LootGsons.getTableGsonBuilder()
                .registerTypeAdapter(LocalLootTable.class, new LocalLootTable.Serializer())
                .create();

        try {
            LOOT_TABLES = gson.fromJson(FileUtils.readFileToString(file, Charset.defaultCharset()), new TypeToken<Map<String, LocalLootTable>>(){}.getType());

            LOGGER.info("Loot tables loaded");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable String getRandomLootTable(@NonNull String biome) {
        LocalLootTable localLootTable = LOOT_TABLES.get(biome);
        if (localLootTable == null) {
            return biome.equalsIgnoreCase("default") ? null : getRandomLootTable("default");
        }

        String[] identifiers = localLootTable.getIdentifiers();
        if (identifiers.length == 0) {
            return null;
        }

        return identifiers[Math.abs(ThreadLocalRandom.current().nextInt()) % identifiers.length];
    }
}