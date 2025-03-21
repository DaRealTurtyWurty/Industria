package dev.turtywurty.industria.init;

import dev.turtywurty.industria.block.MultiblockBlock;
import dev.turtywurty.industria.renderer.world.FluidPocketWorldRenderer;
import dev.turtywurty.industria.renderer.world.PipeNetworkWorldRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class ClientEventsInit {
    public static void init() {
        ClientTickEvents.START_WORLD_TICK.register(world -> MultiblockBlock.SHAPE_CACHE.clear());

        var fluidPocketWorldRenderer = new FluidPocketWorldRenderer();
        WorldRenderEvents.AFTER_ENTITIES.register(fluidPocketWorldRenderer::render);

        var pipeNetworkWorldRenderer = new PipeNetworkWorldRenderer();
        WorldRenderEvents.AFTER_ENTITIES.register(pipeNetworkWorldRenderer::render);
    }
}
