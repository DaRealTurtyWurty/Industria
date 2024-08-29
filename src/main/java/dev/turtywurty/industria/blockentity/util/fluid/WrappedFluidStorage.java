package dev.turtywurty.industria.blockentity.util.fluid;

import com.mojang.datafixers.util.Pair;
import dev.turtywurty.industria.util.NBTSerializable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WrappedFluidStorage<T extends SingleFluidStorage> implements NBTSerializable<NbtList> {
    private final List<T> tanks = new ArrayList<>();
    private final Map<Direction, T> sidedTankMap = new HashMap<>();
    private final CombinedStorage<FluidVariant, T> combinedStorage = new CombinedStorage<>(this.tanks);

    public void addTank(T tank) {
        addTank(tank, null);
    }

    public void addTank(T tank, Direction side) {
        this.tanks.add(tank);

        if (side == null) {
            for (Direction direction : Direction.values()) {
                this.sidedTankMap.put(direction, tank);
            }
        } else {
            this.sidedTankMap.put(side, tank);
        }
    }

    public List<T> getTanks() {
        return tanks;
    }

    public Map<Direction, T> getSidedTankMap() {
        return sidedTankMap;
    }

    public T getStorage(Direction side) {
        if (side == null)
            return this.tanks.getFirst();

        return this.sidedTankMap.get(side);
    }

    public T getStorage(int index) {
        return this.tanks.get(index);
    }

    public List<FluidStack> getFluids() {
        List<FluidStack> fluids = new ArrayList<>();
        for (T tank : this.tanks) {
            fluids.add(new FluidStack(tank.getResource(), tank.getAmount()));
        }

        return fluids;
    }

    @Override
    public NbtList writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var list = new NbtList();
        for (T tank : this.tanks) {
            var nbt = new NbtCompound();
            nbt.putLong("Amount", tank.getAmount());
            nbt.put("Fluid", FluidVariant.CODEC.encode(tank.getResource(), NbtOps.INSTANCE, new NbtCompound()).getOrThrow());
            list.add(nbt);
        }

        return list;
    }

    @Override
    public void readNbt(NbtList nbt, RegistryWrapper.WrapperLookup registryLookup) {
        for (int index = 0; index < nbt.size(); index++) {
            var compound = nbt.getCompound(index);
            this.tanks.get(index).amount = compound.getLong("Amount");
            this.tanks.get(index).variant = FluidVariant.CODEC.decode(NbtOps.INSTANCE, compound.get("Fluid"))
                    .map(Pair::getFirst)
                    .getOrThrow();
        }
    }
}
