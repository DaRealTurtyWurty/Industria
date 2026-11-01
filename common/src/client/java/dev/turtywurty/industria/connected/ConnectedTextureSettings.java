package dev.turtywurty.industria.connected;

import com.google.gson.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.*;

public record ConnectedTextureSettings(
        ConnectedTextureRule defaultRule,
        Set<Direction> connectedFaces,
        Map<Direction, ConnectedTextureRule> faceRules
) {
    public static final ConnectedTextureSettings DEFAULT = new ConnectedTextureSettings(
            ConnectedTextureRule.DEFAULT, EnumSet.allOf(Direction.class), Map.of());

    public ConnectedTextureSettings(Set<Identifier> connectsTo, Set<Direction> connectedFaces, ConnectedTextureAxis axis) {
        this(new ConnectedTextureRule(connectsTo, axis), connectedFaces, Map.of());
    }

    public ConnectedTextureSettings(Set<Identifier> connectsTo, Set<Direction> connectedFaces) {
        this(connectsTo, connectedFaces, ConnectedTextureAxis.BOTH);
    }

    public ConnectedTextureSettings(Set<Identifier> connectsTo) {
        this(connectsTo, DEFAULT.connectedFaces());
    }

    public ConnectedTextureSettings {
        Objects.requireNonNull(defaultRule, "Default connection rule cannot be null");
        connectedFaces = Set.copyOf(connectedFaces);
        faceRules = Map.copyOf(faceRules);
    }

    public ConnectedTextureRule ruleFor(Direction face) {
        return faceRules.getOrDefault(face, defaultRule);
    }

    public Set<Identifier> connectsTo() {
        return defaultRule.connectsTo();
    }

    public ConnectedTextureAxis axis() {
        return defaultRule.axis();
    }

    public Set<Block> resolveBlocks() {
        return defaultRule.resolveBlocks();
    }

    public static ConnectedTextureSettings fromJson(JsonObject json) {
        ConnectedTextureRule defaults = ConnectedTextureRule.fromJson(json, ConnectedTextureRule.DEFAULT);
        return new ConnectedTextureSettings(defaults, readFaces(json), readFaceRules(json, defaults));
    }

    private static Set<Direction> readFaces(JsonObject json) {
        JsonElement value = json.get("connected_faces");
        if (value == null)
            return DEFAULT.connectedFaces();

        if (!value.isJsonArray())
            throw new JsonParseException("connected_faces must be an array of face names");

        Set<Direction> faces = EnumSet.noneOf(Direction.class);
        for (JsonElement entry : value.getAsJsonArray()) {
            if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString())
                throw new JsonParseException("Each connected_faces entry must be a face name string");

            faces.add(readFace(entry.getAsString(), "connected_faces"));
        }

        return faces;
    }

    private static Direction readFace(String name, String property) {
        Direction face = Direction.byName(name);
        if (face == null)
            throw new JsonParseException("Unknown face in " + property + ": " + name
                    + "; expected down, up, north, south, west or east");

        return face;
    }

    private static Map<Direction, ConnectedTextureRule> readFaceRules(JsonObject json, ConnectedTextureRule defaults) {
        JsonElement value = json.get("face_rules");
        if (value == null)
            return Map.of();

        if (!value.isJsonObject())
            throw new JsonParseException("face_rules must be an object keyed by face name");

        Map<Direction, ConnectedTextureRule> rules = new EnumMap<>(Direction.class);
        for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
            Direction face = readFace(entry.getKey(), "face_rules");
            if (!entry.getValue().isJsonObject())
                throw new JsonParseException("face_rules." + entry.getKey() + " must be an object");

            JsonObject ruleJson = entry.getValue().getAsJsonObject();
            for (Map.Entry<String, JsonElement> property : ruleJson.entrySet()) {
                String key = property.getKey();
                if (!key.equals("connects_to") && !key.equals("connection_axis") && !key.equals("layout_type"))
                    throw new JsonParseException("Unknown property in face_rules." + entry.getKey() + ": " + key);
            }

            try {
                rules.put(face, ConnectedTextureRule.fromJson(ruleJson, defaults));
            } catch (JsonParseException error) {
                throw new JsonParseException("Invalid face_rules." + entry.getKey() + ": " + error.getMessage(), error);
            }
        }

        return rules;
    }

    public JsonObject toJson() {
        JsonObject json = defaultRule.toJson(ConnectedTextureRule.DEFAULT);
        if (connectedFaces.size() != Direction.values().length) {
            JsonArray names = new JsonArray();
            for (Direction face : Direction.values()) {
                if (connectedFaces.contains(face)) {
                    names.add(new JsonPrimitive(face.getName()));
                }
            }

            json.add("connected_faces", names);
        }

        if (!faceRules.isEmpty()) {
            JsonObject overrides = new JsonObject();
            for (Direction face : Direction.values()) {
                ConnectedTextureRule rule = faceRules.get(face);
                if (rule != null) {
                    overrides.add(face.getName(), rule.toJson(defaultRule));
                }
            }

            json.add("face_rules", overrides);
        }

        return json;
    }
}
