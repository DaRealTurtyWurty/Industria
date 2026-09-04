package dev.turtywurty.industria.testworld;

import com.mojang.authlib.GameProfile;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.multiblocklib.MultiblockLib;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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

        energyGenContext.runPipe(List.of(
                energyGenContext.pos(-2, 0, 0),
                energyGenContext.pos(-4, 0, 0),
                energyGenContext.pos(-4, 3, 0),
                energyGenContext.pos(-6, 3, 0)
        ), ModBlocks.CABLE);

        energyGenContext.setBlock(-3, 0, 1, ModBlocks.SOLAR_PANEL.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        energyGenContext.setBlock(-3, 0, -1, ModBlocks.ADVANCED_SOLAR_PANEL.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));

        TestWorldContext mixerContext = energyGenContext.at(-7, 0, -1);
        mixerContext.constructMultiblockPattern(0, 0, 0, MultiblockLib.DEFINITION_MANAGER.get(Industria.id("mixer")));

        ServerPlayer player = context.createFakePlayer(0, 0, 0, new GameProfile(UUID.randomUUID(), "FakePlayer"));
        mixerContext.useItemOnBlock(1, 0, 1, player, ModItems.WRENCH, InteractionHand.MAIN_HAND);
    }
}
