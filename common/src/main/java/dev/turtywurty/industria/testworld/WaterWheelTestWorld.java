package dev.turtywurty.industria.testworld;

import com.mojang.logging.LogUtils;
import dev.turtywurty.industria.block.GearboxBlock;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.block.WaterWheelBlock;
import dev.turtywurty.industria.blockentity.BatteryBlockEntity;
import dev.turtywurty.industria.blockentity.KineticDynamoBlockEntity;
import dev.turtywurty.industria.blockentity.WaterWheelBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.mechanical.MechanicalNetwork;
import dev.turtywurty.industria.mechanical.MechanicalNetworkManager;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class WaterWheelTestWorld {
    private WaterWheelTestWorld() {
    }

    public static void place(TestWorldContext context) {
        context.fillAir(-4, -2, -2, 4, 3, 4);
        context.fill(-4, -3, -2, 4, -3, 4, Blocks.STONE_BRICKS);

        // Contain the waterfall's runoff beneath the wheel.
        context.fill(-4, -2, -1, 4, -2, -1, Blocks.STONE_BRICKS);
        context.fill(-4, -2, 1, 4, -2, 1, Blocks.STONE_BRICKS);
        context.setBlock(-4, -2, 0, Blocks.STONE_BRICKS);
        context.setBlock(4, -2, 0, Blocks.STONE_BRICKS);

        // A glass-fronted chute falls beside the left edge; the wheel is its right wall.
        context.fill(-3, -1, 0, -3, 2, 0, Blocks.STONE_BRICKS);
        context.fill(-2, -1, 1, -2, 2, 1, Blocks.STONE_BRICKS);
        context.fill(-2, -1, -1, -2, 2, -1, Blocks.GLASS);
        context.setBlock(-1, 2, 0, Blocks.STONE_BRICKS);
        context.fill(-1, -2, 1, 1, -1, 3, Blocks.STONE_BRICKS);

        BlockState wheelState = ModBlocks.WATER_WHEEL.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        for (int part = 0; part <= 8; part++) {
            var offset = WaterWheelBlock.getPartOffset(part, Direction.NORTH);
            context.setBlock(offset.getX(), offset.getY(), offset.getZ(),
                wheelState.setValue(WaterWheelBlock.PART, part));
        }

        // The gearbox turns the wheel's southbound shaft east toward the dynamo.

        context.setBlock(0, 0, 1, ModBlocks.SHAFT.get().defaultBlockState()
            .setValue(BlockStateProperties.AXIS, Direction.Axis.Z));
        context.setBlock(0, 0, 2, ModBlocks.GEARBOX.get().defaultBlockState()
            .setValue(GearboxBlock.INPUT, Direction.NORTH)
            .setValue(GearboxBlock.OUTPUT, Direction.EAST));
        context.setBlock(1, 0, 2, ModBlocks.SHAFT.get().defaultBlockState()
            .setValue(BlockStateProperties.AXIS, Direction.Axis.X));
        context.setBlock(2, 0, 2, ModBlocks.KINETIC_DYNAMO.get().defaultBlockState()
            .setValue(BlockStateProperties.FACING, Direction.EAST));
        context.setBlock(4, 0, 2, ModBlocks.BASIC_BATTERY);
        context.configureBlockEntity(4, 0, 2, BatteryBlockEntity.class,
            battery -> battery.setChargeMode(BatteryBlockEntity.ChargeMode.CHARGE));
        context.runPipe(context.pos(3, 0, 2), context.pos(3, 0, 2), ModBlocks.CABLE);

        // Place one real source last and let vanilla fluid ticks establish the current.
        context.setBlock(-2, 2, 0, Blocks.WATER);
        LogUtils.getLogger().info(
            "Water wheel test fixture: wheel={}, gearbox={}, dynamo={}, charging battery={}, water source={}",
            context.origin(), context.pos(0, 0, 2), context.pos(2, 0, 2), context.pos(4, 0, 2), context.pos(-2, 2, 0));
    }

    public static void startValidation(TestWorldContext context) {
        int minChunkX = context.pos(-4, 0, -2).getX() >> 4;
        int maxChunkX = context.pos(4, 0, 4).getX() >> 4;
        int minChunkZ = context.pos(-4, 0, -2).getZ() >> 4;
        int maxChunkZ = context.pos(4, 0, 4).getZ() >> 4;
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                context.overworld().setChunkForced(x, z, true);
            }
        }

        place(context);
        BlockState initialCableState = context.overworld().getBlockState(context.pos(3, 0, 2));
        context.afterTicks(160, current -> {
            try {
                if (!initialCableState.is(ModBlocks.CABLE.get())
                    || initialCableState.getValue(PipeBlock.WEST) != PipeBlock.ConnectorType.BLOCK
                    || initialCableState.getValue(PipeBlock.EAST) != PipeBlock.ConnectorType.BLOCK)
                    throw new IllegalStateException(
                        "Cable was placed without both connector arms: " + initialCableState);

                WaterWheelBlockEntity wheel = current.requireBlockEntity(0, 0, 0, WaterWheelBlockEntity.class);
                current.requireBlockEntity(2, 0, 2, KineticDynamoBlockEntity.class);
                BatteryBlockEntity battery = current.requireBlockEntity(4, 0, 2, BatteryBlockEntity.class);
                double flow = WaterWheelBlockEntity.WaterWheelFlowSampler.sample(current.overworld(),
                    current.origin(), wheel.getBlockState()).signedTorque();
                MechanicalNetwork network = MechanicalNetworkManager.getOrCreate(current.overworld())
                    .getNetworkAt(current.origin());
                if (network == null || !network.getNodes().contains(current.pos(0, 0, 1))
                    || !network.getNodes().contains(current.pos(0, 0, 2))
                    || !network.getNodes().contains(current.pos(1, 0, 2))
                    || !network.getNodes().contains(current.pos(2, 0, 2)))
                    throw new IllegalStateException(
                        "Water wheel, shafts, gearbox, and dynamo did not join the same network");

                if (flow <= 0.0 || network.getState().rpm() <= 0.0 || battery.getEnergy().getAmount() <= 0)
                    throw new IllegalStateException("flow=" + flow + ", state=" + network.getState()
                        + ", stored energy=" + battery.getEnergy().getAmount());

                LogUtils.getLogger().info(
                    "[WaterWheelSuite] PASS gearbox transmission and cable connections, flow={}, RPM={}, battery energy={}",
                    flow, network.getState().rpm(), battery.getEnergy().getAmount());
            } catch (Throwable failure) {
                LogUtils.getLogger().error("[WaterWheelSuite] FAIL", failure);
            } finally {
                for (int x = minChunkX; x <= maxChunkX; x++) {
                    for (int z = minChunkZ; z <= maxChunkZ; z++) {
                        current.overworld().setChunkForced(x, z, false);
                    }
                }

                current.server().halt(false);
            }
        });
    }
}
