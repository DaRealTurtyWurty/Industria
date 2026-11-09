package dev.turtywurty.industria.connected.model;

import dev.turtywurty.industria.connected.ConnectedTextureNeighborhood;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Set;

public record BakedVerticalConnectedModels(
        BlockStateModel normal,
        BlockStateModel lower,
        BlockStateModel middle,
        BlockStateModel upper
) {
    public BakedVerticalConnectedModels {
        Objects.requireNonNull(normal, "normal cannot be null");
        Objects.requireNonNull(lower, "lower cannot be null");
        Objects.requireNonNull(middle, "middle cannot be null");
        Objects.requireNonNull(upper, "upper cannot be null");
    }

    public BlockStateModel modelFor(VerticalConnectedModelVariant variant) {
        return switch (variant) {
            case NORMAL -> normal;
            case LOWER -> lower;
            case MIDDLE -> middle;
            case UPPER -> upper;
        };
    }

    public BlockStateModel select(
            BlockGetter level,
            BlockPos pos,
            BlockState state,
            Set<Block> additionalConnections
    ) {
        var neighbourhood = new ConnectedTextureNeighborhood(level, pos, state, additionalConnections);
        boolean above = neighbourhood.isConnected(Direction.UP);
        boolean below = neighbourhood.isConnected(Direction.DOWN);

        VerticalConnectedModelVariant variant = VerticalConnectedModelVariant.fromConnections(above, below);
        return modelFor(variant);
    }

    public static BakedVerticalConnectedModels bake(ModelBaker baker, VerticalConnectedModelSettings settings) {
        return new BakedVerticalConnectedModels(
                bakeModel(baker, settings.normal()),
                bakeModel(baker, settings.lower()),
                bakeModel(baker, settings.middle()),
                bakeModel(baker, settings.upper())
        );
    }

    private static BlockStateModel bakeModel(ModelBaker baker, Identifier id) {
        return new SingleVariant(new Variant(id).bake(baker));
    }
}
