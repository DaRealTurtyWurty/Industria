package dev.turtywurty.industria.connected;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public record ConnectedTextureRule(Set<Identifier> connectsTo, ConnectedTextureAxis axis) {
    public static final ConnectedTextureRule DEFAULT =
            new ConnectedTextureRule(Set.of(), ConnectedTextureAxis.BOTH);

    public ConnectedTextureRule {
        connectsTo = Set.copyOf(connectsTo);
        Objects.requireNonNull(axis, "Connection axis cannot be null");
    }

    public static ConnectedTextureRule fromJson(JsonObject json, ConnectedTextureRule defaults) {
        return new ConnectedTextureRule(readConnections(json, defaults.connectsTo()), readAxis(json, defaults.axis()));
    }

    private static Set<Identifier> readConnections(JsonObject json, Set<Identifier> defaults) {
        JsonElement value = json.get("connects_to");
        if (value == null)
            return defaults;

        Set<Identifier> connections = new HashSet<>();
        if (value.isJsonArray()) {
            for (JsonElement entry : value.getAsJsonArray()) {
                connections.add(readConnection(entry));
            }
        } else {
            connections.add(readConnection(value));
        }

        return connections;
    }

    private static Identifier readConnection(JsonElement value) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
            throw new JsonParseException("connects_to must be a block id or array of block ids");

        Identifier id = Identifier.tryParse(value.getAsString());
        if (id == null)
            throw new JsonParseException("Invalid block id in connects_to: " + value.getAsString());

        return id;
    }

    private static ConnectedTextureAxis readAxis(JsonObject json, ConnectedTextureAxis defaults) {
        JsonElement value = json.get("connection_axis");
        if (value == null)
            return defaults;

        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
            throw new JsonParseException("connection_axis must be a string");

        String name = value.getAsString();
        try {
            return ConnectedTextureAxis.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException _) {
            throw new JsonParseException("Unknown connection_axis: " + name + "; expected both, horizontal or vertical");
        }
    }

    public JsonObject toJson(ConnectedTextureRule defaults) {
        JsonObject json = new JsonObject();
        if (!connectsTo.equals(defaults.connectsTo())) {
            JsonArray targets = new JsonArray();
            for (String id : connectsTo.stream().map(Identifier::toString).sorted().toList()) {
                targets.add(new JsonPrimitive(id));
            }

            json.add("connects_to", targets);
        }

        if (axis != defaults.axis()) {
            json.add("connection_axis", new JsonPrimitive(axis.name().toLowerCase(Locale.ROOT)));
        }

        return json;
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
