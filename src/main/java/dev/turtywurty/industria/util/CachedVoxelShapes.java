package dev.turtywurty.industria.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.WorldView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedVoxelShapes {
    private final VoxelShapeFactory factory;
    private final Map<WorldView, Map<BlockPos, VoxelShape>> cache = new ConcurrentHashMap<>();

    public CachedVoxelShapes(VoxelShapeFactory factory) {
        this.factory = factory;
    }

    public VoxelShape getShape(WorldView world, BlockPos pos) {
        return this.cache.computeIfAbsent(world, w -> new HashMap<>())
                .computeIfAbsent(pos, p -> this.factory.create(world, p));
    }

    public void clear() {
        this.cache.clear();
    }

    @FunctionalInterface
    public interface VoxelShapeFactory {
        VoxelShape create(WorldView world, BlockPos pos);
    }
}
