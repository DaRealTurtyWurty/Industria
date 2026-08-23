package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.gasapi.api.storage.GasStorage;
import dev.turtywurty.industria.blockentity.AgitatorBlockEntity;
import dev.turtywurty.industria.blockentity.ArcFurnaceBlockEntity;
import dev.turtywurty.industria.blockentity.ElectrolyzerBlockEntity;
import dev.turtywurty.industria.blockentity.IndustriaMultiblockControllerBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.multiblocklib.MultiblockLib;
import dev.turtywurty.multiblocklib.world.MultiblockWorldData;
import dev.turtywurty.turtymultiloader.transfer.TransferService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ModGasStorageProviders {
    private ModGasStorageProviders() {
    }

    public static void init() {
        TransferService transfers = TransferService.get();
        transfers.registerBlockEntityProvider(GasStorage.KEY, ModBlockEntityTypes.ELECTROLYZER, ElectrolyzerBlockEntity::getGasProvider);
        transfers.registerBlockEntityProvider(GasStorage.KEY, ModBlockEntityTypes.ARC_FURNACE, ArcFurnaceBlockEntity::getGasProvider);
        transfers.registerBlockEntityProvider(GasStorage.KEY, ModBlockEntityTypes.AGITATOR, AgitatorBlockEntity::getGasProvider);
        transfers.registerBlockProvider(GasStorage.KEY, (level, pos, _, blockEntity, side) -> {
            IndustriaMultiblockControllerBlockEntity controller = resolveMultiblockController(level instanceof ServerLevel serverLevel ? serverLevel : null, pos, blockEntity);
            return controller != null ? controller.getGasStorageForExternal(pos, side) : null;
        }, MultiblockLib.MULTIBLOCK_PART_HANDLE);
        transfers.registerBlockProvider(GasStorage.KEY, (world, pos, state, blockEntity, context) -> {
            if (world instanceof ServerLevel serverWorld)
                return WorldPipeNetworks.getOrCreate(serverWorld).getStorage(TransferType.GAS, pos);

            return null;
        }, ModBlocks.GAS_PIPE);
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
