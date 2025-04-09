package dev.turtywurty.industria.blockentity.util.gas;

import com.mojang.datafixers.util.Pair;
import dev.turtywurty.gasapi.api.GasVariant;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;

import java.util.ArrayList;
import java.util.List;

public class WrappedGasStorage<T extends Storage<GasVariant>> extends WrappedStorage<T> {
    private final CombinedStorage<GasVariant, T> combinedStorage = new CombinedStorage<>(this.storages);

    public CombinedStorage<GasVariant, T> getCombinedStorage() {
        return this.combinedStorage;
    }

    public List<GasStack> getSlurries() {
        List<GasStack> slurries = new ArrayList<>();
        for (T tank : this.storages) {
            for (StorageView<GasVariant> view : tank.nonEmptyViews()) {
                slurries.add(new GasStack(view.getResource(), view.getAmount()));
            }
        }

        return slurries;
    }

    @Override
    public NbtList writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var list = new NbtList();
        for (T tank : this.storages) {
            if(tank instanceof SingleGasStorage singleGasStorage) {
                var nbt = new NbtCompound();
                nbt.putLong("Amount", singleGasStorage.getAmount());
                nbt.put("Gas", GasVariant.CODEC.encode(singleGasStorage.getResource(), NbtOps.INSTANCE, new NbtCompound()).getOrThrow());
                list.add(nbt);
            }
        }

        return list;
    }

    @Override
    public void readNbt(NbtList nbt, RegistryWrapper.WrapperLookup registryLookup) {
        for (int index = 0; index < nbt.size(); index++) {
            var compound = nbt.getCompound(index);
            T storage = this.storages.get(index);
            if (storage == null)
                continue;

            if(storage instanceof SingleGasStorage singleGasStorage) {
                singleGasStorage.amount = compound.getLong("Amount");
                singleGasStorage.variant = GasVariant.CODEC.decode(NbtOps.INSTANCE, compound.get("Gas"))
                        .map(Pair::getFirst)
                        .getOrThrow();
            } else {
                throw new UnsupportedOperationException("Cannot read gas storage of type: " + storage.getClass().getName());
            }
        }
    }
}
