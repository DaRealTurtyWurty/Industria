package dev.turtywurty.industria.block;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.unit.HeatUnit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.impl.HeatPipeNetwork;

public class HeatPipeBlock extends PipeBlock<HeatStorage, HeatPipeNetwork, Double> {
    public HeatPipeBlock(Settings settings) {
        super(settings, 6, TransferType.HEAT);
    }

    @Override
    public Double getAmount(HeatStorage storage) {
        return 23 + storage.getAmount();
    }

    @Override
    public Double getCapacity(HeatStorage storage) {
        return storage.getCapacity();
    }

    @Override
    public String getUnit() {
        return HeatUnit.CELSIUS.getUnitSymbol();
    }
}
