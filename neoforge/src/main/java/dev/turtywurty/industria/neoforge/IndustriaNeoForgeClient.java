package dev.turtywurty.industria.neoforge;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.IndustriaClient;
import dev.turtywurty.industria.connected.ConnectedTextureSettings;
import dev.turtywurty.industria.molding.MoldCastPreviewRenderer;
import dev.turtywurty.industria.molding.MoldCastPreviewState;
import dev.turtywurty.industria.neoforge.connected.NeoForgeConnectedTextureLoader;
import dev.turtywurty.industria.neoforge.connected.NeoForgeConnectedTextureModel;
import dev.turtywurty.industria.neoforge.connected.NeoForgeConnectedUnbakedModel;
import dev.turtywurty.industria.neoforge.connected.model.NeoForgeVerticalConnectedBlockStateModel;
import dev.turtywurty.industria.neoforge.connected.model.NeoForgeVerticalConnectedModelLoader;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashSet;
import java.util.Set;

@Mod(value = IndustriaNeoForge.MOD_ID, dist = Dist.CLIENT)
public final class IndustriaNeoForgeClient {
    public IndustriaNeoForgeClient(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(NeoForgeIndustriaTestWorldButton::onScreenInit);
        IndustriaClient.onInitializeClient();
        modBus.addListener(FMLClientSetupEvent.class,
                event -> event.enqueueWork(IndustriaClient::onRegistriesAppliedClient));

        modBus.addListener(RegisterPictureInPictureRenderersEvent.class, this::registerPictureInPictureRenderers);

        modBus.addListener(ModelEvent.RegisterLoaders.class, this::registerModelLoaders);
        modBus.addListener(ModelEvent.ModifyBakingResult.class, this::modifyBakingResult);
        modBus.addListener(RegisterBlockStateModels.class, this::registerBlockStateModels);
    }

    private void registerPictureInPictureRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(MoldCastPreviewState.class, MoldCastPreviewRenderer::new);
    }

    private void registerModelLoaders(ModelEvent.RegisterLoaders event) {
        event.register(Industria.id("connected"), new NeoForgeConnectedTextureLoader());
        event.register(Industria.id("vertical_connected"), new NeoForgeVerticalConnectedModelLoader());
    }

    private void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        ModelBakery modelBakery = event.getModelBakery();
        event.getBakingResult().blockStateModels().replaceAll((state, model) -> {
            BlockStateModel.UnbakedRoot source = modelBakery.unbakedBlockStateModels.get(state);
            if (source == null)
                return model;

            Set<Identifier> dependencies = new HashSet<>();
            source.resolveDependencies(dependencies::add);
            for (Identifier dependency : dependencies) {
                ResolvedModel resolved = modelBakery.resolvedModels.get(dependency);
                ConnectedTextureSettings settings = NeoForgeConnectedUnbakedModel.findSettings(resolved);

                if (settings != null)
                    return new NeoForgeConnectedTextureModel(model, settings);
            }

            return model;
        });
    }

    private void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(Industria.id("vertical_connected"), NeoForgeVerticalConnectedBlockStateModel.CODEC);
    }
}
