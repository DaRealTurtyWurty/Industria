package dev.turtywurty.industria.testworld;

import com.mojang.logging.LogUtils;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.blockentity.ClarifierBlockEntity;
import dev.turtywurty.industria.blockentity.util.StorageOperations;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModFluids;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.multiblocklib.MultiblockLib;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Supplier;

/** Powered clarification after digestion, with separate fluid and red-mud collection. */
public final class ClarifierTestWorld {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ClarifierTestWorld() {}

    public static TestWorldContext placeMachines(TestWorldContext digester, ServerPlayer player) {
        TestWorldContext clarifier = digester.at(0, 0, -12);
        clarifier.constructMultiblockPattern(-2, 0, -2,
                MultiblockLib.DEFINITION_MANAGER.get(Industria.id("clarifier")));
        clarifier.useItemOnBlock(0, 0, 0, player, ModItems.WRENCH, InteractionHand.MAIN_HAND);
        clarifier.setBlock(2, 0, -6, ModBlocks.FLUID_TANK);
        clarifier.setBlock(0, 1, -3, ModBlocks.FEEDER_CONVEYOR.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        clarifier.setBlock(0, 1, -4, ModBlocks.CONVEYOR.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        clarifier.setBlock(0, 1, -5, Blocks.CHEST);
        return clarifier;
    }

    public static void connectMachines(TestWorldContext clarifier, TestWorldContext digester) {
        connect(clarifier, List.of(digester.pos(0, 0, -3), digester.pos(0, 0, -6),
                digester.pos(-4, 0, -6), digester.pos(-4, 2, -6),
                clarifier.pos(-4, 2, 0), clarifier.pos(-3, 2, 0)), ModBlocks.FLUID_PIPE);
        connect(clarifier, List.of(digester.pos(0, 1, 7), digester.pos(-5, 1, 7),
                clarifier.pos(-5, 1, 0), clarifier.pos(-3, 1, 0)), ModBlocks.CABLE);
        connect(clarifier, List.of(clarifier.pos(0, 0, -3), clarifier.pos(2, 0, -3),
                clarifier.pos(2, 0, -5)), ModBlocks.FLUID_PIPE);

        clarifier.afterTicks(20, current -> {
            boolean validation = Boolean.getBoolean("industria.test.clarifier.validation")
                    && current.server().isDedicatedServer();
            try {
                validateConnections(current);
                if (validation) {
                    validateProcessing(current);
                    LOGGER.info("[ClarifierValidation] PASS ports, production-line connections, power pause/resume, consumption, persistence and outputs");
                }
            } catch (Exception exception) {
                LOGGER.error("[ClarifierValidation] FAIL", exception);
            } finally {
                if (validation)
                    current.server().halt(false);
            }
        });
    }

    private static void connect(TestWorldContext context, List<BlockPos> waypoints, Supplier<? extends PipeBlock<?, ?>> pipe) {
        for (BlockPos pos : context.runPipe(waypoints, pipe))
            context.overworld().updateNeighborsAt(pos, pipe.get());
    }

    private static void validateConnections(TestWorldContext context) {
        var clarifier = context.requireBlockEntity(0, 0, 0, ClarifierBlockEntity.class);
        check(clarifier.isFormed(), "Clarifier failed to form");
        BlockPos input = context.origin().offset(ClarifierBlockEntity.FLUID_INPUT_PORT);
        BlockPos output = context.origin().offset(ClarifierBlockEntity.FLUID_OUTPUT_PORT);
        BlockPos energy = context.origin().offset(ClarifierBlockEntity.ENERGY_PORT);
        BlockPos item = context.origin().offset(ClarifierBlockEntity.ITEM_OUTPUT_PORT);
        var inputStorage = TransferType.FLUID.lookup(context.overworld(), input, Direction.WEST);
        var outputStorage = TransferType.FLUID.lookup(context.overworld(), output, Direction.NORTH);
        var energyStorage = TransferType.ENERGY.lookup(context.overworld(), energy, Direction.WEST);
        check(inputStorage != null && inputStorage.supportsInsertion() && !inputStorage.supportsExtraction(), "Input port lookup");
        check(outputStorage != null && outputStorage.supportsExtraction() && !outputStorage.supportsInsertion(), "Output port lookup");
        check(energyStorage != null && energyStorage.supportsInsertion() && !energyStorage.supportsExtraction(), "Energy port lookup");
        check(TransferType.ITEM.lookup(context.overworld(), item, Direction.NORTH) != null, "Item output lookup");
        check(clarifier.getEnergyStorageForExternal(energy, Direction.EAST) == null, "Wrong energy face accepted");
        check(clarifier.getFluidStorageForExternal(input, Direction.EAST) == null, "Wrong input face accepted");
        check(clarifier.getFluidStorageForExternal(output, Direction.SOUTH) == null, "Wrong output face accepted");
        for (BlockPos pipe : List.of(input.west(), energy.west(), output.north())) {
            Direction side = pipe.equals(output.north()) ? Direction.SOUTH : Direction.EAST;
            check(context.overworld().getBlockState(pipe).getValue(PipeBlock.propertyFor(side))
                    == PipeBlock.ConnectorType.BLOCK, "Disconnected pipe at " + pipe);
        }
        LOGGER.info("Clarifier production-line validation passed at {}", context.origin());
    }

    private static void validateProcessing(TestWorldContext context) {
        var clarifier = context.requireBlockEntity(0, 0, 0, ClarifierBlockEntity.class);
        var dirty = ResourceVariant.of(ResourceTypes.FLUID,
                BuiltInRegistries.FLUID.wrapAsHolder(ModFluids.DIRTY_SODIUM_ALUMINATE.still().get()));
        StorageOperations.set(clarifier.getInputFluidTank(), dirty, FluidAmounts.BUCKET);
        StorageOperations.setEnergy(clarifier.getEnergyStorage(), 0);
        clarifier.onTick();
        int initialProgress = clarifier.getProgress();
        for (int i = 0; i < 5; i++) clarifier.onTick();
        check(clarifier.getMaxProgress() > 0 && clarifier.getProgress() == initialProgress
                && clarifier.getInputFluidTank().getAmount() == FluidAmounts.BUCKET, "Processed without power");
        try (var transaction = TransferTransaction.openRoot()) {
            var external = clarifier.getEnergyStorageForExternal(context.origin().offset(ClarifierBlockEntity.ENERGY_PORT), Direction.WEST);
            check(external.insert(SimpleEnergyStorage.ENERGY, 300, transaction) == 300, "Energy insertion rejected");
            check(!external.supportsExtraction(), "Energy input allows external extraction");
            transaction.commit();
        }
        for (int i = 0; i < 3; i++) clarifier.onTick();
        check(clarifier.getProgress() == initialProgress + 3 && clarifier.getEnergyStorage().getAmount() == 0, "Incorrect energy consumption");
        clarifier.onTick();
        check(clarifier.getProgress() == initialProgress + 3 && !clarifier.isRunning(), "Did not pause when power ran out");
        StorageOperations.setEnergy(clarifier.getEnergyStorage(), 100_000);
        var saved = clarifier.saveWithoutMetadata(context.overworld().registryAccess());
        StorageOperations.setEnergy(clarifier.getEnergyStorage(), 0);
        clarifier.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, context.overworld().registryAccess(), saved));
        check(clarifier.getEnergyStorage().getAmount() == 100_000 && clarifier.getProgress() == initialProgress + 3, "Energy or progress lost on reload");
        int remaining = clarifier.getMaxProgress() - clarifier.getProgress();
        for (int i = 0; i < remaining + 3; i++) clarifier.onTick();
        check(clarifier.getEnergyStorage().getAmount() == 100_000 - remaining * ClarifierBlockEntity.ENERGY_PER_TICK, "Incorrect batch energy cost");
        check(clarifier.getInputFluidTank().getAmount() == 0, "Input not consumed");
        check(clarifier.getOutputFluidTank().getResource().value() == ModFluids.SODIUM_ALUMINATE.still().get()
                && clarifier.getOutputFluidTank().getAmount() > 0, "Clean fluid missing");
        check(clarifier.getOutputInventory().getItem(0).is(ModItems.RED_MUD.get()), "Red mud missing");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
