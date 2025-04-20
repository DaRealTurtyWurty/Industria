package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;

public class ModelInit {
    public static final Identifier SEISMIC_SCANNER_MODEL_ID = Industria.id("item/seismic_scanner_model");

    public static void init() {
        ModelLoadingPlugin.register(context -> {
            context.addModel(SEISMIC_SCANNER_MODEL_ID);
        });
    }
}
