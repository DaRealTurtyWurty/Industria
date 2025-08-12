package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.network.UpgradeStationOpenPayload;
import dev.turtywurty.industria.screenhandler.*;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ScreenHandlerTypeInit {
    public static <T extends ScreenHandler, D extends CustomPayload> ExtendedScreenHandlerType<T, D> register(String name, ExtendedScreenHandlerType.ExtendedFactory<T, D> factory, PacketCodec<? super RegistryByteBuf, D> codec) {
        return Registry.register(Registries.SCREEN_HANDLER, Industria.id(name), new ExtendedScreenHandlerType<>(factory, codec));
    }    public static final ScreenHandlerType<AlloyFurnaceScreenHandler> ALLOY_FURNACE =
            register("alloy_furnace", AlloyFurnaceScreenHandler::new, BlockPosPayload.CODEC);

    public static void init() {
    }    public static final ScreenHandlerType<ThermalGeneratorScreenHandler> THERMAL_GENERATOR =
            register("thermal_generator", ThermalGeneratorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<BatteryScreenHandler> BATTERY =
            register("battery", BatteryScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<CombustionGeneratorScreenHandler> COMBUSTION_GENERATOR =
            register("combustion_generator", CombustionGeneratorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<SolarPanelScreenHandler> SOLAR_PANEL =
            register("solar_panel", SolarPanelScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<CrusherScreenHandler> CRUSHER =
            register("crusher", CrusherScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<WindTurbineScreenHandler> WIND_TURBINE =
            register("wind_turbine", WindTurbineScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<OilPumpJackScreenHandler> OIL_PUMP_JACK =
            register("oil_pump_jack", OilPumpJackScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<DrillScreenHandler> DRILL =
            register("drill", DrillScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<MotorScreenHandler> MOTOR =
            register("motor", MotorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<UpgradeStationScreenHandler> UPGRADE_STATION =
            register("upgrade_station", UpgradeStationScreenHandler::new, UpgradeStationOpenPayload.CODEC);

    public static final ScreenHandlerType<ElectricFurnaceScreenHandler> ELECTRIC_FURNACE =
            register("electric_furnace", ElectricFurnaceScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<FractionalDistillationControllerScreenHandler> FRACTIONAL_DISTILLATION_CONTROLLER =
            register("fractional_distillation_controller", FractionalDistillationControllerScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<InductionHeaterScreenHandler> INDUCTION_HEATER =
            register("induction_heater", InductionHeaterScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<FluidPumpScreenHandler> FLUID_PUMP =
            register("fluid_pump", FluidPumpScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<MixerScreenHandler> MIXER =
            register("mixer", MixerScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<DigesterScreenHandler> DIGESTER =
            register("digester", DigesterScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<ClarifierScreenHandler> CLARIFIER =
            register("clarifier", ClarifierScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<CrystallizerScreenHandler> CRYSTALLIZER =
            register("crystallizer", CrystallizerScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<ElectrolyzerScreenHandler> ELECTROLYZER =
            register("electrolyzer", ElectrolyzerScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<FluidTankScreenHandler> FLUID_TANK =
            register("fluid_tank", FluidTankScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<ShakingTableScreenHandler> SHAKING_TABLE =
            register("shaking_table", ShakingTableScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<CentrifugalConcentratorScreenHandler> CENTRIFUGAL_CONCENTRATOR =
            register("centrifugal_concentrator", CentrifugalConcentratorScreenHandler::new, BlockPosPayload.CODEC);




}
