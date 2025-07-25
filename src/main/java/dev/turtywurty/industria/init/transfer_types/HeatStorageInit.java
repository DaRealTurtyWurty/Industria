package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.blockentity.ElectrolyzerBlockEntity;
import dev.turtywurty.industria.blockentity.FractionalDistillationControllerBlockEntity;
import dev.turtywurty.industria.blockentity.InductionHeaterBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import net.minecraft.server.world.ServerWorld;

public class HeatStorageInit {
    public static void init() {
        HeatStorage.SIDED.registerForBlockEntity(FractionalDistillationControllerBlockEntity::getHeatProvider, BlockEntityTypeInit.FRACTIONAL_DISTILLATION_CONTROLLER);
        HeatStorage.SIDED.registerForBlockEntity(InductionHeaterBlockEntity::getHeatProvider, BlockEntityTypeInit.INDUCTION_HEATER);
        HeatStorage.SIDED.registerForBlockEntity(ElectrolyzerBlockEntity::getHeatProvider, BlockEntityTypeInit.ELECTROLYZER);

        HeatStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (world instanceof ServerWorld serverWorld) {
                return WorldPipeNetworks.getOrCreate(serverWorld).getStorage(TransferType.HEAT, pos);
            }

            return null;
        }, BlockInit.HEAT_PIPE);
    }
}
