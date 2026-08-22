package dev.turtywurty.industria.init;

import dev.turtywurty.industria.renderer.block.*;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;

public class ModBlockEntityRenderers {
    public static void init() {
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.SOLAR_PANEL, AdvancedSolarPanelBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.CRUSHER, CrusherBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.WIND_TURBINE, WindTurbineBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.OIL_PUMP_JACK, OilPumpJackBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.DRILL, DrillBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.MOTOR, MotorBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.UPGRADE_STATION, UpgradeStationBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.MIXER, MixerBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.DIGESTER, DigesterBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.AUTO_MULTIBLOCK_IO, MultiblockIOBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.CLARIFIER, ClarifierBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.CRYSTALLIZER, CrystallizerBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.ROTARY_KILN_CONTROLLER, RotaryKilnBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.ELECTROLYZER, ElectrolyzerBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.FLUID_TANK, FluidTankBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.SHAKING_TABLE, ShakingTableBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.CENTRIFUGAL_CONCENTRATOR, CentrifugalConcentratorBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.ARC_FURNACE, ArcFurnaceBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.TREE_TAP, TreeTapBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.DISTILLATION_TOWER, DistillationTowerBlockEntityRenderer::new);
        ClientRegistrations.registerBlockEntityRenderer(ModBlockEntityTypes.FLUID_PUMP, FluidPumpBlockEntityRenderer::new);
    }
}
