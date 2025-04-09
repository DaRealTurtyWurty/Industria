package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.blockentity.*;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BlockEntityTypeInit {
    public static final BlockEntityType<AlloyFurnaceBlockEntity> ALLOY_FURNACE = register("alloy_furnace",
            FabricBlockEntityTypeBuilder.create(AlloyFurnaceBlockEntity::new, BlockInit.ALLOY_FURNACE)
                    .build());

    public static final BlockEntityType<ThermalGeneratorBlockEntity> THERMAL_GENERATOR = register("thermal_generator",
            FabricBlockEntityTypeBuilder.create(ThermalGeneratorBlockEntity::new, BlockInit.THERMAL_GENERATOR)
                    .build());

    public static final BlockEntityType<BatteryBlockEntity> BATTERY = register("battery",
            FabricBlockEntityTypeBuilder.create(
                            (pos, state) -> new BatteryBlockEntity(pos, state, ((BatteryBlock) state.getBlock()).getLevel()),
                            BlockInit.BASIC_BATTERY, BlockInit.ADVANCED_BATTERY, BlockInit.ELITE_BATTERY,
                            BlockInit.ULTIMATE_BATTERY, BlockInit.CREATIVE_BATTERY)
                    .build());

    public static final BlockEntityType<CombustionGeneratorBlockEntity> COMBUSTION_GENERATOR = register("combustion_generator",
            FabricBlockEntityTypeBuilder.create(CombustionGeneratorBlockEntity::new, BlockInit.COMBUSTION_GENERATOR)
                    .build());

    public static final BlockEntityType<SolarPanelBlockEntity> SOLAR_PANEL = register("solar_panel",
            FabricBlockEntityTypeBuilder.create(SolarPanelBlockEntity::new, BlockInit.SOLAR_PANEL)
                    .build());

    public static final BlockEntityType<CrusherBlockEntity> CRUSHER = register("crusher",
            FabricBlockEntityTypeBuilder.create(CrusherBlockEntity::new, BlockInit.CRUSHER)
                    .build());

    public static final BlockEntityType<WindTurbineBlockEntity> WIND_TURBINE = register("wind_turbine",
            FabricBlockEntityTypeBuilder.create(WindTurbineBlockEntity::new, BlockInit.WIND_TURBINE)
                    .build());

    public static final BlockEntityType<OilPumpJackBlockEntity> OIL_PUMP_JACK = register("oil_pump_jack",
            FabricBlockEntityTypeBuilder.create(OilPumpJackBlockEntity::new, BlockInit.OIL_PUMP_JACK)
                    .build());

    public static final BlockEntityType<DrillBlockEntity> DRILL = register("drill",
            FabricBlockEntityTypeBuilder.create(DrillBlockEntity::new, BlockInit.DRILL)
                    .build());

    public static final BlockEntityType<MotorBlockEntity> MOTOR = register("motor",
            FabricBlockEntityTypeBuilder.create(MotorBlockEntity::new, BlockInit.MOTOR)
                    .build());

    public static final BlockEntityType<UpgradeStationBlockEntity> UPGRADE_STATION = register("upgrade_station",
            FabricBlockEntityTypeBuilder.create(UpgradeStationBlockEntity::new, BlockInit.UPGRADE_STATION)
                    .build());

    public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE = register("electric_furnace",
            FabricBlockEntityTypeBuilder.create(ElectricFurnaceBlockEntity::new, BlockInit.ELECTRIC_FURNACE)
                    .build());

    public static final BlockEntityType<FractionalDistillationControllerBlockEntity> FRACTIONAL_DISTILLATION_CONTROLLER = register("fractional_distillation_controller",
            FabricBlockEntityTypeBuilder.create(FractionalDistillationControllerBlockEntity::new, BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER)
                    .build());

    public static final BlockEntityType<FractionalDistillationTowerBlockEntity> FRACTIONAL_DISTILLATION_TOWER = register("fractional_distillation_tower",
            FabricBlockEntityTypeBuilder.create(FractionalDistillationTowerBlockEntity::new, BlockInit.FRACTIONAL_DISTILLATION_TOWER)
                    .build());

    public static final BlockEntityType<InductionHeaterBlockEntity> INDUCTION_HEATER = register("induction_heater",
            FabricBlockEntityTypeBuilder.create(InductionHeaterBlockEntity::new, BlockInit.INDUCTION_HEATER)
                    .build());

    public static final BlockEntityType<FluidPumpBlockEntity> FLUID_PUMP = register("fluid_pump",
            FabricBlockEntityTypeBuilder.create(FluidPumpBlockEntity::new, BlockInit.FLUID_PUMP)
                    .build());

    public static final BlockEntityType<MixerBlockEntity> MIXER = register("mixer",
            FabricBlockEntityTypeBuilder.create(MixerBlockEntity::new, BlockInit.MIXER)
                    .build());

    public static final BlockEntityType<DigesterBlockEntity> DIGESTER = register("digester",
            FabricBlockEntityTypeBuilder.create(DigesterBlockEntity::new, BlockInit.DIGESTER)
                    .build());

    public static final BlockEntityType<MultiblockIOBlockEntity> MULTIBLOCK_IO = register("multiblock_io",
            FabricBlockEntityTypeBuilder.create(MultiblockIOBlockEntity::new, BlockInit.MULTIBLOCK_IO)
                    .build());

    public static final BlockEntityType<ClarifierBlockEntity> CLARIFIER = register("clarifier",
            FabricBlockEntityTypeBuilder.create(ClarifierBlockEntity::new, BlockInit.CLARIFIER)
                    .build());

    public static final BlockEntityType<CrystallizerBlockEntity> CRYSTALLIZER = register("crystallizer",
            FabricBlockEntityTypeBuilder.create(CrystallizerBlockEntity::new, BlockInit.CRYSTALLIZER)
                    .build());

    public static final BlockEntityType<RotaryKilnControllerBlockEntity> ROTARY_KILN_CONTROLLER = register("rotary_kiln_controller",
            FabricBlockEntityTypeBuilder.create(RotaryKilnControllerBlockEntity::new, BlockInit.ROTARY_KILN_CONTROLLER)
                    .build());

    public static final BlockEntityType<RotaryKilnBlockEntity> ROTARY_KILN = register("rotary_kiln",
            FabricBlockEntityTypeBuilder.create(RotaryKilnBlockEntity::new, BlockInit.ROTARY_KILN)
                    .build());

    public static final BlockEntityType<ElectrolyzerBlockEntity> ELECTROLYZER = register("electrolyzer",
            FabricBlockEntityTypeBuilder.create(ElectrolyzerBlockEntity::new, BlockInit.ELECTROLYZER)
                    .build());

    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Industria.id(name), type);
    }

    public static void init() {
    }
}
