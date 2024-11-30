package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.blockentity.*;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
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
                            BlockInit.BATTERIES.toArray(new Block[0]))
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

    public static final BlockEntityType<CableBlockEntity> CABLE = register("cable",
            FabricBlockEntityTypeBuilder.create(CableBlockEntity::new, BlockInit.CABLE)
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

    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Industria.id(name), type);
    }

    public static void init() {
    }
}
