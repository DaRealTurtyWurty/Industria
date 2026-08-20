package dev.turtywurty.industria.init;

import dev.turtywurty.industria.util.WoodRegistrySet;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class EntityRendererInit {
    public static void init() {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            EntityRenderers.register(woodSet.boatEntityType,
                    ctx -> new BoatRenderer(ctx,
                            EntityModelLayerInit.getBoatModelLayer(woodSet)));
            EntityRenderers.register(woodSet.chestBoatEntityType,
                    ctx -> new BoatRenderer(ctx,
                            EntityModelLayerInit.getChestBoatModelLayer(woodSet)));
        }
    }
}
