package dev.turtywurty.industria.util;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

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
}
