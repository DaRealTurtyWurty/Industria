package dev.turtywurty.industria.pipe;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.resources.Identifier;

public record ConnectionModelReference(ExtraModelKey<BlockStateModel> modelKey, Identifier modelId,
                                       ModelState modelState) {
    public ConnectionModelReference(Identifier modelKey, Identifier modelId, ModelState modelState) {
        this(ExtraModelKey.create(modelKey::toString), modelId, modelState);
    }

    public SimpleUnbakedExtraModel<BlockStateModel> createUnbakedModel() {
        return SimpleUnbakedExtraModel.blockStateModel(modelId, modelState);
    }
}
