package dev.turtywurty.industria.pipe.impl;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

import java.util.UUID;

public class SlurryPipeNetwork extends PipeNetwork<Storage<SlurryVariant>> {
    public SlurryPipeNetwork(UUID id) {
        super(id, TransferType.SLURRY);
    }

    @Override
    protected Storage<SlurryVariant> createStorage() {
        return new SingleSlurryStorage() {
            @Override
            protected long getCapacity(SlurryVariant variant) {
                return SlurryPipeNetwork.this.pipes.size() * FluidConstants.BUCKET;
            }
        };
    }

    @Override
    public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = new NbtCompound();
        nbt.put("networkData", super.writeNbt(registryLookup));

        var storage = new NbtCompound();
        ((SingleSlurryStorage) this.storage).writeNbt(storage, registryLookup);
        nbt.put("storage", storage);

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt.getCompound("networkData"), registryLookup);
        ((SingleSlurryStorage) this.storage).readNbt(nbt.getCompound("storage"), registryLookup);
    }
}
