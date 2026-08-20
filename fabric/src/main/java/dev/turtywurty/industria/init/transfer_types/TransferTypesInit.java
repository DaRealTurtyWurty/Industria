package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.industria.multiblock.TransferType;

public class TransferTypesInit {
    public static void init() {
        ItemStorageInit.init();
        EnergyStorageInit.init();
        FluidStorageInit.init();
        HeatStorageInit.init();
        SlurryStorageInit.init();
        GasStorageInit.init();

        for (TransferType<?, ?, ?> transferType : TransferType.getValues()) {
            transferType.registerForMultiblockIo();
        }
    }
}
