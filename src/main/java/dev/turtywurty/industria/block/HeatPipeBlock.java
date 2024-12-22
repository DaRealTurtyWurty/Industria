package dev.turtywurty.industria.block;

import dev.turtywurty.heatapi.api.unit.HeatUnit;
import dev.turtywurty.industria.blockentity.HeatPipeBlockEntity;
import dev.turtywurty.industria.blockentity.util.heat.FluidHeatStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.Direction;

public class HeatPipeBlock extends PipeBlock<HeatPipeBlockEntity> {
    public HeatPipeBlock(Settings settings) {
        super(settings, HeatPipeBlockEntity.class, 6);
    }

    @Override
    protected BlockApiLookup<?, Direction> getStorageLookup() {
        return FluidHeatStorage.SIDED;
    }

    @Override
    protected BlockEntityType<HeatPipeBlockEntity> getBlockEntityType() {
        return BlockEntityTypeInit.HEAT_PIPE;
    }

    @Override
    protected long getAmount(HeatPipeBlockEntity blockEntity) {
        return blockEntity.getHeatStorage().getAmount();
    }

    @Override
    protected long getCapacity(HeatPipeBlockEntity blockEntity) {
        return blockEntity.getHeatStorage().getCapacity();
    }

    @Override
    protected String getUnit() {
        return HeatUnit.CELSIUS.getUnitSymbol();
    }
}
