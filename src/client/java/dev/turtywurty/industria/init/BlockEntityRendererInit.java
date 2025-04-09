package dev.turtywurty.industria.init;

import dev.turtywurty.industria.renderer.block.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class BlockEntityRendererInit {
    public static void init() {
        BlockEntityRendererFactories.register(BlockEntityTypeInit.CRUSHER, CrusherBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.WIND_TURBINE, WindTurbineBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.OIL_PUMP_JACK, OilPumpJackBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.DRILL, DrillBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.MOTOR, MotorBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.UPGRADE_STATION, UpgradeStationBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.MIXER, MixerBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.DIGESTER, DigesterBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.MULTIBLOCK_IO, MultiblockIOBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.CLARIFIER, ClarifierBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.CRYSTALLIZER, CrystallizerBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.ROTARY_KILN_CONTROLLER, RotaryKilnBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.ELECTROLYZER, ElectrolyzerBlockEntityRenderer::new);
    }
}
