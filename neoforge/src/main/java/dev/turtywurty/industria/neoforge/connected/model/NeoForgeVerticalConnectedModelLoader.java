package dev.turtywurty.industria.neoforge.connected.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import dev.turtywurty.industria.connected.model.VerticalConnectedModelSettings;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public class NeoForgeVerticalConnectedModelLoader implements UnbakedModelLoader<NeoForgeVerticalConnectedUnbakedModel> {
    @Override
    public NeoForgeVerticalConnectedUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
        VerticalConnectedModelSettings settings = VerticalConnectedModelSettings.fromJson(jsonObject);
        JsonObject deepCopy = jsonObject.deepCopy();
        deepCopy.remove("loader");
        deepCopy.remove("models");
        deepCopy.remove("connects_to");
        deepCopy.add("parent", new JsonPrimitive(settings.normal().toString()));
        UnbakedModel unbakedModel = context.deserialize(deepCopy, UnbakedModel.class);
        return new NeoForgeVerticalConnectedUnbakedModel(unbakedModel, settings);
    }
}
