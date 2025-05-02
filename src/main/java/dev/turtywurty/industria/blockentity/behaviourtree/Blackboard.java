package dev.turtywurty.industria.blockentity.behaviourtree;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Blackboard {
    private final Map<String, Object> data = new HashMap<>();

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public <T> @Nullable T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        }

        return null;
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }
}