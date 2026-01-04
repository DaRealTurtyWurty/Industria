package dev.turtywurty.industria.init;

import dev.turtywurty.industria.renderer.block.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class BlockEntityRendererInit {
    public static void init() {
        BlockEntityRenderers.register(BlockEntityTypeInit.CRUSHER, CrusherBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.WIND_TURBINE, WindTurbineBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.OIL_PUMP_JACK, OilPumpJackBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.DRILL, DrillBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.MOTOR, MotorBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.UPGRADE_STATION, UpgradeStationBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.MIXER, MixerBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.DIGESTER, DigesterBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.AUTO_MULTIBLOCK_IO, MultiblockIOBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.CLARIFIER, ClarifierBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.CRYSTALLIZER, CrystallizerBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.ROTARY_KILN_CONTROLLER, RotaryKilnBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.ELECTROLYZER, ElectrolyzerBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.FLUID_TANK, FluidTankBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.SHAKING_TABLE, ShakingTableBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.CENTRIFUGAL_CONCENTRATOR, CentrifugalConcentratorBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.ARC_FURNACE, ArcFurnaceBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.EXAMPLE_MULTIBLOCK_CONTROLLER, ExampleMultiblockControllerBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypeInit.MULTIBLOCK_DESIGNER, MultiblockDesignerBlockEntityRenderer::new);
    }
}
