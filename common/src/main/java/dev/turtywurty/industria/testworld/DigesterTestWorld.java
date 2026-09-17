package dev.turtywurty.industria.testworld;

import com.mojang.logging.LogUtils;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.blockentity.DigesterBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.multiblocklib.MultiblockLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Supplier;

/** The digester stage of the test world's mixer-to-fluid process line. */
public final class DigesterTestWorld {
    private static final Logger LOGGER = LogUtils.getLogger();

    private DigesterTestWorld() {
    }

    public static TestWorldContext placeMachines(TestWorldContext context, TestWorldContext mixer, ServerPlayer player) {
        removeValidationFixtures(context);

        TestWorldContext digester = mixer.at(-8, 0, -4);
        digester.constructMultiblockPattern(-2, 0, -2,
                MultiblockLib.DEFINITION_MANAGER.get(Industria.id("digester")));
        digester.useItemOnBlock(0, 0, 0, player, ModItems.WRENCH, InteractionHand.MAIN_HAND);
        digester.setBlock(0, 0, -6, ModBlocks.FLUID_TANK);
        return digester;
    }

    public static void connectMachines(TestWorldContext digester, TestWorldContext mixer, TestWorldContext energy) {
        connect(digester, List.of(
                mixer.pos(-1, 0, 1),
                mixer.pos(-1, 0, -4),
                digester.pos(4, 0, 0),
                digester.pos(4, 3, 0),
                digester.pos(0, 3, 0)
        ), ModBlocks.SLURRY_PIPE);

        connect(digester, List.of(
                energy.pos(-4, 3, 2),
                digester.pos(0, 3, 7),
                digester.pos(0, 1, 7),
                digester.pos(0, 1, 3)
        ), ModBlocks.CABLE);

        connect(digester, List.of(
                digester.pos(0, 0, -3),
                digester.pos(0, 0, -5)
        ), ModBlocks.FLUID_PIPE);

        digester.afterTicks(20, current -> validate(current, Direction.NORTH,
                DigesterBlockEntity.FLUID_OUTPUT_PORT, DigesterBlockEntity.ENERGY_PORT,
                DigesterBlockEntity.SLURRY_INPUT_PORT));
    }

    private static void connect(TestWorldContext context, List<BlockPos> waypoints, Supplier<? extends PipeBlock<?, ?>> pipe) {
        List<BlockPos> path = context.runPipe(waypoints, pipe);
        for (BlockPos pos : path)
            context.overworld().updateNeighborsAt(pos, pipe.get());
    }

    private static void removeValidationFixtures(TestWorldContext context) {
        var definition = MultiblockLib.DEFINITION_MANAGER.get(Industria.id("digester"));
        int index = 0;
        for (Rotation rotation : Rotation.values()) {
            var fixture = context.at(10 + (index % 2) * 10, 0, (index / 2) * 10);
            index++;
            if (!(fixture.overworld().getBlockEntity(fixture.origin()) instanceof DigesterBlockEntity digester))
                continue;

            // Upgrade existing test saves as well as generating the new layout in fresh saves.
            digester.breakMultiblock();
            for (BlockPos position : definition.pattern().positions()) {
                BlockPos offset = position.subtract(new BlockPos(2, 0, 2)).rotate(rotation);
                var expected = definition.pattern().matcherAt(position).exampleState().orElseThrow();
                BlockPos worldPos = fixture.origin().offset(offset);
                if (fixture.overworld().getBlockState(worldPos).is(expected.getBlock()))
                    fixture.overworld().removeBlock(worldPos, false);
            }
            Direction front = rotation.rotate(Direction.NORTH);
            BlockPos fluid = DigesterBlockEntity.FLUID_OUTPUT_PORT.rotate(rotation).relative(front);
            removePipe(fixture, fluid, ModBlocks.FLUID_PIPE.get());
            removePipe(fixture, fluid.relative(front), ModBlocks.FLUID_PIPE.get());
            removePipe(fixture, DigesterBlockEntity.ENERGY_PORT.rotate(rotation).relative(front.getOpposite()), ModBlocks.CABLE.get());
            removePipe(fixture, DigesterBlockEntity.SLURRY_INPUT_PORT.above(), ModBlocks.SLURRY_PIPE.get());
        }
    }

    private static void removePipe(TestWorldContext context, BlockPos offset, PipeBlock<?, ?> pipe) {
        BlockPos worldPos = context.origin().offset(offset);
        if (context.overworld().getBlockState(worldPos).is(pipe))
            context.overworld().removeBlock(worldPos, false);
    }

    private static void validate(TestWorldContext context, Direction front, BlockPos fluid, BlockPos energy, BlockPos slurry) {
        var digester = context.requireBlockEntity(0, 0, 0, DigesterBlockEntity.class);
        boolean formed = digester.isFormed();
        boolean fluidPort = digester.getFluidStorageForExternal(context.origin().offset(fluid), front) != null;
        boolean energyPort = digester.getEnergyStorageForExternal(context.origin().offset(energy), front.getOpposite()) != null;
        boolean slurryPort = digester.getSlurryStorageForExternal(context.origin().offset(slurry), Direction.UP) != null;
        boolean rejectsWrongFace = digester.getFluidStorageForExternal(context.origin().offset(fluid), front.getOpposite()) == null
                && digester.getEnergyStorageForExternal(context.origin().offset(energy), front) == null
                && digester.getSlurryStorageForExternal(context.origin().offset(slurry), Direction.DOWN) == null;
        boolean connected = isConnected(context, fluid.relative(front), front.getOpposite())
                && isConnected(context, energy.relative(front.getOpposite()), front)
                && isConnected(context, slurry.above(), Direction.DOWN);
        boolean collision = !context.overworld().noCollision(new AABB(0.4, 1, 0.4, 0.6, 1.2, 0.6).move(context.origin()));
        boolean cornerClear = context.overworld().noCollision(new AABB(2.7, 1, 2.7, 2.8, 1.2, 2.8).move(context.origin()));
        boolean aboveLidClear = context.overworld().noCollision(new AABB(1.8, 2.8, 0.4, 1.9, 2.9, 0.6).move(context.origin()));
        LOGGER.info("Digester in-game validation facing={} position={}: formed={}, fluid={}, energy={}, slurry={}, rejectsWrongFace={}, pipesConnected={}, bodyCollision={}, cornerClear={}, aboveLidClear={}",
                front, context.origin(), formed, fluidPort, energyPort, slurryPort, rejectsWrongFace, connected, collision, cornerClear, aboveLidClear);
        if (!(formed && fluidPort && energyPort && slurryPort && rejectsWrongFace && connected && collision && cornerClear && aboveLidClear))
            LOGGER.error("Digester in-game validation FAILED for {}", front);
    }

    private static boolean isConnected(TestWorldContext context, BlockPos offset, Direction direction) {
        return context.overworld().getBlockState(context.origin().offset(offset))
                .getValue(PipeBlock.propertyFor(direction)) == PipeBlock.ConnectorType.BLOCK;
    }
}
