package dev.turtywurty.industria.pipe;

import dev.turtywurty.industria.block.PipeBlock;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;

public final class PipeConnectionModelLoading {
    private PipeConnectionModelLoading() {
    }

    public static void init() {
        ModelLoadingPlugin.register(context -> {
            for (ConnectionModelSet modelSet : PipeConnectionModelRegistry.allModelSets()) {
                for (ConnectionModelReference reference : modelSet.references()) {
                    context.addModel(reference.modelKey(), reference.createUnbakedModel());
                }
            }

            context.modifyBlockModelAfterBake().register(
                    ModelModifier.WRAP_PHASE,
                    (model, modelContext) -> {
                        if(!(modelContext.state().getBlock() instanceof PipeBlock<?,?,?>))
                            return model;

                        return new PipeConnectionBlockStateModel(model);
                    }
            );
        });
    }
}
