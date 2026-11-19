package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.mechanical.MechanicalNode;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class GearboxBlock extends IndustriaBlock implements MechanicalNode {
    public static final EnumProperty<Direction> INPUT = EnumProperty.create("input", Direction.class);
    public static final EnumProperty<Direction> OUTPUT = EnumProperty.create("output", Direction.class);

    public GearboxBlock(Properties properties) {
        super(properties, new BlockProperties()
            .addStateProperty(INPUT, Direction.NORTH)
            .addStateProperty(OUTPUT, Direction.EAST));
    }

    @Override
    public Set<Direction> getMechanicalPorts(BlockState state) {
        Direction input = state.getValue(INPUT);
        Direction output = state.getValue(OUTPUT);
        return input.getAxis() == output.getAxis() ? Set.of() : Set.of(input, output);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction input = ctx.getClickedFace().getOpposite();
        for (Direction output : ctx.getNearestLookingDirections()) {
            if (output.getAxis() != input.getAxis())
                return defaultBlockState()
                    .setValue(INPUT, input)
                    .setValue(OUTPUT, output);
        }

        return null;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        Direction input = state.getValue(INPUT);
        Direction output = state.getValue(OUTPUT);

        return state.setValue(INPUT, rotation.rotate(input))
            .setValue(OUTPUT, rotation.rotate(output));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        Direction input = state.getValue(INPUT);
        Direction output = state.getValue(OUTPUT);

        return state.setValue(INPUT, mirror.mirror(input))
            .setValue(OUTPUT, mirror.mirror(output));
    }
}
