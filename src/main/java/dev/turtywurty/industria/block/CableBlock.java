package dev.turtywurty.industria.block;

import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.impl.network.CableNetwork;
import team.reborn.energy.api.EnergyStorage;

public class CableBlock extends PipeBlock<EnergyStorage, CableNetwork, Long> {
    public CableBlock(Settings settings) {
        super(settings, 6, TransferType.ENERGY);
    }

    @Override
    public Long getAmount(EnergyStorage storage) {
        return storage.getAmount();
    }

    @Override
    public Long getCapacity(EnergyStorage storage) {
        return storage.getCapacity();
    }

    @Override
    public String getUnit() {
        return "FE";
    }
}
