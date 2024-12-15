package dev.turtywurty.industria.block;

import dev.turtywurty.industria.blockentity.CableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.Direction;
import team.reborn.energy.api.EnergyStorage;

public class CableBlock extends PipeBlock<CableBlockEntity> {
    public CableBlock(Settings settings) {
        super(settings, CableBlockEntity.class, 6);
    }

    @Override
    protected BlockApiLookup<?, Direction> getStorageLookup() {
        return EnergyStorage.SIDED;
    }

    @Override
    protected BlockEntityType<CableBlockEntity> getBlockEntityType() {
        return BlockEntityTypeInit.CABLE;
    }

    @Override
    protected long getAmount(CableBlockEntity blockEntity) {
        return blockEntity.getStorageProvider(null).getAmount();
    }

    @Override
    protected long getCapacity(CableBlockEntity blockEntity) {
        return blockEntity.getStorageProvider(null).getCapacity();
    }

    @Override
    protected String getUnit() {
        return "FE";
    }
}
