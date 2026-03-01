package dev.turtywurty.industria.init;

import dev.turtywurty.industria.multiblock.old.AutoMultiblockBlock;
import dev.turtywurty.industria.renderer.block.RotaryKilnBlockEntityRenderer;
import dev.turtywurty.industria.renderer.world.ConveyorNetworkLevelRenderer;
import dev.turtywurty.industria.renderer.world.FluidPocketLevelRenderer;
import dev.turtywurty.industria.renderer.world.PipeNetworkLevelRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class ClientEventsInit {
    public static void init() {
        ClientTickEvents.START_LEVEL_TICK.register(world ->
                AutoMultiblockBlock.SHAPE_CACHE.clear());

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, world) ->
                RotaryKilnBlockEntityRenderer.BLOCK_POS_RENDERER_DATA_MAP.clear());

        var fluidPocketLevelRenderer = new FluidPocketLevelRenderer();
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(fluidPocketLevelRenderer::render);

        var pipeNetworkLevelRenderer = new PipeNetworkLevelRenderer();
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(pipeNetworkLevelRenderer::render);

        var conveyorNetworkLevelRenderer = new ConveyorNetworkLevelRenderer();
        LevelRenderEvents.COLLECT_SUBMITS.register(conveyorNetworkLevelRenderer::render);
    }
}
