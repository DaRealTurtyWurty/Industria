package dev.turtywurty.industria;

import dev.turtywurty.industria.connected.ConnectedTextureSettings;
import dev.turtywurty.industria.connected.FabricConnectedTextureDeserializer;
import dev.turtywurty.industria.connected.FabricConnectedTextureModel;
import dev.turtywurty.industria.connected.FabricConnectedUnbakedModel;
import dev.turtywurty.industria.molding.MoldCastPreviewRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public class FabricIndustriaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricIndustriaTestWorldButton.init();
        IndustriaClient.onInitializeClient();
        IndustriaClient.onRegistriesAppliedClient();

        PictureInPictureRendererRegistry.register(_ -> new MoldCastPreviewRenderer());

        UnbakedModelDeserializer.register(Industria.id("connected"), new FabricConnectedTextureDeserializer());
        ModelLoadingPlugin.register(pluginContext ->
                pluginContext.modifyBlockModelAfterBake().register(ModelModifier.WRAP_LAST_PHASE,
                        (model, context) -> {
                            Set<Identifier> dependencies = new HashSet<>();
                            context.sourceModel().resolveDependencies(dependencies::add);
                            for (Identifier dependency : dependencies) {
                                ResolvedModel resolved = context.baker().getModel(dependency);
                                ConnectedTextureSettings settings =
                                        FabricConnectedUnbakedModel.findSettings(resolved);

                                if (settings != null)
                                    return new FabricConnectedTextureModel(model, settings,
                                            context.baker().materials().spriteFinder(QuadAtlas.BLOCK));
                            }

                            return model;
                        }));
    }
}
