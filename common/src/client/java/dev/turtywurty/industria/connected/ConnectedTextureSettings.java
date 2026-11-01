package dev.turtywurty.industria.connected;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.*;

public record ConnectedTextureSettings(
        Set<Identifier> connectsTo,
        Set<Direction> connectedFaces,
        ConnectedTextureAxis axis
) {
    public static final ConnectedTextureSettings DEFAULT =
            new ConnectedTextureSettings(Set.of(), EnumSet.allOf(Direction.class), ConnectedTextureAxis.BOTH);

    public ConnectedTextureSettings(
            Set<Identifier> connectsTo,
            Set<Direction> connectedFaces) {
        this(connectsTo, connectedFaces, ConnectedTextureAxis.BOTH);
    }

    public ConnectedTextureSettings(Set<Identifier> connectsTo) {
        this(connectsTo, DEFAULT.connectedFaces());
    }

    public ConnectedTextureSettings {
        connectsTo = Set.copyOf(connectsTo);
        connectedFaces = Set.copyOf(connectedFaces);
        Objects.requireNonNull(axis, "Connection axis cannot be null");
    }

    public static ConnectedTextureSettings fromJson(JsonObject json) {
        return new ConnectedTextureSettings(readConnections(json), readFaces(json), readAxis(json));
    }

    private static Set<Identifier> readConnections(JsonObject json) {
        JsonElement connectsToJson = json.get("connects_to");
        if (connectsToJson == null)
            return Set.of();

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

        return connectsTo;
    }

    private static Set<Direction> readFaces(JsonObject json) {
        JsonElement facesJson = json.get("connected_faces");
        if (facesJson == null)
            return DEFAULT.connectedFaces();

        if (!facesJson.isJsonArray())
            throw new JsonParseException("connected_faces must be an array of face names");

        Set<Direction> faces = EnumSet.noneOf(Direction.class);
        for (JsonElement element : facesJson.getAsJsonArray()) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString())
                throw new JsonParseException("Each connected_faces entry must be a face name string");

            String name = element.getAsString();
            Direction direction = Direction.byName(name);
            if (direction == null)
                throw new JsonParseException("Unknown face in connected_faces: " + name
                        + "; expected down, up, north, south, west or east");

            faces.add(direction);
        }

        return faces;
    }

    private static ConnectedTextureAxis readAxis(JsonObject json) {
        JsonElement axisJson = json.get("connection_axis");
        if (axisJson == null)
            return DEFAULT.axis();

        if (!axisJson.isJsonPrimitive() || !axisJson.getAsJsonPrimitive().isString())
            throw new JsonParseException("connection_axis must be a string");

        String axisName = axisJson.getAsString();
        try {
            return ConnectedTextureAxis.valueOf(axisName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException _) {
            throw new JsonParseException("Unknown connection_axis: " + axisName + "; expected both, horizontal or vertical");
        }
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
