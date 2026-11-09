package dev.turtywurty.industria.connected.model;

import com.google.gson.*;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public record VerticalConnectedModelSettings(
        Identifier normal,
        Identifier lower,
        Identifier middle,
        Identifier upper,
        Set<Identifier> connectsTo
) {
    public VerticalConnectedModelSettings {
        Objects.requireNonNull(normal, "normal cannot be null");
        Objects.requireNonNull(lower, "lower cannot be null");
        Objects.requireNonNull(middle, "middle cannot be null");
        Objects.requireNonNull(upper, "upper cannot be null");

        Objects.requireNonNull(connectsTo, "connectsTo cannot be null");
        connectsTo = Set.copyOf(connectsTo);
    }

    public Identifier modelFor(VerticalConnectedModelVariant variant) {
        return switch (variant) {
            case NORMAL -> normal;
            case LOWER -> lower;
            case MIDDLE -> middle;
            case UPPER -> upper;
        };
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

    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        for (VerticalConnectedModelVariant variant : VerticalConnectedModelVariant.values()) {
            resolver.markDependency(modelFor(variant));
        }
    }

    public JsonObject toJson() {
        var json = new JsonObject();

        var models = new JsonObject();
        for (VerticalConnectedModelVariant variant : VerticalConnectedModelVariant.values()) {
            models.addProperty(
                    variant.name().toLowerCase(Locale.ROOT),
                    modelFor(variant).toString()
            );
        }
        json.add("models", models);

        if (!connectsTo.isEmpty()) {
            if (connectsTo.size() == 1) {
                json.addProperty("connects_to", connectsTo.iterator().next().toString());
            } else {
                var array = new JsonArray();
                for (String id : connectsTo.stream()
                        .map(Identifier::toString)
                        .sorted()
                        .toList()) {
                    array.add(new JsonPrimitive(id));
                }

                json.add("connects_to", array);
            }
        }

        return json;
    }

    public static VerticalConnectedModelSettings fromJson(JsonObject json) {
        JsonElement modelsElement = json.get("models");
        if (modelsElement == null || !modelsElement.isJsonObject())
            throw new JsonParseException("models must be an object, but was: " + modelsElement);

        JsonObject models = modelsElement.getAsJsonObject();
        Identifier normal = readModel(models, "normal");
        Identifier lower = readModel(models, "lower");
        Identifier middle = readModel(models, "middle");
        Identifier upper = readModel(models, "upper");

        Set<Identifier> connectsTo = new HashSet<>();
        JsonElement connectsToElement = json.get("connects_to");
        if (connectsToElement != null) {
            if (connectsToElement.isJsonPrimitive() && connectsToElement.getAsJsonPrimitive().isString()) {
                Identifier id = Identifier.tryParse(connectsToElement.getAsString());
                if (id == null)
                    throw new JsonParseException("connects_to must be a valid identifier, but was: " + connectsToElement);

                connectsTo.add(id);
            } else if (connectsToElement.isJsonArray()) {
                for (JsonElement element : connectsToElement.getAsJsonArray()) {
                    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString())
                        throw new JsonParseException("connects_to must be an array of strings, but was: " + connectsToElement);

                    Identifier id = Identifier.tryParse(element.getAsString());
                    if (id == null)
                        throw new JsonParseException("connects_to must be a valid identifier, but was: " + element);

                    connectsTo.add(id);
                }
            } else {
                throw new JsonParseException("connects_to must be a string or an array of strings, but was: " + connectsToElement);
            }
        }

        return new VerticalConnectedModelSettings(normal, lower, middle, upper, connectsTo);
    }

    private static Identifier readModel(JsonObject models, String key) {
        JsonElement element = models.get(key);
        if (element == null || !element.isJsonPrimitive())
            throw new JsonParseException("models." + key + " must be a string, but was: " + element);

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isString())
            throw new JsonParseException("models." + key + " must be a string, but was: " + primitive);

        Identifier id = Identifier.tryParse(primitive.getAsString());
        if (id == null)
            throw new JsonParseException("models." + key + " must be a valid identifier, but was: " + primitive);

        return id;
    }
}
