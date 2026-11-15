package dev.turtywurty.industria.mechanical;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public interface MechanicalNode {
    Set<Direction> getMechanicalPorts(BlockState state);

    default boolean isActiveMechanicalNode(BlockState state) {
        return !getMechanicalPorts(state).isEmpty();
    }
}
