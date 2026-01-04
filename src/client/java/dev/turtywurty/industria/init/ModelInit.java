package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;

public class ModelInit {
    public static final Identifier SEISMIC_SCANNER_MODEL_ID = Industria.id("item/seismic_scanner_model");
    public static final ExtraModelKey<BlockStateModel> SEISMIC_SCANNER_MODEL_KEY = ExtraModelKey.create(SEISMIC_SCANNER_MODEL_ID::toString);

    public static void init() {
        ModelLoadingPlugin.register(context -> {
            context.addModel(SEISMIC_SCANNER_MODEL_KEY, SimpleUnbakedExtraModel.blockStateModel(SEISMIC_SCANNER_MODEL_ID));
        });
    }
}
