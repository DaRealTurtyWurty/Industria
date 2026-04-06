package dev.turtywurty.industria.init;

import dev.turtywurty.industria.conveyor.block.impl.entity.FeederConveyorBlockEntity;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockBlock;
import dev.turtywurty.industria.renderer.block.RotaryKilnBlockEntityRenderer;
import dev.turtywurty.industria.renderer.conveyor.FeederConveyorSpecialRenderer;
import dev.turtywurty.industria.renderer.conveyor.HatchConveyorSpecialRenderer;
import dev.turtywurty.industria.renderer.world.ConveyorNetworkLevelRenderer;
import dev.turtywurty.industria.renderer.world.FluidPocketLevelRenderer;
import dev.turtywurty.industria.renderer.world.PipeNetworkLevelRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class ClientEventsInit {
    public static void init() {
        ClientTickEvents.START_LEVEL_TICK.register(level -> {
            AutoMultiblockBlock.SHAPE_CACHE.clear();
            FeederConveyorSpecialRenderer.INSTANCE.onTick(level);
            HatchConveyorSpecialRenderer.INSTANCE.onTick(level);
        });

        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> {
            if(blockEntity instanceof FeederConveyorBlockEntity) {
                FeederConveyorSpecialRenderer.INSTANCE.onFeederRemoved(level.dimension(), blockEntity.getBlockPos());
            }
        });

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((_, level) -> {
            RotaryKilnBlockEntityRenderer.BLOCK_POS_RENDERER_DATA_MAP.clear();
            FeederConveyorSpecialRenderer.INSTANCE.onDimensionUnload(level.dimension());
            HatchConveyorSpecialRenderer.INSTANCE.onDimensionUnload(level.dimension());
        });

        var fluidPocketLevelRenderer = new FluidPocketLevelRenderer();
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(fluidPocketLevelRenderer::render);

        var pipeNetworkLevelRenderer = new PipeNetworkLevelRenderer();
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(pipeNetworkLevelRenderer::render);

        var conveyorNetworkLevelRenderer = new ConveyorNetworkLevelRenderer();
        LevelRenderEvents.COLLECT_SUBMITS.register(conveyorNetworkLevelRenderer::render);
    }
}
