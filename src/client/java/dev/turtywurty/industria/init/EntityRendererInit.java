package dev.turtywurty.industria.init;

import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.BoatEntityRenderer;

public class EntityRendererInit {
    public static void init() {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            EntityRendererRegistry.register(woodSet.boatEntityType,
                    ctx -> new BoatEntityRenderer(ctx,
                            EntityModelLayerInit.getBoatModelLayer(woodSet)));
            EntityRendererRegistry.register(woodSet.chestBoatEntityType,
                    ctx -> new BoatEntityRenderer(ctx,
                            EntityModelLayerInit.getChestBoatModelLayer(woodSet)));
        }
    }
}
