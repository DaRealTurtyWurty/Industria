package dev.turtywurty.industria.connected;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperUnbakedModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import org.jspecify.annotations.Nullable;

public class FabricConnectedUnbakedModel extends WrapperUnbakedModel {
    private final ConnectedTextureSettings settings;

    public FabricConnectedUnbakedModel(UnbakedModel model, ConnectedTextureSettings settings) {
        super(model);
        this.settings = settings;
    }

    public ConnectedTextureSettings getSettings() {
        return settings;
    }

    public static boolean isConnected(ResolvedModel model) {
        return findSettings(model) != null;
    }

    public static @Nullable ConnectedTextureSettings findSettings(ResolvedModel model) {
        while (model != null) {
            if (model.wrapped() instanceof FabricConnectedUnbakedModel connectedModel)
                return connectedModel.getSettings();

            model = model.parent();
        }

        return null;
    }
}
