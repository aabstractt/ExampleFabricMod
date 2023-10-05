package dev.aabstractt.firstmod.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import dev.aabstractt.firstmod.common.loot.LocalLootTable;
import lombok.NonNull;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.loot.LootGsons;
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

        Gson gson = LootGsons.getTableGsonBuilder()
                .registerTypeAdapter(LocalLootTable.class, new LocalLootTable.Serializer())
                .create();

        LOGGER.info("Dir is " + FabricLoader.getInstance().getConfigDir().toString());

        File file = new File(FabricLoader.getInstance().getConfigDir().resolve("config.json").toString());
        if (!file.exists()) {
            LOGGER.error("File doesn't exist");

            LOOT_TABLES.put("plains", LocalLootTable.buildPlains());

            try {
                FileUtils.write(file, gson.toJson(LOOT_TABLES));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            LOGGER.info("File created");

            return;
        }

        try {
            LOOT_TABLES = gson.fromJson(FileUtils.readFileToString(file, Charset.defaultCharset()), new TypeToken<Map<String, LocalLootTable>>(){}.getType());

            LOGGER.info("Loot tables loaded");

            if (LOOT_TABLES.containsKey("default")) {
                System.out.println("default exists");
            } else {
                System.out.println("non exists");
            }

            System.out.println(LOOT_TABLES);
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