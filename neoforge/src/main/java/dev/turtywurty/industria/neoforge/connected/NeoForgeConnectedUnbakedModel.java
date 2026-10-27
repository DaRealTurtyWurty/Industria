package dev.turtywurty.industria.neoforge.connected;

import dev.turtywurty.industria.connected.ConnectedTextureSettings;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;
import org.jspecify.annotations.Nullable;

public class NeoForgeConnectedUnbakedModel extends DelegateUnbakedModel {
    private final ConnectedTextureSettings settings;

    public NeoForgeConnectedUnbakedModel(UnbakedModel delegate, ConnectedTextureSettings settings) {
        super(delegate);
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
            if (model.wrapped() instanceof NeoForgeConnectedUnbakedModel connectedModel)
                return connectedModel.getSettings();

            model = model.parent();
        }

        return null;
    }
}
