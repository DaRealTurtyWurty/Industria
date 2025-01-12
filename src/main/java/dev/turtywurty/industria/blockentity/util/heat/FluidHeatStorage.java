package dev.turtywurty.industria.blockentity.util.heat;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.util.NBTSerializable;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FluidHeatStorage implements NBTSerializable<NbtCompound> {
    public static final BlockApiLookup<FluidHeatStorage, @Nullable Direction> SIDED =
            BlockApiLookup.get(Industria.id("sided_fluid_heat"), FluidHeatStorage.class, Direction.class);

    protected final HeatStorage heatStorage;
    protected final SingleVariantStorage<FluidVariant> fluidStorage;

    public FluidHeatStorage(@NotNull HeatStorage heatStorage, @NotNull SingleVariantStorage<FluidVariant> fluidStorage) {
        Objects.requireNonNull(heatStorage);
        Objects.requireNonNull(fluidStorage);

        this.heatStorage = heatStorage;
        this.fluidStorage = fluidStorage;
    }

    public HeatStorage heatStorage() {
        return this.heatStorage;
    }

    public SingleVariantStorage<FluidVariant> fluidStorage() {
        return this.fluidStorage;
    }

    @Override
    public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = new NbtCompound();

        var heatNbt = new NbtCompound();
        heatNbt.putLong("Heat", this.heatStorage.getAmount());
        nbt.put("HeatStorage", heatNbt);

        var fluidNbt = new NbtCompound();
        SingleVariantStorage.writeNbt(this.fluidStorage, FluidVariant.CODEC, fluidNbt, registryLookup);
        nbt.put("FluidStorage", fluidNbt);

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        var heatNbt = nbt.getCompound("HeatStorage");
        if(heatStorage instanceof SimpleHeatStorage simpleHeatStorage) {
            simpleHeatStorage.setAmount(heatNbt.getLong("Heat"));
        } else {
            long amount = heatNbt.getLong("Heat");

            try(Transaction transaction = Transaction.openOuter()) {
                long current = heatStorage.getAmount();
                if (current < amount) {
                    heatStorage.insert(amount - current, transaction);
                } else if (current > amount) {
                    heatStorage.extract(current - amount, transaction);
                }

                transaction.commit();
            }
        }

        var fluidNbt = nbt.getCompound("FluidStorage");
        SingleVariantStorage.readNbt(this.fluidStorage, FluidVariant.CODEC, FluidVariant::blank, fluidNbt, registryLookup);
    }
}
