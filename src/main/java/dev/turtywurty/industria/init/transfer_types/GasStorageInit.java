package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.gasapi.api.storage.GasStorage;
import dev.turtywurty.industria.blockentity.ArcFurnaceBlockEntity;
import dev.turtywurty.industria.blockentity.ElectrolyzerBlockEntity;
import dev.turtywurty.industria.blockentity.IndustriaMultiblockControllerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.multiblocklib.MultiblockLib;
import dev.turtywurty.multiblocklib.world.MultiblockWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GasStorageInit {
    public static void init() {
        GasStorage.SIDED.registerForBlockEntity(ElectrolyzerBlockEntity::getGasProvider, BlockEntityTypeInit.ELECTROLYZER);
        GasStorage.SIDED.registerForBlockEntity(ArcFurnaceBlockEntity::getGasProvider, BlockEntityTypeInit.ARC_FURNACE);
        GasStorage.SIDED.registerForBlocks((level, pos, _, blockEntity, side) -> {
            IndustriaMultiblockControllerBlockEntity controller = resolveMultiblockController(level instanceof ServerLevel serverLevel ? serverLevel : null, pos, blockEntity);
            return controller != null ? controller.getGasStorageForExternal(pos, side) : null;
        }, MultiblockLib.MULTIBLOCK_PART);
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
