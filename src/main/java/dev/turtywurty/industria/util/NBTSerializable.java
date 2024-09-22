package dev.turtywurty.industria.util;

import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;

/**
 * An interface that allows for easy serialization and deserialization of objects to NBT.
 *
 * @param <T> The type of NBT element that will be used to serialize and deserialize the object.
 *
 *            <br>
 *            <h4>Example usage:
 *            <pre>{@code
 *                      public class Example implements NBTSerializable<NbtCompound> {
 *                         private int exampleInt;
 *                         private String exampleString;
 *
 *                         public Example(int exampleInt, String exampleString) {
 *                             this.exampleInt = exampleInt;
 *                             this.exampleString = exampleString;
 *                         }
 *
 *                         @Override
 *                         public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
 *                             NbtCompound nbt = new NbtCompound();
 *                             nbt.putInt("exampleInt", this.exampleInt);
 *                             nbt.putString("exampleString", this.exampleString);
 *                             return nbt;
 *                         }
 *
 *                         @Override
 *                         public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
 *                             this.exampleInt = nbt.getInt("exampleInt");
 *                             this.exampleString = nbt.getString("exampleString");
 *                         }
 *                      }
 *                  }</pre>
 */
public interface NBTSerializable<T extends NbtElement> {
    /**
     * Writes the object to an NBT element.
     *
     * @param registryLookup The registry lookup to use when serializing the object.
     * @return The NBT element that represents the object.
     */
    T writeNbt(RegistryWrapper.WrapperLookup registryLookup);

    /**
     * Reads the object from an NBT element.
     *
     * @param nbt The NBT element to read the object from.
     * @param registryLookup The registry lookup to use when deserializing the object.
     */
    void readNbt(T nbt, RegistryWrapper.WrapperLookup registryLookup);
}
