package dev.turtywurty.industria.connected.model;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.minecraft.client.resources.model.ModelBaker;

public record FabricVerticalConnectedModelDependencies(
        VerticalConnectedModelSettings settings) implements UnbakedExtraModel<VerticalConnectedModelSettings> {
    @Override
    public void resolveDependencies(Resolver resolver) {
        settings.resolveDependencies(resolver);
    }

    @Override
    public VerticalConnectedModelSettings bake(ModelBaker baker) {
        return settings;
    }
}
