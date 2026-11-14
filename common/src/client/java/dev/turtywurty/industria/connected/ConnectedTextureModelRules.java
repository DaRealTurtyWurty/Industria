package dev.turtywurty.industria.connected;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.*;

public final class ConnectedTextureModelRules {
    private final int[] connectionGroups = new int[Direction.values().length];
    private final ConnectedTextureAxis[] axes = new ConnectedTextureAxis[Direction.values().length];
    private final ConnectedTextureLayoutType[] layouts = new ConnectedTextureLayoutType[Direction.values().length];
    private final List<Set<Block>> connections;

    public ConnectedTextureModelRules(ConnectedTextureSettings settings) {
        Arrays.fill(connectionGroups, -1);
        Map<Set<Identifier>, Integer> groups = new HashMap<>();
        List<Set<Block>> resolved = new ArrayList<>();
        for (Direction face : Direction.values()) {
            if (!settings.connectedFaces().contains(face))
                continue;

            ConnectedTextureRule rule = settings.ruleFor(face);
            Integer group = groups.get(rule.connectsTo());
            if (group == null) {
                group = resolved.size();
                resolved.add(rule.resolveBlocks());
                groups.put(rule.connectsTo(), group);
            }
            connectionGroups[face.ordinal()] = group;
            axes[face.ordinal()] = rule.axis();
            layouts[face.ordinal()] = rule.layout();
        }
        connections = List.copyOf(resolved);
    }

    public boolean isEmpty() {
        return connections.isEmpty();
    }

    public int connectionGroup(Direction face) {
        return connectionGroups[face.ordinal()];
    }

    public ConnectedTextureAxis axis(Direction face) {
        return axes[face.ordinal()];
    }

    public int groupCount() {
        return connections.size();
    }

    public Set<Block> connections(int group) {
        return connections.get(group);
    }

    public ConnectedTextureLayoutType layout(Direction face) {
        return layouts[face.ordinal()];
    }
}
