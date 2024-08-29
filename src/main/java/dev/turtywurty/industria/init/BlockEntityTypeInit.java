package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.blockentity.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BlockEntityTypeInit {
    public static final BlockEntityType<AlloyFurnaceBlockEntity> ALLOY_FURNACE = register("alloy_furnace",
            BlockEntityType.Builder.create(AlloyFurnaceBlockEntity::new, BlockInit.ALLOY_FURNACE)
                    .build());

    public static final BlockEntityType<ThermalGeneratorBlockEntity> THERMAL_GENERATOR = register("thermal_generator",
            BlockEntityType.Builder.create(ThermalGeneratorBlockEntity::new, BlockInit.THERMAL_GENERATOR)
                    .build());

    public static final BlockEntityType<BatteryBlockEntity> BATTERY = register("battery",
            BlockEntityType.Builder.create(
                            (pos, state) -> new BatteryBlockEntity(pos, state, ((BatteryBlock) state.getBlock()).getLevel()),
                            BlockInit.BASIC_BATTERY, BlockInit.ADVANCED_BATTERY, BlockInit.ELITE_BATTERY,
                            BlockInit.ULTIMATE_BATTERY, BlockInit.CREATIVE_BATTERY)
                    .build());

    public static final BlockEntityType<CombustionGeneratorBlockEntity> COMBUSTION_GENERATOR = register("combustion_generator",
            BlockEntityType.Builder.create(CombustionGeneratorBlockEntity::new, BlockInit.COMBUSTION_GENERATOR)
                    .build());

    public static final BlockEntityType<SolarPanelBlockEntity> SOLAR_PANEL = register("solar_panel",
            BlockEntityType.Builder.create(SolarPanelBlockEntity::new, BlockInit.SOLAR_PANEL)
                    .build());

    public static final BlockEntityType<CrusherBlockEntity> CRUSHER = register("crusher",
            BlockEntityType.Builder.create(CrusherBlockEntity::new, BlockInit.CRUSHER)
                    .build());

    public static final BlockEntityType<CableBlockEntity> CABLE = register("cable",
            BlockEntityType.Builder.create(CableBlockEntity::new, BlockInit.CABLE)
                    .build());

    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Industria.id(name), type);
    }

    public static void init() {
    }
}
