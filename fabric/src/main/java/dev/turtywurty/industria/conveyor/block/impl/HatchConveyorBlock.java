package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.block.BaseConveyorBlock;
import dev.turtywurty.industria.conveyor.block.ConveyorInput;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class HatchConveyorBlock extends AbstractPoweredConveyorBlock {
    public HatchConveyorBlock(Properties settings) {
        super(settings, new BlockProperties());
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

    @Override
    public boolean canAttachToStorageOutput(Level level, BlockPos pos, BlockState state, ConveyorOutput output, BlockPos storagePos) {
        return true;
    }
}
