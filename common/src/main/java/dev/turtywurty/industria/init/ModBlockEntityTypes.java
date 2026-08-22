package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.conveyor.block.impl.entity.*;
import dev.turtywurty.turtymultiloader.registration.BlockEntityTypeBuilder;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ModBlockEntityTypes {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<AlloyFurnaceBlockEntity>> ALLOY_FURNACE = register("alloy_furnace",
            AlloyFurnaceBlockEntity::new, builder -> builder.validBlock(ModBlocks.ALLOY_FURNACE));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<ThermalGeneratorBlockEntity>> THERMAL_GENERATOR = register("thermal_generator",
            ThermalGeneratorBlockEntity::new, builder -> builder.validBlock(ModBlocks.THERMAL_GENERATOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<BatteryBlockEntity>> BATTERY = register("battery",
            (pos, state) -> new BatteryBlockEntity(((BatteryBlock) state.getBlock()), pos, state),
            builder -> builder.validBlocks(ModBlocks.BASIC_BATTERY, ModBlocks.ADVANCED_BATTERY, ModBlocks.ELITE_BATTERY, ModBlocks.ULTIMATE_BATTERY, ModBlocks.CREATIVE_BATTERY));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<CombustionGeneratorBlockEntity>> COMBUSTION_GENERATOR = register("combustion_generator",
            CombustionGeneratorBlockEntity::new, builder -> builder.validBlock(ModBlocks.COMBUSTION_GENERATOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL = register("solar_panel",
            SolarPanelBlockEntity::new, builder -> builder.validBlocks(ModBlocks.SOLAR_PANEL, ModBlocks.ADVANCED_SOLAR_PANEL));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<CrusherBlockEntity>> CRUSHER = register("crusher",
            CrusherBlockEntity::new, builder -> builder.validBlock(ModBlocks.CRUSHER));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<WindTurbineBlockEntity>> WIND_TURBINE = register("wind_turbine",
            WindTurbineBlockEntity::new, builder -> builder.validBlock(ModBlocks.WIND_TURBINE));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<OilPumpJackBlockEntity>> OIL_PUMP_JACK = register("oil_pump_jack",
            OilPumpJackBlockEntity::new, builder -> builder.validBlock(ModBlocks.OIL_PUMP_JACK));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<DrillBlockEntity>> DRILL = register("drill",
            DrillBlockEntity::new, builder -> builder.validBlock(ModBlocks.DRILL));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<MotorBlockEntity>> MOTOR = register("motor",
            MotorBlockEntity::new, builder -> builder.validBlock(ModBlocks.MOTOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<UpgradeStationBlockEntity>> UPGRADE_STATION = register("upgrade_station",
            UpgradeStationBlockEntity::new, builder -> builder.validBlock(ModBlocks.UPGRADE_STATION));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE = register("electric_furnace",
            ElectricFurnaceBlockEntity::new, builder -> builder.validBlock(ModBlocks.ELECTRIC_FURNACE));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<InductionHeaterBlockEntity>> INDUCTION_HEATER = register("induction_heater",
            InductionHeaterBlockEntity::new, builder -> builder.validBlock(ModBlocks.INDUCTION_HEATER));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<FluidPumpBlockEntity>> FLUID_PUMP = register("fluid_pump",
            FluidPumpBlockEntity::new, builder -> builder.validBlock(ModBlocks.FLUID_PUMP));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<MixerBlockEntity>> MIXER = register("mixer",
            MixerBlockEntity::new, builder -> builder.validBlock(ModBlocks.MIXER));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<DigesterBlockEntity>> DIGESTER = register("digester",
            DigesterBlockEntity::new, builder -> builder.validBlock(ModBlocks.DIGESTER));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<MultiblockIOBlockEntity>> AUTO_MULTIBLOCK_IO = register("multiblock_io",
            MultiblockIOBlockEntity::new, builder -> builder.validBlock(ModBlocks.AUTO_MULTIBLOCK_IO));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<ClarifierBlockEntity>> CLARIFIER = register("clarifier",
            ClarifierBlockEntity::new, builder -> builder.validBlock(ModBlocks.CLARIFIER));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<CrystallizerBlockEntity>> CRYSTALLIZER = register("crystallizer",
            CrystallizerBlockEntity::new, builder -> builder.validBlock(ModBlocks.CRYSTALLIZER));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<RotaryKilnControllerBlockEntity>> ROTARY_KILN_CONTROLLER = register("rotary_kiln_controller",
            RotaryKilnControllerBlockEntity::new, builder -> builder.validBlock(ModBlocks.ROTARY_KILN_CONTROLLER));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<RotaryKilnBlockEntity>> ROTARY_KILN = register("rotary_kiln",
            RotaryKilnBlockEntity::new, builder -> builder.validBlock(ModBlocks.ROTARY_KILN));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<ElectrolyzerBlockEntity>> ELECTROLYZER = register("electrolyzer",
            ElectrolyzerBlockEntity::new, builder -> builder.validBlock(ModBlocks.ELECTROLYZER));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<FluidTankBlockEntity>> FLUID_TANK = register("fluid_tank",
            FluidTankBlockEntity::new, builder -> builder.validBlock(ModBlocks.FLUID_TANK));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<WellheadBlockEntity>> WELLHEAD = register("wellhead",
            WellheadBlockEntity::new, builder -> builder.validBlock(ModBlocks.WELLHEAD));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<ShakingTableBlockEntity>> SHAKING_TABLE = register("shaking_table",
            ShakingTableBlockEntity::new, builder -> builder.validBlock(ModBlocks.SHAKING_TABLE));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<CentrifugalConcentratorBlockEntity>> CENTRIFUGAL_CONCENTRATOR = register("centrifugal_concentrator",
            CentrifugalConcentratorBlockEntity::new, builder -> builder.validBlock(ModBlocks.CENTRIFUGAL_CONCENTRATOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<ArcFurnaceBlockEntity>> ARC_FURNACE = register("arc_furnace",
            ArcFurnaceBlockEntity::new, builder -> builder.validBlock(ModBlocks.ARC_FURNACE));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<FeederConveyorBlockEntity>> FEEDER_CONVEYOR = register("feeder_conveyor",
            FeederConveyorBlockEntity::new, builder -> builder.validBlock(ModBlocks.FEEDER_CONVEYOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<FilterConveyorBlockEntity>> FILTER_CONVEYOR = register("filter_conveyor",
            FilterConveyorBlockEntity::new, builder -> builder.validBlock(ModBlocks.FILTER_CONVEYOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<DetectorConveyorBlockEntity>> DETECTOR_CONVEYOR = register("detector_conveyor",
            DetectorConveyorBlockEntity::new, builder -> builder.validBlock(ModBlocks.DETECTOR_CONVEYOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<MagneticConveyorBlockEntity>> MAGNETIC_CONVEYOR = register("magnetic_conveyor",
            MagneticConveyorBlockEntity::new, builder -> builder.validBlock(ModBlocks.MAGNETIC_CONVEYOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<CountConveyorBlockEntity>> COUNT_CONVEYOR = register("count_conveyor",
            CountConveyorBlockEntity::new, builder -> builder.validBlock(ModBlocks.COUNT_CONVEYOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<DelayConveyorBlockEntity>> DELAY_CONVEYOR = register("delay_conveyor",
            DelayConveyorBlockEntity::new, builder -> builder.validBlock(ModBlocks.DELAY_CONVEYOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<ContainmentConveyorBlockEntity>> CONTAINMENT_CONVEYOR = register("containment_conveyor",
            ContainmentConveyorBlockEntity::new, builder -> builder.validBlock(ModBlocks.CONTAINMENT_CONVEYOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<TreeTapBlockEntity>> TREE_TAP = register("tree_tap",
            TreeTapBlockEntity::new, builder -> builder.validBlock(ModBlocks.TREE_TAP));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<AgitatorBlockEntity>> AGITATOR = register("agitator",
            AgitatorBlockEntity::new, builder -> builder.validBlock(ModBlocks.AGITATOR));

    public static final RegistrationHandle<BlockEntityType<?>, BlockEntityType<DistillationTowerBlockEntity>> DISTILLATION_TOWER = register("distillation_tower",
            DistillationTowerBlockEntity::new, builder -> builder.validBlock(ModBlocks.DISTILLATION_TOWER));

    public static <T extends BlockEntity> RegistrationHandle<BlockEntityType<?>, BlockEntityType<T>> register(
            String name,
            BiFunction<BlockPos, BlockState, T> factory,
            Consumer<BlockEntityTypeBuilder<T>> configuration
    ) {
        return REGISTRIES.registerBlockEntityType(Industria.id(name), factory, configuration);
    }

    public static void init() {
    }
}
