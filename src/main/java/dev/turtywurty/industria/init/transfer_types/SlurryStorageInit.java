package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.fabricslurryapi.api.storage.SlurryStorage;
import dev.turtywurty.industria.blockentity.CentrifugalConcentratorBlockEntity;
import dev.turtywurty.industria.blockentity.DigesterBlockEntity;
import dev.turtywurty.industria.blockentity.IndustriaMultiblockControllerBlockEntity;
import dev.turtywurty.industria.blockentity.MixerBlockEntity;
import dev.turtywurty.industria.blockentity.ShakingTableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.multiblocklib.MultiblockLib;
import dev.turtywurty.multiblocklib.world.MultiblockWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SlurryStorageInit {
    public static void init() {
        SlurryStorage.SIDED.registerForBlockEntity(MixerBlockEntity::getSlurryProvider, BlockEntityTypeInit.MIXER);
        SlurryStorage.SIDED.registerForBlockEntity(DigesterBlockEntity::getSlurryProvider, BlockEntityTypeInit.DIGESTER);
        SlurryStorage.SIDED.registerForBlockEntity(ShakingTableBlockEntity::getSlurryProvider, BlockEntityTypeInit.SHAKING_TABLE);
        SlurryStorage.SIDED.registerForBlockEntity(CentrifugalConcentratorBlockEntity::getSlurryProvider, BlockEntityTypeInit.CENTRIFUGAL_CONCENTRATOR);
        SlurryStorage.SIDED.registerForBlocks((level, pos, state, blockEntity, side) -> {
            IndustriaMultiblockControllerBlockEntity controller = resolveMultiblockController(level instanceof ServerLevel serverLevel ? serverLevel : null, pos, blockEntity);
            return controller != null ? controller.getSlurryStorageForExternal(pos, side) : null;
        }, MultiblockLib.MULTIBLOCK_PART);

        SlurryStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (world instanceof ServerLevel serverWorld)
                return WorldPipeNetworks.getOrCreate(serverWorld).getStorage(TransferType.SLURRY, pos);

            return null;
        }, BlockInit.SLURRY_PIPE);
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
