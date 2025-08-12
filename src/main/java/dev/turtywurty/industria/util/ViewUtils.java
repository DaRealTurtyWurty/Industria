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

    public static Set<String> getKeys(ReadView view) {
        // noinspection deprecation
        return view.read(MapCodec.assumeMapUnsafe(NbtCompound.CODEC)).orElseThrow().getKeys();
    }

    public static void put(WriteView view, NbtCompound compound) {
        for (var entry : compound.entrySet()) {
            view.put(entry.getKey(), Codecs.NBT_ELEMENT, entry.getValue());
        }
    }

    public static void putChild(WriteView view, String key, ValueIOSerializable child) {
        child.writeData(view.get(key));
    }

    public static void readChild(ReadView view, String key, ValueIOSerializable child) {
        child.readData(view.getReadView(key));
    }
}
