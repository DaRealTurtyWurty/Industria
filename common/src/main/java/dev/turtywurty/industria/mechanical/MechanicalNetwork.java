package dev.turtywurty.industria.mechanical;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class MechanicalNetwork {
    private static final AtomicLong NEXT_ID = new AtomicLong();

    private final long id = NEXT_ID.incrementAndGet();
    private final Set<BlockPos> nodes;
    private final MechanicalSimulation simulation;
    private final List<BlockPos> participantPositions = new ArrayList<>();
    private boolean participantsCached;
    private double rotationRadians;
    private long rotationGameTick;

    public MechanicalNetwork(Set<BlockPos> nodes, double initialRPM) {
        this(nodes, initialRPM, 0.0);
    }

    public MechanicalNetwork(Set<BlockPos> nodes, double initialRPM, double initialRotationRadians) {
        this.nodes = Set.copyOf(Objects.requireNonNull(nodes, "nodes cannot be null"));
        this.simulation = new MechanicalSimulation(initialRPM);
        this.rotationRadians = initialRotationRadians;
    }

    public long getId() {
        return id;
    }

    public MechanicalState getState() {
        return simulation.getState();
    }

    public Set<BlockPos> getNodes() {
        return nodes;
    }

    public double getRotationRadians() {
        return rotationRadians;
    }

    public ShaftRotation getRotationSnapshot() {
        return new ShaftRotation(rotationRadians, getState().rpm(), rotationGameTick);
    }

    public void tick(ServerLevel level) {
        if (!participantsCached) {
            refreshParticipants(level);
        }

        List<MechanicalSource> sources = new ArrayList<>();
        List<MechanicalConsumer> consumers = new ArrayList<>();
        for (BlockPos pos : participantPositions) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MechanicalSource source) {
                sources.add(source);
            }

            if (blockEntity instanceof MechanicalConsumer consumer) {
                consumers.add(consumer);
            }
        }

        simulation.tick(sources, consumers);

        rotationRadians = Math.IEEEremainder(rotationRadians + simulation.getRotationDelta(), Math.TAU);
        rotationGameTick = level.getGameTime();

        for (MechanicalSource source : sources) {
            source.onShaftUpdate(simulation.getState().rpm(), simulation.getRotationDelta());
        }
    }

    public void refreshParticipants(ServerLevel level) {
        participantPositions.clear();
        for (BlockPos pos : nodes) {
            if (!level.getBlockState(pos).hasBlockEntity())
                continue;

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MechanicalSource || blockEntity instanceof MechanicalConsumer) {
                participantPositions.add(pos);
            }
        }

        participantsCached = true;
    }
}
