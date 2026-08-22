package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.client.registration.AdditionalModel;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;

public class ModModels {
    public static final Identifier SEISMIC_SCANNER_MODEL_ID = Industria.id("item/seismic_scanner_model");
    public static final AdditionalModel<BlockStateModel> SEISMIC_SCANNER_MODEL_KEY = ClientRegistrations.registerAdditionalBlockStateModel(SEISMIC_SCANNER_MODEL_ID);

    public static void init() {
        ModPipeConnectionModels.init();
    }
}
