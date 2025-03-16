package dev.turtywurty.industria.pipe.impl;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

import java.util.UUID;

public class HeatPipeNetwork extends PipeNetwork<HeatStorage> {
    public HeatPipeNetwork(UUID id) {
        super(id, TransferType.HEAT);
    }

    @Override
    protected HeatStorage createStorage() {
        return new SimpleHeatStorage(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
    }

    @Override
    public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = new NbtCompound();
        nbt.put("networkData", super.writeNbt(registryLookup));
        nbt.putLong("amount", this.storage.getAmount());
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt.getCompound("networkData"), registryLookup);
        ((SimpleHeatStorage) this.storage).amount = nbt.getLong("amount");
    }
}
