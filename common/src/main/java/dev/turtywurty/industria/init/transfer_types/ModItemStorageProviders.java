package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.conveyor.block.impl.entity.FeederConveyorBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import dev.turtywurty.multiblocklib.MultiblockLib;
import dev.turtywurty.multiblocklib.world.MultiblockWorldData;
import dev.turtywurty.turtymultiloader.transfer.TransferService;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ModItemStorageProviders {
    private ModItemStorageProviders() {
    }

    public static void init() {
        TransferService transfers = TransferService.get();
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.ALLOY_FURNACE, AlloyFurnaceBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.THERMAL_GENERATOR, ThermalGeneratorBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.BATTERY, BatteryBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.COMBUSTION_GENERATOR, CombustionGeneratorBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.CRUSHER, CrusherBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.ELECTRIC_FURNACE, ElectricFurnaceBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.MIXER, MixerBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.CLARIFIER, ClarifierBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.CRYSTALLIZER, CrystallizerBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.ROTARY_KILN_CONTROLLER, RotaryKilnControllerBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.ELECTROLYZER, ElectrolyzerBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.DRILL, DrillBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.SHAKING_TABLE, ShakingTableBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.CENTRIFUGAL_CONCENTRATOR, CentrifugalConcentratorBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.ARC_FURNACE, ArcFurnaceBlockEntity::getInventoryProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ITEM, ModBlockEntityTypes.AGITATOR, AgitatorBlockEntity::getInventoryProvider);
        transfers.registerBlockProvider(StorageKeys.ITEM, (level, pos, state, blockEntity, side) -> {
            IndustriaMultiblockControllerBlockEntity controller = resolveMultiblockController(level instanceof ServerLevel serverLevel ? serverLevel : null, pos, blockEntity);
            return controller != null ? controller.getItemStorageForExternal(pos, side) : null;
        }, MultiblockLib.MULTIBLOCK_PART_HANDLE);
        transfers.registerBlockProvider(StorageKeys.ITEM, (level, pos, _, _, _) -> {
                    if (level instanceof ServerLevel serverLevel)
                        return LevelConveyorNetworks.getOrCreate(serverLevel).getStorage(serverLevel, pos);

                    return null;
                }, ModBlocks.CONVEYOR, ModBlocks.SPLITTER_CONVEYOR, ModBlocks.MERGER_CONVEYOR, ModBlocks.ALTERNATOR_CONVEYOR, ModBlocks.HATCH_CONVEYOR,
                ModBlocks.SIDE_INJECTOR_CONVEYOR, ModBlocks.LADDER_CONVEYOR, ModBlocks.FILTER_CONVEYOR,
                ModBlocks.MAGNETIC_CONVEYOR, ModBlocks.DROP_CHUTE_CONVEYOR, ModBlocks.DETECTOR_CONVEYOR,
                ModBlocks.COUNT_CONVEYOR, ModBlocks.DELAY_CONVEYOR, ModBlocks.CONTAINMENT_CONVEYOR);

        transfers.registerBlockProvider(StorageKeys.ITEM, (level, _, _, blockEntity, side) -> {
            if (level instanceof ServerLevel && blockEntity instanceof FeederConveyorBlockEntity feeder)
                return feeder.getItemStorage(side);

            return null;
        }, ModBlocks.FEEDER_CONVEYOR);
    }

    private static IndustriaMultiblockControllerBlockEntity resolveMultiblockController(ServerLevel level, BlockPos pos, BlockEntity blockEntity) {
        if (blockEntity instanceof IndustriaMultiblockControllerBlockEntity controller)
            return controller;

        if (level == null)
            return null;

        BlockPos controllerPos = MultiblockWorldData.get(level).getControllerFor(pos);
        if (controllerPos == null)
            return null;

        BlockEntity controllerEntity = level.getBlockEntity(controllerPos);
        return controllerEntity instanceof IndustriaMultiblockControllerBlockEntity controller ? controller : null;
    }
}
