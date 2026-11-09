package dev.turtywurty.industria.connected.model;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

public class FabricVerticalConnectedModel extends WrapperBlockStateModel {
    private final BakedVerticalConnectedModels models;
    private final Set<Block> additionalConnections;

    public FabricVerticalConnectedModel(BakedVerticalConnectedModels models, VerticalConnectedModelSettings settings) {
        super(models.normal());
        this.models = models;
        this.additionalConnections = settings.resolveBlocks();
    }

    private BlockStateModel select(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return models.select(level, pos, state, additionalConnections);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        BlockStateModel selected = select(level, pos, state);
        selected.emitQuads(emitter, level, pos, state, random, cullTest);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return select(level, pos, state).createGeometryKey(level, pos, state, random);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return select(level, pos, state).particleMaterial(level, pos, state);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return select(level, pos, state).materialFlags(level, pos, state, random);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, @BakedQuad.MaterialFlags int flag) {
        return select(level, pos, state).hasMaterialFlag(level, pos, state, random, flag);
    }
}
