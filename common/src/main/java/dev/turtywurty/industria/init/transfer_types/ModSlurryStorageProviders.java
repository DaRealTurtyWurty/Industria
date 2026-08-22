package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.multiblocklib.MultiblockLib;
import dev.turtywurty.multiblocklib.world.MultiblockWorldData;
import dev.turtywurty.slurryapi.api.storage.SlurryStorage;
import dev.turtywurty.turtymultiloader.transfer.TransferService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ModSlurryStorageProviders {
    private ModSlurryStorageProviders() {
    }

    public static void init() {
        TransferService transfers = TransferService.get();
        transfers.registerBlockEntityProvider(SlurryStorage.KEY, ModBlockEntityTypes.MIXER, MixerBlockEntity::getSlurryProvider);
        transfers.registerBlockEntityProvider(SlurryStorage.KEY, ModBlockEntityTypes.DIGESTER, DigesterBlockEntity::getSlurryProvider);
        transfers.registerBlockEntityProvider(SlurryStorage.KEY, ModBlockEntityTypes.SHAKING_TABLE, ShakingTableBlockEntity::getSlurryProvider);
        transfers.registerBlockEntityProvider(SlurryStorage.KEY, ModBlockEntityTypes.CENTRIFUGAL_CONCENTRATOR, CentrifugalConcentratorBlockEntity::getSlurryProvider);
        transfers.registerBlockEntityProvider(SlurryStorage.KEY, ModBlockEntityTypes.AGITATOR, AgitatorBlockEntity::getSlurryProvider);
        transfers.registerBlockProvider(SlurryStorage.KEY, (level, pos, state, blockEntity, side) -> {
            IndustriaMultiblockControllerBlockEntity controller = resolveMultiblockController(level instanceof ServerLevel serverLevel ? serverLevel : null, pos, blockEntity);
            return controller != null ? controller.getSlurryStorageForExternal(pos, side) : null;
        }, MultiblockLib.MULTIBLOCK_PART_HANDLE);

        transfers.registerBlockProvider(SlurryStorage.KEY, (world, pos, state, blockEntity, context) -> {
            if (world instanceof ServerLevel serverWorld)
                return WorldPipeNetworks.getOrCreate(serverWorld).getStorage(TransferType.SLURRY, pos);

            return null;
        }, ModBlocks.SLURRY_PIPE);
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
