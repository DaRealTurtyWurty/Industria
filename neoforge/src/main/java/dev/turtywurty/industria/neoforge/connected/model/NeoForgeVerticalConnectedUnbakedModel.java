package dev.turtywurty.industria.neoforge.connected.model;

import dev.turtywurty.industria.connected.model.VerticalConnectedModelSettings;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;
import org.jspecify.annotations.Nullable;

public class NeoForgeVerticalConnectedUnbakedModel extends DelegateUnbakedModel {
    private final VerticalConnectedModelSettings settings;

    public NeoForgeVerticalConnectedUnbakedModel(UnbakedModel model, VerticalConnectedModelSettings settings) {
        super(model);
        this.settings = settings;
    }

    public VerticalConnectedModelSettings getSettings() {
        return settings;
    }

    public static @Nullable VerticalConnectedModelSettings findSettings(ResolvedModel model) {
        while (model != null) {
            if (model.wrapped() instanceof NeoForgeVerticalConnectedUnbakedModel neoForgeModel)
                return neoForgeModel.getSettings();

            model = model.parent();
        }

        return null;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        super.resolveDependencies(resolver);
        settings.resolveDependencies(resolver);
    }
}
