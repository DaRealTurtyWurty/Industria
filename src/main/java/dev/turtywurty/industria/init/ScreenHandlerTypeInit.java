package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.network.UpgradeStationOpenPayload;
import dev.turtywurty.industria.screenhandler.*;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ScreenHandlerTypeInit {
    public static final MenuType<AlloyFurnaceScreenHandler> ALLOY_FURNACE =
            register("alloy_furnace", AlloyFurnaceScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<ThermalGeneratorScreenHandler> THERMAL_GENERATOR =
            register("thermal_generator", ThermalGeneratorScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<BatteryScreenHandler> BATTERY =
            register("battery", BatteryScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<CombustionGeneratorScreenHandler> COMBUSTION_GENERATOR =
            register("combustion_generator", CombustionGeneratorScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<SolarPanelScreenHandler> SOLAR_PANEL =
            register("solar_panel", SolarPanelScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<CrusherScreenHandler> CRUSHER =
            register("crusher", CrusherScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<WindTurbineScreenHandler> WIND_TURBINE =
            register("wind_turbine", WindTurbineScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<OilPumpJackScreenHandler> OIL_PUMP_JACK =
            register("oil_pump_jack", OilPumpJackScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<DrillScreenHandler> DRILL =
            register("drill", DrillScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<MotorScreenHandler> MOTOR =
            register("motor", MotorScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<UpgradeStationScreenHandler> UPGRADE_STATION =
            register("upgrade_station", UpgradeStationScreenHandler::new, UpgradeStationOpenPayload.CODEC);

    public static final MenuType<ElectricFurnaceScreenHandler> ELECTRIC_FURNACE =
            register("electric_furnace", ElectricFurnaceScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<FractionalDistillationControllerScreenHandler> FRACTIONAL_DISTILLATION_CONTROLLER =
            register("fractional_distillation_controller", FractionalDistillationControllerScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<InductionHeaterScreenHandler> INDUCTION_HEATER =
            register("induction_heater", InductionHeaterScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<FluidPumpScreenHandler> FLUID_PUMP =
            register("fluid_pump", FluidPumpScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<MixerScreenHandler> MIXER =
            register("mixer", MixerScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<DigesterScreenHandler> DIGESTER =
            register("digester", DigesterScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<ClarifierScreenHandler> CLARIFIER =
            register("clarifier", ClarifierScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<CrystallizerScreenHandler> CRYSTALLIZER =
            register("crystallizer", CrystallizerScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<ElectrolyzerScreenHandler> ELECTROLYZER =
            register("electrolyzer", ElectrolyzerScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<FluidTankScreenHandler> FLUID_TANK =
            register("fluid_tank", FluidTankScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<ShakingTableScreenHandler> SHAKING_TABLE =
            register("shaking_table", ShakingTableScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<CentrifugalConcentratorScreenHandler> CENTRIFUGAL_CONCENTRATOR =
            register("centrifugal_concentrator", CentrifugalConcentratorScreenHandler::new, BlockPosPayload.CODEC);

    public static final MenuType<ArcFurnaceScreenHandler> ARC_FURNACE =
            register("arc_furnace", ArcFurnaceScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuType<MultiblockDesignerScreenHandler, BlockPosPayload> MULTIBLOCK_DESIGNER =
            register("multiblock_designer", MultiblockDesignerScreenHandler::new, BlockPosPayload.CODEC);

    public static <T extends AbstractContainerMenu, D extends CustomPacketPayload> ExtendedMenuType<T, D> register(String name, ExtendedMenuType.ExtendedFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        return Registry.register(BuiltInRegistries.MENU, Industria.id(name), new ExtendedMenuType<>(factory, codec));
    }

    public static void init() {
    }
}
