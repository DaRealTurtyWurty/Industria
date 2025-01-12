package dev.turtywurty.industria.blockentity.util.heat;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import org.jetbrains.annotations.NotNull;

public class SyncingFluidHeatStorage<H extends HeatStorage & SyncableStorage,
        F extends SingleVariantStorage<FluidVariant> & SyncableStorage> extends FluidHeatStorage implements SyncableStorage {
    protected final H heatStorage;
    protected final F fluidStorage;

    public SyncingFluidHeatStorage(@NotNull H heatStorage, @NotNull F fluidStorage) {
        super(heatStorage, fluidStorage);
        this.heatStorage = heatStorage;
        this.fluidStorage = fluidStorage;
    }

    @Override
    public void sync() {
        this.heatStorage.sync();
        this.fluidStorage.sync();
    }
}
