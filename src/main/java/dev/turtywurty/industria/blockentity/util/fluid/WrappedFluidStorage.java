package dev.turtywurty.industria.blockentity.util.fluid;

import com.mojang.datafixers.util.Pair;
import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;

import java.util.ArrayList;
import java.util.List;

public class WrappedFluidStorage<T extends Storage<FluidVariant>> extends WrappedStorage<T> {
    private final CombinedStorage<FluidVariant, T> combinedStorage = new CombinedStorage<>(this.storages);

    public CombinedStorage<FluidVariant, T> getCombinedStorage() {
        return this.combinedStorage;
    }

    public List<FluidStack> getFluids() {
        List<FluidStack> fluids = new ArrayList<>();
        for (T tank : this.storages) {
            for (StorageView<FluidVariant> view : tank.nonEmptyViews()) {
                fluids.add(new FluidStack(view.getResource(), view.getAmount()));
            }
        }

        return fluids;
    }

    @Override
    public NbtList writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var list = new NbtList();
        for (T tank : this.storages) {
            if (tank instanceof SingleFluidStorage singleFluidStorage) {
                var nbt = new NbtCompound();
                nbt.putLong("Amount", singleFluidStorage.getAmount());
                nbt.put("Fluid", FluidVariant.CODEC.encode(singleFluidStorage.getResource(), NbtOps.INSTANCE, new NbtCompound()).getOrThrow());
                list.add(nbt);
            }
        }

        return list;
    }

    @Override
    public void readNbt(NbtList nbt, RegistryWrapper.WrapperLookup registryLookup) {
        for (int index = 0; index < nbt.size(); index++) {
            var compound = nbt.getCompoundOrEmpty(index);
            T storage = this.storages.get(index);
            if (storage == null)
                continue;

            if(storage instanceof SingleFluidStorage singleFluidStorage) {
                singleFluidStorage.amount = compound.getLong("Amount", 0L);
                singleFluidStorage.variant = FluidVariant.CODEC.decode(NbtOps.INSTANCE, compound.get("Fluid"))
                        .map(Pair::getFirst)
                        .getOrThrow();
            } else {
                throw new UnsupportedOperationException("Cannot read fluid storage of type: " + storage.getClass().getName());
            }
        }
    }
}
