package dev.turtywurty.industria.testworld;

import com.mojang.logging.LogUtils;
import dev.turtywurty.industria.blockentity.SmelteryControllerBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.Logger;

/** A single chamber with removable attachments and spare parts for manual formation tests. */
public final class SmelteryTestWorld {
    private static final Logger LOGGER = LogUtils.getLogger();

    private SmelteryTestWorld() {
    }

    public static void place(TestWorldContext context) {
        // Local coordinates: bottom Y=0, enclosed fuel Y=1, floor Y=2,
        // 3x2x3 interior at (1,3,1)..(3,4,3), roof Y=5, chimney Y=6.
        context.fill(0, 0, 0, 4, 5, 4, ModBlocks.FIREBRICK.get());
        context.fillAir(1, 1, 1, 3, 1, 3);
        context.fillAir(1, 3, 1, 3, 4, 3);

        context.setBlock(0, 1, 2, ModBlocks.FIREBOX.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
        context.setBlock(2, 3, 0, ModBlocks.CHARGING_HATCH.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        context.setBlock(2, 6, 2, ModBlocks.CHIMNEY);
        context.setBlock(5, 3, 2, ModBlocks.TAPPING_OUTLET.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        context.setBlock(5, 2, 2, ModBlocks.CASTING_BASIN.get());

        // Embed the controller in the north wall beside the charging hatch.
        context.setBlock(1, 3, 0, ModBlocks.SMELTERY_CONTROLLER.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));

        LOGGER.info("Smeltery test fixture: controller={}, hatch={}, firebox={}, chimney={}, outlet={}, basin={}, spare parts={}",
                context.pos(1, 3, 0), context.pos(2, 3, 0), context.pos(0, 1, 2),
                context.pos(2, 6, 2), context.pos(5, 3, 2), context.pos(5, 2, 2), context.pos(2, 0, -3));

        context.afterTicks(40, current -> {
            if (!(current.overworld().getBlockEntity(current.pos(1, 3, 0)) instanceof SmelteryControllerBlockEntity)) {
                LOGGER.warn("Smeltery test controller at {} has no SmelteryControllerBlockEntity; "
                                + "configure its block entity and ticker before testing formation.",
                        current.pos(1, 3, 0));
            }
        });

        if (Boolean.getBoolean("industria.test.smeltery.retention")) {
            // No players are present in the headless run; keep the fixture's block entities ticking.
            context.overworld().setChunkForced(context.origin().getX() >> 4, context.origin().getZ() >> 4, true);
            context.afterTicks(60, current -> runTestStep(current, "seed fluid", () -> seedFluid(current)));
        }
    }

    private static final long TEST_AMOUNT = FluidAmounts.BUCKET * 7 + FluidAmounts.BUCKET / 2;

    private static void seedFluid(TestWorldContext context) {
        var controller = context.requireBlockEntity(1, 3, 0, SmelteryControllerBlockEntity.class);
        CompoundTag data = controller.saveWithoutMetadata(context.overworld().registryAccess());
        check(data.getListOrEmpty("Chambers").size() == 1, "Expected one formed chamber before seeding");
        check(data.getListOrEmpty("RetainedFluids").isEmpty(), "Expected no initial retained fluid");
        var water = new FluidStack(ResourceTypes.FLUID.of(Fluids.WATER.builtInRegistryHolder()), TEST_AMOUNT);
        var ops = context.overworld().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var chamber = (CompoundTag) data.getListOrEmpty("Chambers").getFirst();
        check(chamber.getLongOr("capacity", 0) >= TEST_AMOUNT, "Fixture cannot hold test fluid");
        chamber.put("fluid", FluidStack.CODEC.codec().encodeStart(ops, water).getOrThrow());
        controller.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                context.overworld().registryAccess(), data));
        controller.setChanged();
        verifyFluid(context, 1, 0, "seeded");
        context.removeBlock(2, 6, 2);
        context.afterTicks(40, current -> runTestStep(current, "broken chamber", () -> {
            verifyFluid(current, 0, 1, "chimney removed");
            var entity = current.requireBlockEntity(1, 3, 0, SmelteryControllerBlockEntity.class);
            CompoundTag saved = entity.saveWithoutMetadata(current.overworld().registryAccess());
            entity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    current.overworld().registryAccess(), saved));
            verifyFluid(current, 0, 1, "retained fluid save/load round trip");
            current.afterTicks(40, waiting -> runTestStep(waiting, "repeat broken scan", () -> {
                verifyFluid(waiting, 0, 1, "repeated scan without duplication");
                waiting.setBlock(2, 6, 2, ModBlocks.CHIMNEY);
                waiting.afterTicks(40, repaired -> runTestStep(repaired, "repair", () -> {
                    verifyFluid(repaired, 1, 0, "chimney restored");
                    repaired.afterTicks(40, stable -> runTestStep(stable, "stable repaired scan", () -> {
                        verifyFluid(stable, 1, 0, "repeated repaired scan without duplication");
                        LOGGER.info("[SmelteryRetentionTest] PASS: {} units of water retained, serialized, and restored", TEST_AMOUNT);
                        stopTestServer(stable);
                    }));
                }));
            }));
        }));
    }

    private static void verifyFluid(TestWorldContext context, int activeCount, int retainedCount, String stage) {
        var entity = context.requireBlockEntity(1, 3, 0, SmelteryControllerBlockEntity.class);
        CompoundTag data = entity.saveWithoutMetadata(context.overworld().registryAccess());
        var active = data.getListOrEmpty("Chambers");
        var retained = data.getListOrEmpty("RetainedFluids");
        check(active.size() == activeCount, stage + ": wrong active chamber count " + active.size());
        check(retained.size() == retainedCount, stage + ": wrong retained record count " + retained.size());
        var ops = context.overworld().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        long total = 0;
        for (var entry : active) {
            var chamber = (CompoundTag) entry;
            var fluid = FluidStack.CODEC.codec().parse(ops, chamber.getCompoundOrEmpty("fluid")).getOrThrow();
            check(fluid.variant().value() == Fluids.WATER, stage + ": active fluid type changed");
            check(fluid.amount() <= chamber.getLongOr("capacity", 0), stage + ": active fluid exceeds capacity");
            total += fluid.amount();
        }
        for (var entry : retained) {
            var fluid = FluidStack.CODEC.codec().parse(ops,
                    ((CompoundTag) entry).getCompoundOrEmpty("fluid_stack")).getOrThrow();
            check(fluid.variant().value() == Fluids.WATER, stage + ": retained fluid type changed");
            total += fluid.amount();
        }
        check(total == TEST_AMOUNT, stage + ": expected " + TEST_AMOUNT + " fluid units, got " + total);
        LOGGER.info("[SmelteryRetentionTest] {}: active={} retained={} total={}", stage, active.size(), retained.size(), total);
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }

    private static void runTestStep(TestWorldContext context, String stage, Runnable action) {
        try {
            action.run();
        } catch (Exception exception) {
            context.setBlock(2, 6, 2, ModBlocks.CHIMNEY);
            LOGGER.error("[SmelteryRetentionTest] FAIL at " + stage, exception);
            stopTestServer(context);
        }
    }

    private static void stopTestServer(TestWorldContext context) {
        context.overworld().setChunkForced(context.origin().getX() >> 4, context.origin().getZ() >> 4, false);
        if (Boolean.getBoolean("industria.test.smeltery.stopAfterTest") && context.server().isDedicatedServer())
            context.server().halt(false);
    }
}
