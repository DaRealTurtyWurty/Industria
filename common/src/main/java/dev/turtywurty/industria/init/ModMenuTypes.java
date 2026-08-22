package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.menu.*;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.network.UpgradeStationOpenPayload;
import dev.turtywurty.turtymultiloader.menu.ExtendedMenuFactory;
import dev.turtywurty.turtymultiloader.menu.ExtendedMenuRegistration;
import dev.turtywurty.turtymultiloader.menu.Menus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ModMenuTypes {
    public static final ExtendedMenuRegistration<AlloyFurnaceScreenHandler, BlockPosPayload> ALLOY_FURNACE =
            register("alloy_furnace", AlloyFurnaceScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<ThermalGeneratorScreenHandler, BlockPosPayload> THERMAL_GENERATOR =
            register("thermal_generator", ThermalGeneratorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<BatteryScreenHandler, BlockPosPayload> BATTERY =
            register("battery", BatteryScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<CombustionGeneratorScreenHandler, BlockPosPayload> COMBUSTION_GENERATOR =
            register("combustion_generator", CombustionGeneratorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<CrusherScreenHandler, BlockPosPayload> CRUSHER =
            register("crusher", CrusherScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<OilPumpJackScreenHandler, BlockPosPayload> OIL_PUMP_JACK =
            register("oil_pump_jack", OilPumpJackScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<DrillScreenHandler, BlockPosPayload> DRILL =
            register("drill", DrillScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<MotorScreenHandler, BlockPosPayload> MOTOR =
            register("motor", MotorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<UpgradeStationScreenHandler, UpgradeStationOpenPayload> UPGRADE_STATION =
            register("upgrade_station", UpgradeStationScreenHandler::new, UpgradeStationOpenPayload.CODEC);

    public static final ExtendedMenuRegistration<ElectricFurnaceScreenHandler, BlockPosPayload> ELECTRIC_FURNACE =
            register("electric_furnace", ElectricFurnaceScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<InductionHeaterScreenHandler, BlockPosPayload> INDUCTION_HEATER =
            register("induction_heater", InductionHeaterScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<MixerScreenHandler, BlockPosPayload> MIXER =
            register("mixer", MixerScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<DigesterScreenHandler, BlockPosPayload> DIGESTER =
            register("digester", DigesterScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<ClarifierScreenHandler, BlockPosPayload> CLARIFIER =
            register("clarifier", ClarifierScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<CrystallizerScreenHandler, BlockPosPayload> CRYSTALLIZER =
            register("crystallizer", CrystallizerScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<ElectrolyzerScreenHandler, BlockPosPayload> ELECTROLYZER =
            register("electrolyzer", ElectrolyzerScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<FluidTankScreenHandler, BlockPosPayload> FLUID_TANK =
            register("fluid_tank", FluidTankScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<ShakingTableScreenHandler, BlockPosPayload> SHAKING_TABLE =
            register("shaking_table", ShakingTableScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<CentrifugalConcentratorScreenHandler, BlockPosPayload> CENTRIFUGAL_CONCENTRATOR =
            register("centrifugal_concentrator", CentrifugalConcentratorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<ArcFurnaceScreenHandler, BlockPosPayload> ARC_FURNACE =
            register("arc_furnace", ArcFurnaceScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<FilterConveyorScreenHandler, BlockPosPayload> FILTER_CONVEYOR =
            register("filter_conveyor", FilterConveyorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<DetectorConveyorScreenHandler, BlockPosPayload> DETECTOR_CONVEYOR =
            register("detector_conveyor", DetectorConveyorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<ContainmentConveyorScreenHandler, BlockPosPayload> CONTAINMENT_CONVEYOR =
            register("containment_conveyor", ContainmentConveyorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<AgitatorScreenHandler, BlockPosPayload> AGITATOR =
            register("agitator", AgitatorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ExtendedMenuRegistration<DistillationTowerScreenHandler, BlockPosPayload> DISTILLATION_TOWER =
            register("distillation_tower", DistillationTowerScreenHandler::new, BlockPosPayload.CODEC);

    public static <T extends AbstractContainerMenu, D extends CustomPacketPayload> ExtendedMenuRegistration<T, D> register(String name, ExtendedMenuFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        return Menus.registerExtended(Industria.id(name), factory, codec);
    }

    public static void init() {
    }
}
