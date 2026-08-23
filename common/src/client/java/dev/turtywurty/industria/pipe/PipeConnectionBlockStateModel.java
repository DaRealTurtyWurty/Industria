package dev.turtywurty.industria.pipe;

import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.turtymultiloader.client.registration.BlockStateModelAugmenter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class PipeConnectionBlockStateModel implements BlockStateModelAugmenter {
    private static final long DIRECTION_SEED_MULTIPLIER = 0x9E3779B97F4A7C15L;

    @Override
    public void collectAdditionalModels(Context context, ModelCollector models) {
        BlockState state = context.state();
        for (Direction pipeToTarget : Direction.values()) {
            PipeBlock.ConnectorType connectorType = state.getValue(PipeBlock.propertyFor(pipeToTarget));
            if (connectorType != PipeBlock.ConnectorType.BLOCK)
                continue;

            BlockPos targetPos = context.pos().relative(pipeToTarget);
            BlockState targetState = context.level().getBlockState(targetPos);
            Direction targetFace = pipeToTarget.getOpposite();

            ConnectionModelSet modelSet = PipeConnectionModelRegistry.findModel(
                    context.level(),
                    targetPos,
                    state,
                    targetState,
                    targetFace
            );

            if (modelSet == null)
                continue;

            ConnectionModelReference reference = modelSet.get(pipeToTarget);
            if (reference == null)
                continue;

            BlockStateModel connectionModel = getBakedModel(reference);
            if (connectionModel == null)
                continue;

            long seed = context.baseSeed()
                    ^ ((long) pipeToTarget.ordinal()
                    * DIRECTION_SEED_MULTIPLIER);

            models.accept(connectionModel, seed);
        }
    }

    @Override
    public @Nullable Object createGeometryKey(Context context, @Nullable Object wrappedKey) {
        if (wrappedKey == null)
            return null;

        return new GeometryKey(
                wrappedKey,
                findModelId(context, Direction.NORTH),
                findModelId(context, Direction.SOUTH),
                findModelId(context, Direction.WEST),
                findModelId(context, Direction.EAST),
                findModelId(context, Direction.UP),
                findModelId(context, Direction.DOWN)
        );
    }

    private static @Nullable Identifier findModelId(Context context, Direction pipeToTarget) {
        BlockState state = context.state();

        if (state.getValue(PipeBlock.propertyFor(pipeToTarget)) != PipeBlock.ConnectorType.BLOCK)
            return null;

        BlockPos targetPos = context.pos().relative(pipeToTarget);
        BlockState targetState = context.level().getBlockState(targetPos);

        ConnectionModelSet modelSet = PipeConnectionModelRegistry.findModel(
                context.level(),
                targetPos,
                state,
                targetState,
                pipeToTarget.getOpposite()
        );

        return modelSet == null ? null : modelSet.id();
    }

    private static @Nullable BlockStateModel getBakedModel(ConnectionModelReference reference) {
        return PipeConnectionModelLoading.getBakedModel(reference);
    }

    private record GeometryKey(
            Object wrapped,
            @Nullable Identifier north,
            @Nullable Identifier south,
            @Nullable Identifier west,
            @Nullable Identifier east,
            @Nullable Identifier up,
            @Nullable Identifier down
    ) {
    }
}
