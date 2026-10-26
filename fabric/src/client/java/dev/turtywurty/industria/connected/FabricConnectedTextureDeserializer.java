package dev.turtywurty.industria.connected;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.resources.model.UnbakedModel;

public class FabricConnectedTextureDeserializer implements UnbakedModelDeserializer {
    @Override
    public UnbakedModel deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
        JsonObject deepCopy = jsonObject.deepCopy();
        deepCopy.remove("fabric:type");
        UnbakedModel unbakedModel = context.deserialize(deepCopy, UnbakedModel.class);
        return new FabricConnectedUnbakedModel(unbakedModel);
    }
}
