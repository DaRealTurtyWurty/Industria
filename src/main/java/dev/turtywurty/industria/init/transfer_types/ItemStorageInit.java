package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.conveyor.block.impl.entity.FeederConveyorBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.server.level.ServerLevel;

public class ItemStorageInit {
    public static void init() {
        ItemStorage.SIDED.registerForBlockEntity(AlloyFurnaceBlockEntity::getInventoryProvider, BlockEntityTypeInit.ALLOY_FURNACE);
        ItemStorage.SIDED.registerForBlockEntity(ThermalGeneratorBlockEntity::getInventoryProvider, BlockEntityTypeInit.THERMAL_GENERATOR);
        ItemStorage.SIDED.registerForBlockEntity(BatteryBlockEntity::getInventoryProvider, BlockEntityTypeInit.BATTERY);
        ItemStorage.SIDED.registerForBlockEntity(CombustionGeneratorBlockEntity::getInventoryProvider, BlockEntityTypeInit.COMBUSTION_GENERATOR);
        ItemStorage.SIDED.registerForBlockEntity(CrusherBlockEntity::getInventoryProvider, BlockEntityTypeInit.CRUSHER);
        ItemStorage.SIDED.registerForBlockEntity(ElectricFurnaceBlockEntity::getInventoryProvider, BlockEntityTypeInit.ELECTRIC_FURNACE);
        ItemStorage.SIDED.registerForBlockEntity(MixerBlockEntity::getInventoryProvider, BlockEntityTypeInit.MIXER);
        ItemStorage.SIDED.registerForBlockEntity(ClarifierBlockEntity::getInventoryProvider, BlockEntityTypeInit.CLARIFIER);
        ItemStorage.SIDED.registerForBlockEntity(CrystallizerBlockEntity::getInventoryProvider, BlockEntityTypeInit.CRYSTALLIZER);
        ItemStorage.SIDED.registerForBlockEntity(RotaryKilnControllerBlockEntity::getInventoryProvider, BlockEntityTypeInit.ROTARY_KILN_CONTROLLER);
        ItemStorage.SIDED.registerForBlockEntity(ElectrolyzerBlockEntity::getInventoryProvider, BlockEntityTypeInit.ELECTROLYZER);
        ItemStorage.SIDED.registerForBlockEntity(DrillBlockEntity::getInventoryProvider, BlockEntityTypeInit.DRILL);
        ItemStorage.SIDED.registerForBlockEntity(ShakingTableBlockEntity::getInventoryProvider, BlockEntityTypeInit.SHAKING_TABLE);
        ItemStorage.SIDED.registerForBlockEntity(CentrifugalConcentratorBlockEntity::getInventoryProvider, BlockEntityTypeInit.CENTRIFUGAL_CONCENTRATOR);
        ItemStorage.SIDED.registerForBlockEntity(ArcFurnaceBlockEntity::getInventoryProvider, BlockEntityTypeInit.ARC_FURNACE);
        ItemStorage.SIDED.registerForBlocks((level, pos, _, _, _) -> {
                    if (level instanceof ServerLevel serverLevel)
                        return LevelConveyorNetworks.getOrCreate(serverLevel).getStorage(serverLevel, pos);

                    return null;
                }, BlockInit.CONVEYOR, BlockInit.SPLITTER_CONVEYOR, BlockInit.MERGER_CONVEYOR, BlockInit.HATCH_CONVEYOR,
                BlockInit.SIDE_INJECTOR_CONVEYOR, BlockInit.LADDER_CONVEYOR, BlockInit.FILTER_CONVEYOR);

        ItemStorage.SIDED.registerForBlocks((level, _, _, blockEntity, side) -> {
            if (level instanceof ServerLevel && blockEntity instanceof FeederConveyorBlockEntity feeder)
                return feeder.getItemStorage(side);

            return null;
        }, BlockInit.FEEDER_CONVEYOR);
    }
}
