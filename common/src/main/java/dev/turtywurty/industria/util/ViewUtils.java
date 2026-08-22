package dev.turtywurty.industria.util;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class ViewUtils {
    private ViewUtils() {
        // Prevent initialization
    }

    public static void putChild(ValueOutput view, String key, ViewSerializable serializable) {
        serializable.writeData(view.child(key));
    }

    public static void readChild(ValueInput view, String key, ViewSerializable serializable) {
        serializable.readData(view.childOrEmpty(key));
    }
}
