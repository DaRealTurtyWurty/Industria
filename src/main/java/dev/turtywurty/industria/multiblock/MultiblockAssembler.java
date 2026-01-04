package dev.turtywurty.industria.multiblock;

import dev.turtywurty.industria.blockentity.MultiblockIOBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

public record MultiblockAssembler(MultiblockDefinition definition, MultiblockMatcher.MatchResult matchResult) {
    public AssembleResult assemble(ServerLevel world) {
        for (MultiblockMatcher.MatchResult.Cell cell : matchResult.cells()) {
            if(Objects.equals(cell.position(), matchResult.controllerPos()))
                continue;

            boolean placedPort = false;
            for (MultiblockMatcher.MatchResult.ResolvedPort port : matchResult.ports()) {
                if(port.localX() == cell.localX() && port.localY() == cell.localY() && port.localZ() == cell.localZ()) {
                    BlockPos pos = cell.position();
                    world.setBlockAndUpdate(pos, BlockInit.AUTO_MULTIBLOCK_IO.defaultBlockState());
                    if(world.getBlockEntity(pos) instanceof MultiblockIOBlockEntity io) {
                        //io.setPortData(port.something()?);
                    }

                    placedPort = true;
                    break;
                }
            }

            if(!placedPort) {
                BlockPos pos = cell.position();
                world.setBlockAndUpdate(pos, BlockInit.AUTO_MULTIBLOCK_BLOCK.defaultBlockState());
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
