package dev.turtywurty.industria.init;

import dev.turtywurty.industria.util.WoodRegistrySet;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactories;

public class EntityRendererInit {
    public static void init() {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            EntityRendererFactories.register(woodSet.boatEntityType,
                    ctx -> new BoatEntityRenderer(ctx,
                            EntityModelLayerInit.getBoatModelLayer(woodSet)));
            EntityRendererFactories.register(woodSet.chestBoatEntityType,
                    ctx -> new BoatEntityRenderer(ctx,
                            EntityModelLayerInit.getChestBoatModelLayer(woodSet)));
        }
    }
}
