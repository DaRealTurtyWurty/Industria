package dev.turtywurty.industria.init;

import dev.turtywurty.industria.conveyor.block.impl.entity.FeederConveyorBlockEntity;
import dev.turtywurty.industria.renderer.block.RotaryKilnBlockEntityRenderer;
import dev.turtywurty.industria.renderer.conveyor.FeederConveyorSpecialRenderer;
import dev.turtywurty.industria.renderer.conveyor.HatchConveyorSpecialRenderer;
import dev.turtywurty.industria.renderer.world.ConveyorNetworkLevelRenderer;
import dev.turtywurty.industria.renderer.world.FluidPocketLevelRenderer;
import dev.turtywurty.industria.renderer.world.MultiblockExportSelectionRenderer;
import dev.turtywurty.industria.renderer.world.PipeNetworkLevelRenderer;
import dev.turtywurty.turtymultiloader.event.client.ClientEvents;
import dev.turtywurty.turtymultiloader.event.client.RenderStage;

public class ModClientEvents {
    public static void init() {
        ClientEvents.onStartLevelTick(level -> {
            FeederConveyorSpecialRenderer.INSTANCE.onTick(level);
            HatchConveyorSpecialRenderer.INSTANCE.onTick(level);
        });

        ClientEvents.onBlockEntityUnload((blockEntity, level) -> {
            if (blockEntity instanceof FeederConveyorBlockEntity) {
                FeederConveyorSpecialRenderer.INSTANCE.onFeederRemoved(level.dimension(), blockEntity.getBlockPos());
            }
        });


        ClientEvents.onLevelLeave(level -> {
            RotaryKilnBlockEntityRenderer.BLOCK_POS_RENDERER_DATA_MAP.clear();
            FeederConveyorSpecialRenderer.INSTANCE.onDimensionUnload(level.dimension());
            HatchConveyorSpecialRenderer.INSTANCE.onDimensionUnload(level.dimension());
        });

        var fluidPocketLevelRenderer = new FluidPocketLevelRenderer();
        var pipeNetworkLevelRenderer = new PipeNetworkLevelRenderer();
        var multiblockExportSelectionRenderer = new MultiblockExportSelectionRenderer();

        ClientEvents.onRenderStage(RenderStage.AFTER_SOLID_FEATURES, context -> {
            fluidPocketLevelRenderer.render(context);
            pipeNetworkLevelRenderer.render(context);
            multiblockExportSelectionRenderer.render(context);
        });

        var conveyorNetworkLevelRenderer = new ConveyorNetworkLevelRenderer();
        ClientEvents.onRenderStage(RenderStage.COLLECT_SUBMITS, conveyorNetworkLevelRenderer::render);
    }
}
