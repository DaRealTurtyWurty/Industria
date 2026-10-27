package dev.turtywurty.industria.connected;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public record ConnectedTextureSettings(Set<Identifier> connectsTo) {
    public static final ConnectedTextureSettings DEFAULT = new ConnectedTextureSettings(Set.of());

    public ConnectedTextureSettings {
        connectsTo = Set.copyOf(connectsTo);
    }

    public static ConnectedTextureSettings fromJson(JsonObject json) {
        JsonElement connectsToJson = json.get("connects_to");
        if (connectsToJson == null)
            return DEFAULT;

        if (!connectsToJson.isJsonArray()
                && !(connectsToJson.isJsonPrimitive() && connectsToJson.getAsJsonPrimitive().isString()))
            throw new JsonParseException("connects_to must be a block id or array of block ids");

        Set<Identifier> connectsTo = new HashSet<>();
        if (connectsToJson.isJsonArray()) {
            for (JsonElement element : connectsToJson.getAsJsonArray()) {
                if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString())
                    throw new JsonParseException("connects_to must be a block id or array of block ids");

                Identifier blockId = Identifier.tryParse(element.getAsString());
                if (blockId == null)
                    throw new JsonParseException("Invalid block id: " + element.getAsString());

                connectsTo.add(blockId);
            }
        } else {
            Identifier blockId = Identifier.tryParse(connectsToJson.getAsString());
            if (blockId == null)
                throw new JsonParseException("Invalid block id: " + connectsToJson.getAsString());

            connectsTo.add(blockId);
        }

        return new ConnectedTextureSettings(connectsTo);
    }

    public Set<Block> resolveBlocks() {
        Set<Block> blocks = new HashSet<>();
        for (Identifier id : connectsTo) {
            if (!BuiltInRegistries.BLOCK.containsKey(id))
                throw new JsonParseException("Unknown block in connects_to: " + id);

            blocks.add(BuiltInRegistries.BLOCK.getValue(id));
        }

        return Set.copyOf(blocks);
    }
}
