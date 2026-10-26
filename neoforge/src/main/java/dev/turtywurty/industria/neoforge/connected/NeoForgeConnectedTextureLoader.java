package dev.turtywurty.industria.neoforge.connected;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public class NeoForgeConnectedTextureLoader implements UnbakedModelLoader<NeoForgeConnectedUnbakedModel> {
    @Override
    public NeoForgeConnectedUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
        JsonObject deepCopy = jsonObject.deepCopy();
        deepCopy.remove("loader");
        UnbakedModel unbakedModel = context.deserialize(deepCopy, UnbakedModel.class);
        return new NeoForgeConnectedUnbakedModel(unbakedModel);
    }
}
