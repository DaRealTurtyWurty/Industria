package dev.turtywurty.industria.neoforge.connected.model;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.connected.model.BakedVerticalConnectedModels;
import dev.turtywurty.industria.connected.model.VerticalConnectedModelSettings;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public record NeoForgeVerticalConnectedBlockStateModel(Identifier model) implements CustomUnbakedBlockStateModel {
    public static final MapCodec<NeoForgeVerticalConnectedBlockStateModel> CODEC =
            Identifier.CODEC.fieldOf("model").xmap(
                    NeoForgeVerticalConnectedBlockStateModel::new,
                    NeoForgeVerticalConnectedBlockStateModel::model
            );

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        resolver.markDependency(model);
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        VerticalConnectedModelSettings settings =
                NeoForgeVerticalConnectedUnbakedModel.findSettings(baker.getModel(model));
        if (settings == null)
            throw new IllegalStateException("No vertical connected model settings found for model: " + model);

        BakedVerticalConnectedModels variants =
                BakedVerticalConnectedModels.bake(baker, settings);

        return new NeoForgeVerticalConnectedModel(variants, settings);
    }
}
