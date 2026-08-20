package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.renderer.item.DrillHeadItemRenderer;
import dev.turtywurty.industria.renderer.item.IndustriaBlockEntityItemRenderer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;

import java.util.concurrent.CompletableFuture;

public class DynamicItemRendererInit {
    public static void init() {
        SpecialModelRenderers.ID_MAPPER.put(Industria.id("drill_head"), DrillHeadItemRenderer.Unbaked.CODEC);
        SpecialModelRenderers.ID_MAPPER.put(Industria.id("block_entity_item"), IndustriaBlockEntityItemRenderer.Unbaked.CODEC);
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Industria.id("drill_head_item_renderer"),
                (_, _, preparationBarrier, applyExecutor) ->
                        preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
                            DrillHeadItemRenderer.INSTANCE.drillHeadModels.clear();
                            DrillHeadItemRenderer.INSTANCE.drillHeadTextures.clear();
                        }, applyExecutor));
    }
}
