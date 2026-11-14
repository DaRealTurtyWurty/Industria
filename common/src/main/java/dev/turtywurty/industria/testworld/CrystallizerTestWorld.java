package dev.turtywurty.industria.testworld;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.multiblocklib.MultiblockLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/** Crystallization after clarification, with water supplied by the production line's pump. */
public final class CrystallizerTestWorld {
    private CrystallizerTestWorld() {
    }

    public static TestWorldContext placeMachines(TestWorldContext clarifier, ServerPlayer player) {
        TestWorldContext crystallizer = clarifier.at(0, 0, -10);
        crystallizer.constructMultiblockPattern(-1, 0, -1,
            MultiblockLib.DEFINITION_MANAGER.get(Industria.id("crystallizer")));
        crystallizer.useItemOnBlock(0, 0, 0, player, ModItems.WRENCH, InteractionHand.MAIN_HAND);

        // Replace the old terminal tank so clarified fluid continues into the crystallizer.
        if (clarifier.overworld().getBlockState(clarifier.pos(2, 0, -6)).is(ModBlocks.FLUID_TANK.get())) {
            clarifier.removeBlock(2, 0, -6);
        }

        // Aluminium hydroxide leaves the east port; sodium carbonate leaves the north port.
        crystallizer.setBlock(2, 0, 0, ModBlocks.FEEDER_CONVEYOR.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        crystallizer.setBlock(3, 0, 0, ModBlocks.CONVEYOR.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        crystallizer.setBlock(4, 0, 0, Blocks.CHEST);
        crystallizer.setBlock(0, 0, -2, ModBlocks.FEEDER_CONVEYOR.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        crystallizer.setBlock(0, 0, -3, ModBlocks.CONVEYOR.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        crystallizer.setBlock(0, 0, -4, Blocks.CHEST);
        return crystallizer;
    }

    public static void connectMachines(
        TestWorldContext crystallizer,
        TestWorldContext clarifier,
        TestWorldContext pool
    ) {
        // Sodium aluminate enters the east port at (1, 2, 0).
        connect(crystallizer, List.of(
            clarifier.pos(2, 0, -5),
            crystallizer.pos(2, 0, 3),
            crystallizer.pos(2, 2, 3),
            crystallizer.pos(2, 2, 0)));

        // Keep the pump's water branch above the other networks, then enter the north port.
        connect(crystallizer, List.of(
            pool.pos(1, 3, 0),
            pool.pos(1, 4, 0),
            pool.pos(0, 4, 0),
            crystallizer.pos(5, 4, -3),
            crystallizer.pos(0, 4, -3),
            crystallizer.pos(0, 4, -2),
            crystallizer.pos(0, 2, -2)));
    }

    private static void connect(TestWorldContext context, List<BlockPos> waypoints) {
        for (BlockPos pos : context.runPipe(waypoints)) {
            context.overworld().updateNeighborsAt(pos, ModBlocks.FLUID_PIPE.get());
        }
    }
}
