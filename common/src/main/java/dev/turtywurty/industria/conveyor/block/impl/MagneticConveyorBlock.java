package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.block.ConveyorInput;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MagneticConveyorBlock extends AbstractPoweredConveyorBlock {
    public MagneticConveyorBlock(Properties settings) {
        super(settings, new BlockProperties()
                .blockEntityProperties(
                        new BlockProperties.BlockBlockEntityProperties<>(() -> ModBlockEntityTypes.MAGNETIC_CONVEYOR.get())
                                .shouldTick())
        );
    }

    @Override
    public ConveyorTopology getTopology(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction back = facing.getOpposite();

        return new ConveyorTopology(
                List.of(new ConveyorInput("in", pos.relative(back))),
                List.of(new ConveyorOutput("out", pos.relative(facing), pos))
        );
    }
}
