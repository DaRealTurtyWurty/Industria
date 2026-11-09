package dev.turtywurty.industria.connected.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.resources.model.UnbakedModel;

public class FabricVerticalConnectedModelDeserializer implements UnbakedModelDeserializer {
    @Override
    public UnbakedModel deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
        VerticalConnectedModelSettings settings = VerticalConnectedModelSettings.fromJson(jsonObject);
        JsonObject deepCopy = jsonObject.deepCopy();
        deepCopy.remove("fabric:type");
        deepCopy.remove("models");
        deepCopy.remove("connects_to");
        deepCopy.add("parent", new JsonPrimitive(settings.normal().toString()));
        UnbakedModel unbakedModel = context.deserialize(deepCopy, UnbakedModel.class);
        return new FabricVerticalConnectedUnbakedModel(unbakedModel, settings);
    }
}
