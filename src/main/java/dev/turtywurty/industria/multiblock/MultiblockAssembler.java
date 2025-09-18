package dev.turtywurty.industria.multiblock;

import dev.turtywurty.industria.blockentity.MultiblockIOBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public record MultiblockAssembler(MultiblockDefinition definition, MultiblockMatcher.MatchResult matchResult) {
    public AssembleResult assemble(ServerWorld world) {
        for (MultiblockMatcher.MatchResult.Cell cell : matchResult.cells()) {
            if(Objects.equals(cell.position(), matchResult.controllerPos()))
                continue;

            boolean placedPort = false;
            for (MultiblockMatcher.MatchResult.ResolvedPort port : matchResult.ports()) {
                if(port.localX() == cell.localX() && port.localY() == cell.localY() && port.localZ() == cell.localZ()) {
                    BlockPos pos = cell.position();
                    world.setBlockState(pos, BlockInit.AUTO_MULTIBLOCK_IO.getDefaultState());
                    if(world.getBlockEntity(pos) instanceof MultiblockIOBlockEntity io) {
                        //io.setPortData(port.something()?);
                    }

                    placedPort = true;
                    break;
                }
            }

            if(!placedPort) {
                BlockPos pos = cell.position();
                world.setBlockState(pos, BlockInit.AUTO_MULTIBLOCK_BLOCK.getDefaultState());
            }
        }

        Block controllerBlock = world.getBlockState(matchResult.controllerPos()).getBlock();
        if (controllerBlock instanceof MultiblockController controller) {
            controller.onAssembled(world, matchResult.controllerPos(), matchResult);
        }

        return new AssembleResult();
    }

    public record AssembleResult() {}
}
