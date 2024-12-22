package dev.turtywurty.industria.blockentity.util.heat;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

public class WrappedHeatStorage<T extends HeatStorage> extends WrappedStorage<T> {
    @Override
    public NbtList writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var list = new NbtList();
        for (HeatStorage storage : this.storages) {
            var nbt = new NbtCompound();
            nbt.putLong("Amount", storage.getAmount());
            list.add(nbt);
        }

        return list;
    }

    @Override
    public void readNbt(NbtList nbt, RegistryWrapper.WrapperLookup registryLookup) {
        for (int index = 0; index < nbt.size(); index++) {
            NbtCompound compound = nbt.getCompound(index);
            HeatStorage storage = this.storages.get(index);
            long amount = compound.getLong("Amount");
            if (storage instanceof SimpleHeatStorage simpleHeatStorage) {
                simpleHeatStorage.amount = amount;
            } else {
                try(Transaction transaction = Transaction.openOuter()) {
                    long current = storage.getAmount();
                    if (current < amount) {
                        storage.insert(amount - current, transaction);
                    } else if (current > amount) {
                        storage.extract(current - amount, transaction);
                    }

                    transaction.commit();
                }
            }
        }
    }
}
