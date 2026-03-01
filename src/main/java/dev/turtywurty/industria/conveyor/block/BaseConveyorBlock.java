package dev.turtywurty.industria.conveyor.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseConveyorBlock extends IndustriaBlock implements ConveyorLike {
    protected BaseConveyorBlock(Properties settings, BlockProperties properties) {
        super(settings, properties);
    }

    public final ConveyorTopology topology(Level level, BlockPos pos, BlockState state) {
        return getTopology(level, pos, state);
    }
}
