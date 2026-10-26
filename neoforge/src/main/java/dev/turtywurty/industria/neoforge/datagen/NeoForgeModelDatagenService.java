package dev.turtywurty.industria.neoforge.datagen;

import com.google.gson.JsonObject;
import dev.turtywurty.industria.datagen.ModelDatagenService;
import net.minecraft.resources.Identifier;

public final class NeoForgeModelDatagenService implements ModelDatagenService {
    @Override
    public void addModelLoader(JsonObject json, Identifier loaderId) {
        json.addProperty("loader", loaderId.toString());
    }
}
