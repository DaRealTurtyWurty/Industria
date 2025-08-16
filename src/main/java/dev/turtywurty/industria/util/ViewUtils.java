package dev.turtywurty.industria.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.Set;

public final class ViewUtils {
    private ViewUtils() {
        // Prevent initialization
    }

    public static void putChild(WriteView view, String key, ViewSerializable serializable) {
        serializable.writeData(view.get(key));
    }

    public static void readChild(ReadView view, String key, ViewSerializable serializable) {
        serializable.readData(view.getReadView(key));
    }

    public static Set<String> getKeys(ReadView view) {
        return view.read(MapCodec.assumeMapUnsafe(NbtCompound.CODEC)).orElseThrow().getKeys();
    }
}
