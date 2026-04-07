package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.blockentity.ElectrolyzerBlockEntity;
import dev.turtywurty.industria.blockentity.FractionalDistillationControllerBlockEntity;
import dev.turtywurty.industria.blockentity.IndustriaMultiblockControllerBlockEntity;
import dev.turtywurty.industria.blockentity.InductionHeaterBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.multiblocklib.MultiblockLib;
import dev.turtywurty.multiblocklib.world.MultiblockWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class HeatStorageInit {
    public static void init() {
        HeatStorage.SIDED.registerForBlockEntity(FractionalDistillationControllerBlockEntity::getHeatProvider, BlockEntityTypeInit.FRACTIONAL_DISTILLATION_CONTROLLER);
        HeatStorage.SIDED.registerForBlockEntity(InductionHeaterBlockEntity::getHeatProvider, BlockEntityTypeInit.INDUCTION_HEATER);
        HeatStorage.SIDED.registerForBlockEntity(ElectrolyzerBlockEntity::getHeatProvider, BlockEntityTypeInit.ELECTROLYZER);
        HeatStorage.SIDED.registerForBlocks((level, pos, state, blockEntity, side) -> {
            IndustriaMultiblockControllerBlockEntity controller = resolveMultiblockController(level instanceof ServerLevel serverLevel ? serverLevel : null, pos, blockEntity);
            return controller != null ? controller.getHeatStorageForExternal(pos, side) : null;
        }, MultiblockLib.MULTIBLOCK_PART);

        HeatStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (world instanceof ServerLevel serverWorld)
                return WorldPipeNetworks.getOrCreate(serverWorld).getStorage(TransferType.HEAT, pos);

            return null;
        }, BlockInit.HEAT_PIPE);
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
