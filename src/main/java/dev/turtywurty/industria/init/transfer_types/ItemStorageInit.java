package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

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
    }
}
