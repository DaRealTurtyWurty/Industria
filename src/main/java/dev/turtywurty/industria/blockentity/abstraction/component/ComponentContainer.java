package dev.turtywurty.industria.blockentity.abstraction.component;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ComponentContainer {
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();

    public <T extends Component> void addComponent(Class<T> clazz, T component) {
        this.components.put(clazz, component);
    }

    public <T extends Component> T getComponent(Class<T> clazz) {
        return clazz.cast(this.components.get(clazz));
    }

    public void removeComponent(Class<? extends Component> clazz) {
        this.components.remove(clazz);
    }

    public boolean hasComponent(Class<? extends Component> clazz) {
        return this.components.containsKey(clazz);
    }

    public void clearComponents() {
        this.components.clear();
    }

    public Map<Class<? extends Component>, Component> getComponents() {
        return ImmutableMap.copyOf(this.components);
    }

    public void forEach(ComponentConsumer<Component> consumer) {
        this.components.forEach((clazz, component) -> consumer.accept(component));
    }

    public Stream<Component> stream() {
        return this.components.values().stream();
    }

    @FunctionalInterface
    public interface ComponentConsumer<T extends Component> {
        void accept(T component);
    }
}