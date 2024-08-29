package dev.turtywurty.industria.util;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;

public interface NBTSerializable<T extends NbtElement> {
    T writeNbt(RegistryWrapper.WrapperLookup registryLookup);

    void readNbt(T nbt, RegistryWrapper.WrapperLookup registryLookup);
}
