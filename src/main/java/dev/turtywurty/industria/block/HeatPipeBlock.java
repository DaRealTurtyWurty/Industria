package dev.turtywurty.industria.block;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.unit.HeatUnit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;

public class HeatPipeBlock extends PipeBlock<HeatStorage> {
    public HeatPipeBlock(Settings settings) {
        super(settings, 6, TransferType.HEAT, PipeNetworkManager.HEAT);
    }

    @Override
    protected long getAmount(HeatStorage storage) {
        return storage.getAmount();
    }

    @Override
    protected long getCapacity(HeatStorage storage) {
        return storage.getCapacity();
    }

    @Override
    protected String getUnit() {
        return HeatUnit.CELSIUS.getUnitSymbol();
    }
}
