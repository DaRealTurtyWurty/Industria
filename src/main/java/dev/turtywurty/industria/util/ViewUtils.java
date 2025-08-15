package dev.turtywurty.industria.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.dynamic.Codecs;

import java.util.Set;

public final class ViewUtils {
    private ViewUtils() {
    }

    /**
     * relative to {@link NbtCompound#getKeys()}, but in view form.
     * Helper method from neoforge.
     */
    public static Set<String> getKeys(ReadView view) {
        return view.read(MapCodec.assumeMapUnsafe(NbtCompound.CODEC)).orElseThrow().getKeys();
    }

    public static void put(WriteView view, NbtCompound compound) {
        compound.entrySet().forEach(entry -> view.put(entry.getKey(), Codecs.NBT_ELEMENT, entry.getValue()));
    }

    // from neo
    public static void putChild(WriteView view, String key, ViewSerializable child) {
        child.writeData(view.get(key));
    }

    // inspired by neo
    public static void readChild(ReadView view, String key, ViewSerializable child) {
        child.readData(view.getReadView(key));
    }
}
