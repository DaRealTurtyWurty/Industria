package dev.turtywurty.industria.pipe.impl;

import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

import java.util.UUID;

public class FluidPipeNetwork extends PipeNetwork<Storage<FluidVariant>> {
    public FluidPipeNetwork(UUID id) {
        super(id, TransferType.FLUID);
    }

    @Override
    protected Storage<FluidVariant> createStorage() {
        return new SingleFluidStorage() {
            @Override
            protected long getCapacity(FluidVariant variant) {
                return FluidPipeNetwork.this.pipes.size() * FluidConstants.BUCKET;
            }
        };
    }

    @Override
    public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = new NbtCompound();
        nbt.put("networkData", super.writeNbt(registryLookup));

        var storage = new NbtCompound();
        ((SingleFluidStorage) this.storage).writeNbt(storage, registryLookup);
        nbt.put("storage", storage);

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt.getCompound("networkData"), registryLookup);
        ((SingleFluidStorage) this.storage).readNbt(nbt.getCompound("storage"), registryLookup);
    }
}
