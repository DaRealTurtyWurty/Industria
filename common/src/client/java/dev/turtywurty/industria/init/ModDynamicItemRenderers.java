package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.renderer.item.DrillHeadItemRenderer;
import dev.turtywurty.industria.renderer.item.IndustriaBlockEntityItemRenderer;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;
import dev.turtywurty.turtymultiloader.event.client.ClientEvents;
import net.minecraft.util.Unit;

public class ModDynamicItemRenderers {
    public static void init() {
        ClientRegistrations.registerSpecialModelRenderer(Industria.id("drill_head"), DrillHeadItemRenderer.Unbaked.CODEC);
        ClientRegistrations.registerSpecialModelRenderer(Industria.id("block_entity_item"), IndustriaBlockEntityItemRenderer.Unbaked.CODEC);
        ClientEvents.registerResourceReloadListener(Industria.id("drill_head_item_renderer"),
                (_, _, preparationBarrier, applyExecutor) ->
                        preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
                            DrillHeadItemRenderer.INSTANCE.drillHeadModels.clear();
                            DrillHeadItemRenderer.INSTANCE.drillHeadTextures.clear();
                        }, applyExecutor));
    }
}
