package dev.turtywurty.industria.block.abstraction.state;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateProperties {
    private final Map<String, StateProperty<?>> properties = new ConcurrentHashMap<>();

    public void addHorizontalFacing() {
        addProperty(new StateProperty<>(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    public void addAxis() {
        addProperty(new StateProperty<>(BlockStateProperties.AXIS, Direction.Axis.Y));
    }

    public void addLit() {
        addProperty(new StateProperty<>(BlockStateProperties.LIT, false));
    }

    public void addWaterlogged() {
        addProperty(new StateProperty<>(BlockStateProperties.WATERLOGGED, false));
    }

    public <T extends Comparable<T>> void addProperty(StateProperty<T> property) {
        if (this.properties.containsKey(property.delegate().getName())) {
            throw new IllegalArgumentException("Property with name: " + property.delegate().getName() + " already exists!");
        }

        this.properties.put(property.delegate().getName(), property);
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> StateProperty<T> getProperty(String name, Class<T> type) {
        StateProperty<?> stateProperty = this.properties.get(name);
        if (stateProperty == null) {
            throw new IllegalArgumentException("Property with name: " + name + " does not exist!");
        }

        try {
            return (StateProperty<T>) stateProperty;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Property with name: " + name + " is not of the correct type!");
        }
    }

    public <T extends Comparable<T>> Property<T> getProperty(Property<T> property) {
        String name = property.getName();
        return containsProperty(name) ? getProperty(name, property.getValueClass()).delegate() : null;
    }

    public boolean containsProperty(String name) {
        return this.properties.containsKey(name);
    }

    public boolean containsProperty(Property<?> property) {
        return containsProperty(property.getName());
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> void setDefaultValue(String name, T value) {
        StateProperty<T> stateProperty = getProperty(name, (Class<T>) value.getClass());
        stateProperty.setDefaultValue(value);
    }

    private <T extends Comparable<T>> BlockState applyDefault(BlockState state, StateProperty<T> property) {
        return state.setValue(property.delegate(), property.defaultValue());
    }

    public @Nullable BlockState applyDefaults(@Nullable BlockState state) {
        if(state == null)
            return null;

        for (StateProperty<?> property : this.properties.values()) {
            state = applyDefault(state, property);
        }

        return state;
    }

    public void addToBuilder(StateDefinition.Builder<Block, BlockState> builder) {
        for (StateProperty<?> property : this.properties.values()) {
            builder.add(property.delegate());
        }
    }
}
