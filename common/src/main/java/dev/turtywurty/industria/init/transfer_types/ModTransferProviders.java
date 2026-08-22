package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.turtymultiloader.transfer.TransferService;

public final class ModTransferProviders {
    private ModTransferProviders() {
    }

    public static void init() {
        ModItemStorageProviders.init();
        ModEnergyStorageProviders.init();
        ModFluidStorageProviders.init();
        ModSlurryStorageProviders.init();
        ModGasStorageProviders.init();
        TransferService.get().apply();
    }
}
