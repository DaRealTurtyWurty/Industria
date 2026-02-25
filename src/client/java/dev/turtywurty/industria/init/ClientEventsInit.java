package dev.turtywurty.industria.init;

import dev.turtywurty.industria.multiblock.old.AutoMultiblockBlock;
import dev.turtywurty.industria.renderer.block.RotaryKilnBlockEntityRenderer;
import dev.turtywurty.industria.renderer.world.FluidPocketWorldRenderer;
import dev.turtywurty.industria.renderer.world.PipeNetworkWorldRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ClientEventsInit {
    public static void init() {
        ClientTickEvents.START_LEVEL_TICK.register(world ->
                AutoMultiblockBlock.SHAPE_CACHE.clear());

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, world) ->
                RotaryKilnBlockEntityRenderer.BLOCK_POS_RENDERER_DATA_MAP.clear());

        var fluidPocketWorldRenderer = new FluidPocketWorldRenderer();
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(fluidPocketWorldRenderer::render);

        var pipeNetworkWorldRenderer = new PipeNetworkWorldRenderer();
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(pipeNetworkWorldRenderer::render);
    }
}
