package dev.turtywurty.industria.blockentity.util.heat;

import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

public class WrappedFluidHeatStorage extends WrappedStorage<FluidHeatStorage> {
    @Override
    public NbtList writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = new NbtList();
        for (FluidHeatStorage storage : this.storages) {
            nbt.add(storage.writeNbt(registryLookup));
        }

        return nbt;
    }

    @Override
    public void readNbt(NbtList nbt, RegistryWrapper.WrapperLookup registryLookup) {
        for (int index = 0; index < nbt.size(); index++) {
            this.storages.get(index).readNbt(nbt.getCompound(index), registryLookup);
        }
    }
}
