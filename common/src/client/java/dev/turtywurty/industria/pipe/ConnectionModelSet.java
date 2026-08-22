package dev.turtywurty.industria.pipe;

import com.mojang.math.Quadrant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import java.util.EnumMap;
import java.util.Map;

public final class ConnectionModelSet {
    private final Identifier id;
    private final Map<Direction, ConnectionModelReference> models;

    private ConnectionModelSet(Identifier id, Map<Direction, ConnectionModelReference> models) {
        this.id = id;
        this.models = Map.copyOf(models);
    }

    public Identifier id() {
        return id;
    }

    public ConnectionModelReference get(Direction pipeToTarget) {
        return models.get(pipeToTarget);
    }

    public Iterable<ConnectionModelReference> references() {
        return models.values();
    }

    public static ConnectionModelSet horizontalAndVertical(
            Identifier id,
            Identifier horizontalModel,
            Identifier upModel,
            Identifier downModel
    ) {
        Map<Direction, ConnectionModelReference> models = new EnumMap<>(Direction.class);

        models.put(
                Direction.NORTH,
                reference(
                        id,
                        Direction.NORTH,
                        horizontalModel,
                        Variant.SimpleModelState.DEFAULT
                )
        );

        models.put(
                Direction.EAST,
                reference(
                        id,
                        Direction.EAST,
                        horizontalModel,
                        Variant.SimpleModelState.DEFAULT.withY(Quadrant.R90)
                )
        );

        models.put(
                Direction.SOUTH,
                reference(
                        id,
                        Direction.SOUTH,
                        horizontalModel,
                        Variant.SimpleModelState.DEFAULT.withY(Quadrant.R180)
                )
        );

        models.put(
                Direction.WEST,
                reference(
                        id,
                        Direction.WEST,
                        horizontalModel,
                        Variant.SimpleModelState.DEFAULT.withY(Quadrant.R270)
                )
        );

        models.put(
                Direction.UP,
                reference(id, Direction.UP, upModel, Variant.SimpleModelState.DEFAULT)
        );

        models.put(
                Direction.DOWN,
                reference(id, Direction.DOWN, downModel, Variant.SimpleModelState.DEFAULT)
        );

        return new ConnectionModelSet(id, models);
    }

    public static ConnectionModelSet horizontal(Identifier id, Identifier horizontalModel) {
        Map<Direction, ConnectionModelReference> models = new EnumMap<>(Direction.class);

        addHorizontalModels(models, id, horizontalModel);
        return new ConnectionModelSet(id, models);
    }

    public static ConnectionModelSet forDirection(Identifier id, Direction direction, Identifier model) {
        return new ConnectionModelSet(id, Map.of(
                direction,
                reference(id, direction, model, Variant.SimpleModelState.DEFAULT)
        ));
    }

    public static ConnectionModelSet forDirection(
            Identifier id,
            Direction direction,
            Identifier model,
            Variant.SimpleModelState state
    ) {
        return new ConnectionModelSet(id, Map.of(
                direction,
                reference(id, direction, model, state)
        ));
    }

    public static ConnectionModelSet rotatedFromNorth(Identifier id, Identifier model) {
        Map<Direction, ConnectionModelReference> models = new EnumMap<>(Direction.class);

        addHorizontalModels(models, id, model);
        models.put(Direction.UP, reference(id, Direction.UP, model,
                Variant.SimpleModelState.DEFAULT.withX(Quadrant.R270)));
        models.put(Direction.DOWN, reference(id, Direction.DOWN, model,
                Variant.SimpleModelState.DEFAULT.withX(Quadrant.R90)));
        return new ConnectionModelSet(id, models);
    }

    private static void addHorizontalModels(
            Map<Direction, ConnectionModelReference> models,
            Identifier id,
            Identifier horizontalModel
    ) {
        models.put(Direction.NORTH,
                reference(id, Direction.NORTH, horizontalModel, Variant.SimpleModelState.DEFAULT));
        models.put(Direction.EAST,
                reference(id, Direction.EAST, horizontalModel,
                        Variant.SimpleModelState.DEFAULT.withY(Quadrant.R90)));
        models.put(Direction.SOUTH,
                reference(id, Direction.SOUTH, horizontalModel,
                        Variant.SimpleModelState.DEFAULT.withY(Quadrant.R180)));
        models.put(Direction.WEST,
                reference(id, Direction.WEST, horizontalModel,
                        Variant.SimpleModelState.DEFAULT.withY(Quadrant.R270)));
    }

    private static ConnectionModelReference reference(
            Identifier setId,
            Direction direction,
            Identifier modelId,
            Variant.SimpleModelState state
    ) {
        Identifier keyId = setId.withSuffix("/" + direction.getName());

        return new ConnectionModelReference(
                keyId,
                modelId,
                state.asModelState()
        );
    }
}
