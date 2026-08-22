package dev.turtywurty.industria.pipe;

import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.turtymultiloader.client.registration.AdditionalModel;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PipeConnectionModelLoading {
    private static volatile Map<ConnectionModelReference, AdditionalModel<BlockStateModel>> registeredModels = Map.of();

    private static boolean initialized;

    private PipeConnectionModelLoading() {
    }

    public static synchronized void init() {
        if (initialized)
            return;

        Map<ConnectionModelReference, AdditionalModel<BlockStateModel>> models = new LinkedHashMap<>();
        for (ConnectionModelSet modelSet : PipeConnectionModelRegistry.allModelSets()) {
            for (ConnectionModelReference reference : modelSet.references()) {
                models.putIfAbsent(reference, reference.modelKey());
            }
        }

        registeredModels = Map.copyOf(models);

        ClientRegistrations.registerBlockStateModelAugmenter(
                state -> state.getBlock() instanceof PipeBlock<?, ?>,
                new PipeConnectionBlockStateModel()
        );

        initialized = true;
    }

    public static @Nullable BlockStateModel getBakedModel(ConnectionModelReference reference) {
        AdditionalModel<BlockStateModel> model = registeredModels.get(reference);
        return model == null ? null : model.get();
    }
}
