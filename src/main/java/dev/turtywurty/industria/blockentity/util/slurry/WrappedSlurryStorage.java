package dev.turtywurty.industria.blockentity.util.slurry;

import com.mojang.datafixers.util.Pair;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
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

public class WrappedSlurryStorage<T extends Storage<SlurryVariant>> extends WrappedStorage<T> {
    private final CombinedStorage<SlurryVariant, T> combinedStorage = new CombinedStorage<>(this.storages);

    public CombinedStorage<SlurryVariant, T> getCombinedStorage() {
        return this.combinedStorage;
    }

    public List<SlurryStack> getSlurries() {
        List<SlurryStack> slurries = new ArrayList<>();
        for (T tank : this.storages) {
            for (StorageView<SlurryVariant> view : tank.nonEmptyViews()) {
                slurries.add(new SlurryStack(view.getResource(), view.getAmount()));
            }
        }

        return slurries;
    }

    @Override
    public NbtList writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var list = new NbtList();
        for (T tank : this.storages) {
            for (StorageView<SlurryVariant> view : tank.nonEmptyViews()) {
                var nbt = new NbtCompound();
                nbt.putLong("Amount", view.getAmount());
                nbt.put("Slurry", SlurryVariant.CODEC.encode(view.getResource(), NbtOps.INSTANCE, new NbtCompound()).getOrThrow());
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

            if(storage instanceof SingleSlurryStorage singleSlurryStorage) {
                singleSlurryStorage.amount = compound.getLong("Amount");
                singleSlurryStorage.variant = SlurryVariant.CODEC.decode(NbtOps.INSTANCE, compound.get("Slurry"))
                        .map(Pair::getFirst)
                        .getOrThrow();
            } else {
                throw new UnsupportedOperationException("Cannot read slurry storage of type: " + storage.getClass().getName());
            }
        }
    }
}
