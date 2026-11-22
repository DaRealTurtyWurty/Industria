package dev.turtywurty.industria.testworld;

import dev.turtywurty.industria.blockentity.BatteryBlockEntity;
import dev.turtywurty.industria.blockentity.WindMillBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class WindMillTestWorld {
    private WindMillTestWorld() {
    }

    public static void place(TestWorldContext context) {
        context.fillAir(-3, -3, 0, -1, 3, 0);
        context.fillAir(1, -3, 0, 3, 3, 0);
        context.fillAir(0, -3, 0, 0, -1, 0);
        context.fillAir(0, 1, 0, 0, 3, 0);
        context.fill(-3, -4, 0, 3, -4, 4, Blocks.STONE_BRICKS);
        // Brace the hub from behind to keep the rotor's swept area clear.
        context.fill(0, -3, 1, 0, -1, 4, Blocks.STONE_BRICKS);

        context.setBlock(0, 0, 0, ModBlocks.WIND_MILL.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        context.configureBlockEntity(0, 0, 0, WindMillBlockEntity.class, mill -> {
            var sail = new ItemStack(ModItems.WIND_MILL_SAIL.get());
            for (int slot = 0; slot < 4; slot++) {
                mill.installSail(sail);
            }
        });

        context.setBlock(0, 0, 1, ModBlocks.SHAFT.get().defaultBlockState()
            .setValue(BlockStateProperties.AXIS, Direction.Axis.Z));
        context.setBlock(0, 0, 2, ModBlocks.KINETIC_DYNAMO.get().defaultBlockState()
            .setValue(BlockStateProperties.FACING, Direction.SOUTH));
        context.setBlock(0, 0, 4, ModBlocks.BASIC_BATTERY);
        context.configureBlockEntity(0, 0, 4, BatteryBlockEntity.class,
            battery -> battery.setChargeMode(BatteryBlockEntity.ChargeMode.CHARGE));
        context.runPipe(context.pos(0, 0, 3), context.pos(0, 0, 3), ModBlocks.CABLE);
    }
}
