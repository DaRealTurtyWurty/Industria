package dev.turtywurty.industria.pipe;

import dev.turtywurty.turtymultiloader.client.registration.AdditionalBlockStateModelDefinition;
import dev.turtywurty.turtymultiloader.client.registration.AdditionalModel;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.resources.Identifier;

public record ConnectionModelReference(
        AdditionalModel<BlockStateModel> modelKey,
        Identifier modelId,
        ModelState modelState
) {
    public ConnectionModelReference(Identifier modelKey, Identifier modelId, ModelState modelState) {
        this(ClientRegistrations.registerAdditionalBlockStateModel(
                modelKey,
                AdditionalBlockStateModelDefinition.blockStateModel(modelId, modelState)
        ), modelId, modelState);
    }

    public AdditionalBlockStateModelDefinition createUnbakedModel() {
        return AdditionalBlockStateModelDefinition.blockStateModel(modelId, modelState);
    }
}
