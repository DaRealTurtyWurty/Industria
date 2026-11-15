package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.mechanical.MechanicalNode;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Set;

public class KineticDynamoBlock extends IndustriaBlock implements MechanicalNode {
    public KineticDynamoBlock(Properties settings) {
        super(settings, new BlockProperties()
            .hasFacing()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.KINETIC_DYNAMO)
                .shouldTick()));
    }

    @Override
    public Set<Direction> getMechanicalPorts(BlockState state) {
        return Set.of(state.getValue(BlockStateProperties.FACING).getOpposite());
    }
}
