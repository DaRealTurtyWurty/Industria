package dev.turtywurty.industria.neoforge.connected;

import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;

public class NeoForgeConnectedUnbakedModel extends DelegateUnbakedModel {
    public NeoForgeConnectedUnbakedModel(UnbakedModel delegate) {
        super(delegate);
    }

    public static boolean isConnected(ResolvedModel model) {
        while (model != null) {
            if (model.wrapped() instanceof NeoForgeConnectedUnbakedModel)
                return true;

            model = model.parent();
        }

        return false;
    }
}
