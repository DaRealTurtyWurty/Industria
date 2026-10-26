package dev.turtywurty.industria.connected;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperUnbakedModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;

public class FabricConnectedUnbakedModel extends WrapperUnbakedModel {
    public FabricConnectedUnbakedModel(UnbakedModel model) {
        super(model);
    }

    public static boolean isConnected(ResolvedModel model) {
        while (model != null) {
            if (model.wrapped() instanceof FabricConnectedUnbakedModel)
                return true;

            model = model.parent();
        }

        return false;
    }
}
