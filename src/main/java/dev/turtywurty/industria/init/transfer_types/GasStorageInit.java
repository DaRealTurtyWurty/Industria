package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.gasapi.api.storage.GasStorage;
import dev.turtywurty.industria.blockentity.ElectrolyzerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;

public class GasStorageInit {
    public static void init() {
        GasStorage.SIDED.registerForBlockEntity(ElectrolyzerBlockEntity::getGasProvider, BlockEntityTypeInit.ELECTROLYZER);
    }
}
