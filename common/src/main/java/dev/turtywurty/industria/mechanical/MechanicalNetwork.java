package dev.turtywurty.industria.mechanical;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public class MechanicalNetwork {
    private final Set<BlockPos> nodes;
    private final MechanicalSimulation simulation;

    public MechanicalNetwork(Set<BlockPos> nodes, double initialRPM) {
        this.nodes = Set.copyOf(Objects.requireNonNull(nodes, "nodes cannot be null"));
        this.simulation = new MechanicalSimulation(initialRPM);
    }

    public MechanicalState getState() {
        return simulation.getState();
    }

    public Set<BlockPos> getNodes() {
        return nodes;
    }

    public void tick(ServerLevel level) {
        List<MechanicalSource> sources = findSources(level);
        simulation.tick(sources, findConsumers(level));
        for (MechanicalSource source : sources) {
            source.onShaftUpdate(simulation.getState().rpm(), simulation.getRotationDelta());
        }
    }

    private List<MechanicalSource> findSources(ServerLevel level) {
        List<MechanicalSource> sources = new ArrayList<>();
        for (BlockPos pos : nodes) {
            if (level.getBlockEntity(pos) instanceof MechanicalSource source) {
                sources.add(source);
            }
        }

        return sources;
    }

    private List<MechanicalConsumer> findConsumers(ServerLevel level) {
        List<MechanicalConsumer> consumers = new ArrayList<>();
        for (BlockPos pos : nodes) {
            if (level.getBlockEntity(pos) instanceof MechanicalConsumer consumer) {
                consumers.add(consumer);
            }
        }

        return consumers;
    }
}
