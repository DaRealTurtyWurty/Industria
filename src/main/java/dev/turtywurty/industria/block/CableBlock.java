package dev.turtywurty.industria.block;

import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import team.reborn.energy.api.EnergyStorage;

public class CableBlock extends PipeBlock<EnergyStorage> {
    public CableBlock(Settings settings) {
        super(settings, 6, TransferType.ENERGY, PipeNetworkManager.ENERGY);
    }

    @Override
    protected long getAmount(EnergyStorage storage) {
        return storage.getAmount();
    }

    @Override
    protected long getCapacity(EnergyStorage storage) {
        return storage.getCapacity();
    }

    @Override
    protected String getUnit() {
        return "FE";
    }
}
