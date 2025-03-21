package dev.turtywurty.industria.init;

import dev.turtywurty.industria.screen.*;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class ScreenInit {
    public static void init() {
        HandledScreens.register(ScreenHandlerTypeInit.ALLOY_FURNACE, AlloyFurnaceScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.THERMAL_GENERATOR, ThermalGeneratorScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.BATTERY, BatteryScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.COMBUSTION_GENERATOR, CombustionGeneratorScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.SOLAR_PANEL, SolarPanelScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.CRUSHER, CrusherScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.WIND_TURBINE, WindTurbineScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.OIL_PUMP_JACK, OilPumpJackScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.DRILL, DrillScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.MOTOR, MotorScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.UPGRADE_STATION, UpgradeStationScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.ELECTRIC_FURNACE, ElectricFurnaceScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.FRACTIONAL_DISTILLATION_CONTROLLER, FractionalDistillationControllerScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.INDUCTION_HEATER, InductionHeaterScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.FLUID_PUMP, FluidPumpScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.MIXER, MixerScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.DIGESTER, DigesterScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.CLARIFIER, ClarifierScreen::new);
    }
}
