package dev.turtywurty.industria.init;

import dev.turtywurty.industria.screen.*;
import net.minecraft.client.gui.screens.MenuScreens;

public class ScreenInit {
    public static void init() {
        MenuScreens.register(ScreenHandlerTypeInit.ALLOY_FURNACE, AlloyFurnaceScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.THERMAL_GENERATOR, ThermalGeneratorScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.BATTERY, BatteryScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.COMBUSTION_GENERATOR, CombustionGeneratorScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.SOLAR_PANEL, SolarPanelScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.CRUSHER, CrusherScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.WIND_TURBINE, WindTurbineScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.OIL_PUMP_JACK, OilPumpJackScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.DRILL, DrillScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.MOTOR, MotorScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.UPGRADE_STATION, UpgradeStationScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.ELECTRIC_FURNACE, ElectricFurnaceScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.FRACTIONAL_DISTILLATION_CONTROLLER, FractionalDistillationControllerScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.INDUCTION_HEATER, InductionHeaterScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.FLUID_PUMP, FluidPumpScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.MIXER, MixerScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.DIGESTER, DigesterScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.CLARIFIER, ClarifierScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.CRYSTALLIZER, CrystallizerScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.ELECTROLYZER, ElectrolyzerScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.FLUID_TANK, FluidTankScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.SHAKING_TABLE, ShakingTableScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.CENTRIFUGAL_CONCENTRATOR, CentrifugalConcentratorScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.ARC_FURNACE, ArcFurnaceScreen::new);
        MenuScreens.register(ScreenHandlerTypeInit.MULTIBLOCK_DESIGNER, MultiblockDesignerScreen::new);
    }
}
