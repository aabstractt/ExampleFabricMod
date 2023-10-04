package dev.aabstractt.firstmod.common.loot;

import com.google.gson.*;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.util.JsonHelper;

import java.lang.reflect.Type;

@RequiredArgsConstructor @Data
public final class LocalLootTable {

    private final @NonNull LootTable[] tables;
    private final @NonNull String[] identifiers;

    public static @NonNull LocalLootTable buildPlains() {
        return new LocalLootTable(new LootTable[]{LootTable.builder().pool(
                LootPool.builder()
                        .with(ItemEntry.builder(Items.EGG))
                        .build()
        ).build()}, new String[]{"loot:plains"});
    }

    public final static class Serializer
            implements JsonDeserializer<LocalLootTable>,
            JsonSerializer<LocalLootTable> {

        @Override
        public LocalLootTable deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = JsonHelper.asObject(jsonElement, "loot pool");

            return new LocalLootTable(
                    JsonHelper.deserialize(jsonObject, "tables", new LootTable[0], jsonDeserializationContext, LootTable[].class),
                    JsonHelper.deserialize(jsonObject, "identifiers", new String[0], jsonDeserializationContext, String[].class)
            );
        }

        @Override
        public JsonElement serialize(LocalLootTable localLootTable, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("tables", context.serialize(localLootTable.tables));
            jsonObject.add("identifiers", context.serialize(localLootTable.identifiers));

            return jsonObject;
        }
    }
}