package dev.turtywurty.industria.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedVoxelShapes {
    private final VoxelShapeFactory factory;
    private final Map<LevelReader, Map<BlockPos, VoxelShape>> cache = new ConcurrentHashMap<>();

    public CachedVoxelShapes(VoxelShapeFactory factory) {
        this.factory = factory;
    }

    public VoxelShape getShape(LevelReader world, BlockPos pos) {
        return this.cache.computeIfAbsent(world, w -> new HashMap<>())
                .computeIfAbsent(pos, p -> this.factory.create(world, p));
    }

    public void clear() {
        this.cache.clear();
    }

    @FunctionalInterface
    public interface VoxelShapeFactory {
        VoxelShape create(LevelReader world, BlockPos pos);
    }
}
