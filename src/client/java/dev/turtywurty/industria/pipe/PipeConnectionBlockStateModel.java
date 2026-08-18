package dev.turtywurty.industria.pipe;

import dev.turtywurty.industria.block.PipeBlock;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public final class PipeConnectionBlockStateModel extends WrapperBlockStateModel {
    public PipeConnectionBlockStateModel(BlockStateModel wrapped) {
        super(wrapped);
    }

    @Override
    public void emitQuads(
            QuadEmitter emitter,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            RandomSource random,
            Predicate<@Nullable Direction> cullTest
    ) {
        long seed = state.getSeed(pos);
        random.setSeed(seed);
        wrapped.emitQuads(emitter, level, pos, state, random, cullTest);

        for (Direction pipeToTarget : Direction.values()) {
            PipeBlock.ConnectorType connectorType = state.getValue(PipeBlock.propertyFor(pipeToTarget));
            if (connectorType != PipeBlock.ConnectorType.BLOCK)
                continue;

            BlockPos targetPos = pos.relative(pipeToTarget);
            BlockState targetState = level.getBlockState(targetPos);
            Direction targetFace = pipeToTarget.getOpposite();

            ConnectionModelSet models = PipeConnectionModelRegistry.findModel(
                    level, targetPos, state, targetState, targetFace);
            if (models == null)
                continue;

            ConnectionModelReference reference = models.get(pipeToTarget);
            if (reference == null)
                continue;

            BlockStateModel connectionModel = getBakedModel(reference);
            if (connectionModel == null)
                continue;

            random.setSeed(seed ^ ((long) pipeToTarget.ordinal() * 0x9E3779B97F4A7C15L));

            connectionModel.emitQuads(emitter, level, pos, state, random, cullTest);
        }
    }

    @Override
    public @Nullable Object createGeometryKey(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            RandomSource random
    ) {
        random.setSeed(state.getSeed(pos));
        Object wrappedKey = wrapped.createGeometryKey(level, pos, state, random);
        if (wrappedKey == null)
            return null;

        return new GeometryKey(
                wrappedKey,
                findModelId(level, pos, state, Direction.NORTH),
                findModelId(level, pos, state, Direction.SOUTH),
                findModelId(level, pos, state, Direction.WEST),
                findModelId(level, pos, state, Direction.EAST),
                findModelId(level, pos, state, Direction.UP),
                findModelId(level, pos, state, Direction.DOWN)
        );
    }

    private static @Nullable Identifier findModelId(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            Direction pipeToTarget
    ) {
        if (state.getValue(PipeBlock.propertyFor(pipeToTarget)) != PipeBlock.ConnectorType.BLOCK)
            return null;

        BlockPos targetPos = pos.relative(pipeToTarget);
        BlockState targetState = level.getBlockState(targetPos);
        ConnectionModelSet models = PipeConnectionModelRegistry.findModel(
                level, targetPos, state, targetState, pipeToTarget.getOpposite());
        return models == null ? null : models.id();
    }

    @Nullable
    private static BlockStateModel getBakedModel(ConnectionModelReference reference) {
        FabricModelManager modelManager = Minecraft.getInstance().getModelManager();
        return modelManager.getModel(reference.modelKey());
    }

    private record GeometryKey(Object wrapped, @Nullable Identifier north, @Nullable Identifier south,
                               @Nullable Identifier west, @Nullable Identifier east, @Nullable Identifier up,
                               @Nullable Identifier down) {
    }
}
