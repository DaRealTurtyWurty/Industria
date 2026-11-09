package dev.turtywurty.industria.connected.model;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperUnbakedModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import org.jspecify.annotations.Nullable;

public class FabricVerticalConnectedUnbakedModel extends WrapperUnbakedModel {
    private final VerticalConnectedModelSettings settings;

    public FabricVerticalConnectedUnbakedModel(UnbakedModel model, VerticalConnectedModelSettings settings) {
        super(model);
        this.settings = settings;
    }

    public VerticalConnectedModelSettings getSettings() {
        return settings;
    }

    public static @Nullable VerticalConnectedModelSettings findSettings(ResolvedModel model) {
        while (model != null) {
            if (model.wrapped() instanceof FabricVerticalConnectedUnbakedModel fabricModel)
                return fabricModel.getSettings();

            model = model.parent();
        }

        return null;
    }
}
