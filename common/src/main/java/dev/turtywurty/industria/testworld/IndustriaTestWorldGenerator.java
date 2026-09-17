package dev.turtywurty.industria.testworld;

import com.mojang.authlib.GameProfile;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.multiblocklib.MultiblockLib;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;
import java.util.UUID;

public final class IndustriaTestWorldGenerator {
    private IndustriaTestWorldGenerator() {
    }

    public static void generate(TestWorldContext context) {
        TestWorldContext energyGenContext = context.at(0, 0, 5);
        energyGenContext.setBlock(-1, 0, 0, ModBlocks.WIND_TURBINE);
        energyGenContext.setBlock(0, 0, 0, ModBlocks.WIND_TURBINE);
        energyGenContext.setBlock(1, 0, 0, ModBlocks.WIND_TURBINE);

        energyGenContext.setBlock(-3, 0, 1, ModBlocks.SOLAR_PANEL.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        energyGenContext.setBlock(-3, 0, -1, ModBlocks.ADVANCED_SOLAR_PANEL.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));

        TestWorldContext mixerContext = energyGenContext.at(-7, 0, -1);
        mixerContext.constructMultiblockPattern(0, 0, 0, MultiblockLib.DEFINITION_MANAGER.get(Industria.id("mixer")));

        ServerPlayer player = context.createFakePlayer(0, 0, 0, new GameProfile(UUID.randomUUID(), "FakePlayer"));
        mixerContext.useItemOnBlock(1, 0, 1, player, ModItems.WRENCH, InteractionHand.MAIN_HAND);

        TestWorldContext poolContext = energyGenContext.at(-10, 0, 0);
        poolContext.fill(-1, -2, -1, 1, -1, 1, Blocks.WATER);

        var pumpState = ModBlocks.FLUID_PUMP.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        poolContext.setBlock(0, -2, 0, pumpState.setValue(BlockStateProperties.WATERLOGGED, true));
        poolContext.setBlock(0, -1, 0, pumpState
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)
                .setValue(BlockStateProperties.WATERLOGGED, true));

        for (int y = 0; y < 3; y++) {
            var oldPipe = poolContext.pos(2, y, 0);
            if (context.overworld().getBlockState(oldPipe).is(ModBlocks.FLUID_PIPE.get()))
                context.overworld().removeBlock(oldPipe, false);
        }

        TestWorldContext digesterContext = DigesterTestWorld.placeMachines(context, mixerContext, player);
        addMixerItemFeed(mixerContext);

        connectMachines(energyGenContext, poolContext, mixerContext, digesterContext);
    }

    private static void connectMachines(TestWorldContext energyGenContext, TestWorldContext poolContext,
                                        TestWorldContext mixerContext, TestWorldContext digesterContext) {
        energyGenContext.runPipe(List.of(
                energyGenContext.pos(-2, 0, 0),
                energyGenContext.pos(-4, 0, 0),
                energyGenContext.pos(-4, 0, 2),
                energyGenContext.pos(-4, 3, 2),
                energyGenContext.pos(-6, 3, 2),
                energyGenContext.pos(-6, 3, 0)
        ), ModBlocks.CABLE);

        energyGenContext.runPipe(List.of(
                energyGenContext.pos(-4, 3, 2),
                poolContext.pos(0, 3, 2),
                poolContext.pos(0, 1, 2),
                poolContext.pos(0, 0, 2),
                poolContext.pos(0, 0, 0)
        ), ModBlocks.CABLE);

        poolContext.placeBlockPath(
                List.of(
                        poolContext.pos(1, -2, 0),
                        poolContext.pos(1, -1, 0)
                ),
                ModBlocks.FLUID_PIPE.get().defaultBlockState()
                        .setValue(BlockStateProperties.WATERLOGGED, true));
        poolContext.runPipe(List.of(
                poolContext.pos(1, -2, 0),
                poolContext.pos(1, 3, 0),
                mixerContext.pos(0, 3, 1)
        ));

        DigesterTestWorld.connectMachines(digesterContext, mixerContext, energyGenContext);
    }

    private static void addMixerItemFeed(TestWorldContext mixer) {
        // The controller is at (1, 0, 1); its item input is the top of (2, 2, 1).
        mixer.setBlock(2, 3, 1, Blocks.HOPPER.defaultBlockState()
                .setValue(HopperBlock.FACING, Direction.DOWN));
        for (int x = 3; x <= 4; x++) {
            mixer.setBlock(x, 3, 1, Blocks.HOPPER.defaultBlockState()
                    .setValue(HopperBlock.FACING, Direction.WEST));
        }

        mixer.setBlock(4, 4, 1, Blocks.CHEST);
        mixer.configureBlockEntity(4, 4, 1, ChestBlockEntity.class, chest -> {
            chest.clearContent();
            for (int batch = 0; batch < 6; batch++) {
                chest.setItem(batch * 2, new ItemStack(ModItems.BAUXITE.get(), 16));
                chest.setItem(batch * 2 + 1, new ItemStack(ModItems.SODIUM_HYDROXIDE.get(), 4));
            }
        });
    }
}
