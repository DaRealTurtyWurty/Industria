package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.multiblocklib.MultiblockLib;
import dev.turtywurty.multiblocklib.world.MultiblockWorldData;
import dev.turtywurty.turtymultiloader.transfer.TransferService;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ModFluidStorageProviders {
    private ModFluidStorageProviders() {
    }

    public static void init() {
        TransferService transfers = TransferService.get();
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.THERMAL_GENERATOR, ThermalGeneratorBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.INDUCTION_HEATER, InductionHeaterBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.FLUID_PUMP, FluidPumpBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.MIXER, MixerBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.DIGESTER, DigesterBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.CLARIFIER, ClarifierBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.CRYSTALLIZER, CrystallizerBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.ELECTROLYZER, ElectrolyzerBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.FLUID_TANK, FluidTankBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.WELLHEAD, WellheadBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.SHAKING_TABLE, ShakingTableBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.CENTRIFUGAL_CONCENTRATOR, CentrifugalConcentratorBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.ARC_FURNACE, ArcFurnaceBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.TREE_TAP, TreeTapBlockEntity::getFluidProvider);
        transfers.registerBlockEntityProvider(StorageKeys.FLUID, ModBlockEntityTypes.AGITATOR, AgitatorBlockEntity::getFluidProvider);

        transfers.registerBlockProvider(StorageKeys.FLUID, (level, pos, state, blockEntity, side) -> {
            IndustriaMultiblockControllerBlockEntity controller = resolveMultiblockController(level instanceof ServerLevel serverLevel ? serverLevel : null, pos, blockEntity);
            return controller != null ? controller.getFluidStorageForExternal(pos, side) : null;
        }, MultiblockLib.MULTIBLOCK_PART_HANDLE);
        transfers.registerBlockProvider(StorageKeys.FLUID, (world, pos, state, blockEntity, context) -> {
            if (world instanceof ServerLevel serverWorld)
                return WorldPipeNetworks.getOrCreate(serverWorld).getStorage(TransferType.FLUID, pos);

            return null;
        }, ModBlocks.FLUID_PIPE);
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
