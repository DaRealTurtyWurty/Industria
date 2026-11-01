package dev.turtywurty.industria.neoforge.connected;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.turtywurty.industria.connected.ConnectedTextureSettings;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public class NeoForgeConnectedTextureLoader implements UnbakedModelLoader<NeoForgeConnectedUnbakedModel> {
    @Override
    public NeoForgeConnectedUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
        ConnectedTextureSettings settings = ConnectedTextureSettings.fromJson(jsonObject);
        JsonObject deepCopy = jsonObject.deepCopy();
        deepCopy.remove("loader");
        deepCopy.remove("connects_to");
        deepCopy.remove("connected_faces");
        deepCopy.remove("connection_axis");
        deepCopy.remove("face_rules");
        UnbakedModel unbakedModel = context.deserialize(deepCopy, UnbakedModel.class);
        return new NeoForgeConnectedUnbakedModel(unbakedModel, settings);
    }
}
