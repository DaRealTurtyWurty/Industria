package dev.turtywurty.industria.init;

import dev.turtywurty.industria.screen.*;
import dev.turtywurty.turtymultiloader.menu.client.ClientMenus;

public class ModScreens {
    public static void init() {
        ClientMenus.register(ModMenuTypes.ALLOY_FURNACE, AlloyFurnaceScreen::new);
        ClientMenus.register(ModMenuTypes.THERMAL_GENERATOR, ThermalGeneratorScreen::new);
        ClientMenus.register(ModMenuTypes.BATTERY, BatteryScreen::new);
        ClientMenus.register(ModMenuTypes.COMBUSTION_GENERATOR, CombustionGeneratorScreen::new);
        ClientMenus.register(ModMenuTypes.SOLAR_PANEL, SolarPanelScreen::new);
        ClientMenus.register(ModMenuTypes.CRUSHER, CrusherScreen::new);
        ClientMenus.register(ModMenuTypes.WIND_TURBINE, WindTurbineScreen::new);
        ClientMenus.register(ModMenuTypes.OIL_PUMP_JACK, OilPumpJackScreen::new);
        ClientMenus.register(ModMenuTypes.DRILL, DrillScreen::new);
        ClientMenus.register(ModMenuTypes.MOTOR, MotorScreen::new);
        ClientMenus.register(ModMenuTypes.UPGRADE_STATION, UpgradeStationScreen::new);
        ClientMenus.register(ModMenuTypes.ELECTRIC_FURNACE, ElectricFurnaceScreen::new);
        ClientMenus.register(ModMenuTypes.INDUCTION_HEATER, InductionHeaterScreen::new);
        ClientMenus.register(ModMenuTypes.FLUID_PUMP, FluidPumpScreen::new);
        ClientMenus.register(ModMenuTypes.MIXER, MixerScreen::new);
        ClientMenus.register(ModMenuTypes.DIGESTER, DigesterScreen::new);
        ClientMenus.register(ModMenuTypes.CLARIFIER, ClarifierScreen::new);
        ClientMenus.register(ModMenuTypes.CRYSTALLIZER, CrystallizerScreen::new);
        ClientMenus.register(ModMenuTypes.ELECTROLYZER, ElectrolyzerScreen::new);
        ClientMenus.register(ModMenuTypes.FLUID_TANK, FluidTankScreen::new);
        ClientMenus.register(ModMenuTypes.SHAKING_TABLE, ShakingTableScreen::new);
        ClientMenus.register(ModMenuTypes.CENTRIFUGAL_CONCENTRATOR, CentrifugalConcentratorScreen::new);
        ClientMenus.register(ModMenuTypes.ARC_FURNACE, ArcFurnaceScreen::new);
        ClientMenus.register(ModMenuTypes.FILTER_CONVEYOR, FilterConveyorScreen::new);
        ClientMenus.register(ModMenuTypes.DETECTOR_CONVEYOR, DetectorConveyorScreen::new);
        ClientMenus.register(ModMenuTypes.CONTAINMENT_CONVEYOR, ContainmentConveyorScreen::new);
        ClientMenus.register(ModMenuTypes.AGITATOR, AgitatorScreen::new);
        ClientMenus.register(ModMenuTypes.DISTILLATION_TOWER, DistillationTowerScreen::new);
    }
}
