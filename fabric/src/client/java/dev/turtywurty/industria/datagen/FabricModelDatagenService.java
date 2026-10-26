package dev.turtywurty.industria.datagen;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

public final class FabricModelDatagenService implements ModelDatagenService {
    @Override
    public void addModelLoader(JsonObject json, Identifier loaderId) {
        json.addProperty("fabric:type", loaderId.toString());
    }
}
