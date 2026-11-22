package dev.turtywurty.industria.testworld;

import com.mojang.authlib.GameProfile;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.CokeOvenBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.multiblocklib.MultiblockLib;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import dev.turtywurty.industria.block.WindTurbineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import dev.turtywurty.multiblocklib.data.MultiblockDefinition;

public final class IndustriaTestWorldGenerator {
    private IndustriaTestWorldGenerator() {
    }

    public static void generate(TestWorldContext context) {
        if (Boolean.getBoolean("industria.test.casting.validation") && context.server().isDedicatedServer()) {
            CastingValidationTestWorld.start(context);
            return;
        }

        if (Boolean.getBoolean("industria.test.crusher.validation") && context.server().isDedicatedServer()) {
            CrusherValidationTestWorld.start(context);
            return;
        }

        if (Boolean.getBoolean("industria.test.waterWheel.validation") && context.server().isDedicatedServer()) {
            WaterWheelTestWorld.startValidation(context.at(7, 3, 7));
            return;
        }

        if (Boolean.getBoolean("industria.test.alloyFurnace.validation") && context.server().isDedicatedServer()) {
            AlloyFurnaceValidationTestWorld.start(context);
            return;
        }

        if (Boolean.getBoolean("industria.test.smeltery.validation") && context.server().isDedicatedServer()) {
            SmelteryValidationTestWorld.start(context);
            return;
        }

        if (Boolean.getBoolean("industria.test.smeltery.retention") && context.server().isDedicatedServer()) {
            SmelteryTestWorld.place(context.at(8, 8, -10));
            return;
        }

        TestWorldContext energyGenContext = context.at(0, 0, 5);
        WindTurbineBlock windTurbine = ModBlocks.WIND_TURBINE.get();
        BlockState windTurbineState = windTurbine.defaultBlockState();
        for (int x = -1; x <= 1; x++) {
            energyGenContext.setBlock(x, 0, 0, windTurbineState);
            windTurbine.setPlacedBy(energyGenContext.overworld(), energyGenContext.pos(x, 0, 0),
                windTurbineState, null, new ItemStack(windTurbine));
        }

        energyGenContext.setBlock(-3, 0, 1, ModBlocks.SOLAR_PANEL.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        energyGenContext.setBlock(-3, 0, -1, ModBlocks.ADVANCED_SOLAR_PANEL.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));

        TestWorldContext mixerContext = energyGenContext.at(-7, 0, -1);
        mixerContext.constructMultiblockPattern(0, 0, 0, MultiblockLib.DEFINITION_MANAGER.get(Industria.id("mixer")));

        ServerPlayer player = context.createFakePlayer(0, 0, 0, new GameProfile(UUID.randomUUID(), "FakePlayer"));
        mixerContext.useItemOnBlock(1, 0, 1, player, ModItems.WRENCH, InteractionHand.MAIN_HAND);

        TestWorldContext poolContext = energyGenContext.at(-10, 0, 0);
        poolContext.fill(-1, -2, -1, 1, -1, 1, Blocks.WATER);

        BlockState pumpState = ModBlocks.FLUID_PUMP.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        poolContext.setBlock(0, -2, 0, pumpState.setValue(BlockStateProperties.WATERLOGGED, true));
        poolContext.setBlock(0, -1, 0, pumpState
            .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)
            .setValue(BlockStateProperties.WATERLOGGED, true));

        for (int y = 0; y < 3; y++) {
            BlockPos oldPipe = poolContext.pos(2, y, 0);
            if (context.overworld().getBlockState(oldPipe).is(ModBlocks.FLUID_PIPE.get())) {
                context.overworld().removeBlock(oldPipe, false);
            }
        }

        TestWorldContext digesterContext = DigesterTestWorld.placeMachines(context, mixerContext, player);
        TestWorldContext clarifierContext = ClarifierTestWorld.placeMachines(digesterContext, player);
        TestWorldContext crystallizerContext = CrystallizerTestWorld.placeMachines(clarifierContext, player);
        addMixerItemFeed(mixerContext);

        connectMachines(energyGenContext, poolContext, mixerContext, digesterContext);
        ClarifierTestWorld.connectMachines(clarifierContext, digesterContext);
        CrystallizerTestWorld.connectMachines(crystallizerContext, clarifierContext, poolContext);
        SmelteryTestWorld.place(context.at(8, 0, -10));
        AlloyFurnaceTestWorld.place(context.at(4, 0, -10));
        TestWorldContext cokeOven = context.at(0, 1, -10);
        placeCokeOven(cokeOven, player);
        connectCokeOven(cokeOven);
        BlastFurnaceTestWorld.place(context.at(16, 0, -10));
        SteamBoilerTestWorld.place(context.at(21, 0, -10));
        WaterWheelTestWorld.place(context.at(7, 3, 7));
        WindMillTestWorld.place(context.at(17, 4, 7));
    }

    private static void placeCokeOven(TestWorldContext oven, ServerPlayer player) {
        MultiblockDefinition definition = MultiblockLib.DEFINITION_MANAGER.get(Industria.id("coke_oven"));
        BlockPos controller = definition.controllerPos();
        BlockPos worldPos = oven.origin().offset(controller);
        if (oven.overworld().getBlockEntity(worldPos) instanceof CokeOvenBlockEntity existing && existing.isFormed())
            return;

        oven.constructMultiblockPattern(0, 0, 0, definition);
        oven.useItemOnBlock(controller.getX(), controller.getY(), controller.getZ(), player, ModItems.WRENCH,
            InteractionHand.MAIN_HAND);
    }

    private static void connectCokeOven(TestWorldContext oven) {
        // The ingredient belt feeds a hopper above the oven's top port.
        oven.setBlock(1, 2, 1, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        placeCoalChest(oven, 1, 2, -2);
        placeConveyor(oven, 1, 2, -1, ModBlocks.FEEDER_CONVEYOR, Direction.SOUTH);
        placeConveyor(oven, 1, 2, 0, ModBlocks.HATCH_CONVEYOR, Direction.SOUTH);

        // The fuel belt inserts directly into the lower rear corner.
        placeCoalChest(oven, 0, 0, 7);
        placeConveyor(oven, 0, 0, 6, ModBlocks.FEEDER_CONVEYOR, Direction.NORTH);
        placeConveyor(oven, 0, 0, 5, ModBlocks.CONVEYOR, Direction.NORTH);
        placeConveyor(oven, 0, 0, 4, ModBlocks.HATCH_CONVEYOR, Direction.NORTH);

        // The lower center port drops into a hopper that hands coke off to the output belt.
        oven.setBlock(1, -1, 3, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.SOUTH));
        oven.setBlock(1, -1, 6, Blocks.CHEST);
        placeConveyor(oven, 1, -1, 4, ModBlocks.FEEDER_CONVEYOR, Direction.SOUTH);
        placeConveyor(oven, 1, -1, 5, ModBlocks.HATCH_CONVEYOR, Direction.SOUTH);

        oven.setBlock(1, 1, 6, ModBlocks.FLUID_TANK);
        oven.runPipe(oven.pos(1, 1, 4), oven.pos(1, 1, 5));
    }

    private static void placeCoalChest(TestWorldContext context, int x, int y, int z) {
        if (context.overworld().getBlockState(context.pos(x, y, z)).is(Blocks.CHEST))
            return;

        context.setBlock(x, y, z, Blocks.CHEST);
        context.configureBlockEntity(x, y, z, ChestBlockEntity.class,
            chest -> chest.setItem(0, new ItemStack(Items.COAL, 8)));
    }

    private static void placeConveyor(
        TestWorldContext context,
        int x,
        int y,
        int z,
        Supplier<? extends Block> conveyor,
        Direction facing
    ) {
        context.setBlock(x, y, z, conveyor.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
    }

    private static void connectMachines(
        TestWorldContext energyGenContext,
        TestWorldContext poolContext,
        TestWorldContext mixerContext,
        TestWorldContext digesterContext
    ) {
        energyGenContext.runPipe(List.of(
            energyGenContext.pos(-2, 0, 0),
            energyGenContext.pos(-4, 0, 0),
            energyGenContext.pos(-4, 0, 2),
            energyGenContext.pos(-4, 3, 2),
            energyGenContext.pos(-6, 3, 2),
            energyGenContext.pos(-6, 3, 0)), ModBlocks.CABLE);

        energyGenContext.runPipe(List.of(
            energyGenContext.pos(-4, 3, 2),
            poolContext.pos(0, 3, 2),
            poolContext.pos(0, 1, 2),
            poolContext.pos(0, 0, 2),
            poolContext.pos(0, 0, 0)), ModBlocks.CABLE);

        poolContext.placeBlockPath(
            List.of(
                poolContext.pos(1, -2, 0),
                poolContext.pos(1, -1, 0)),
            ModBlocks.FLUID_PIPE.get().defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, true));
        poolContext.runPipe(List.of(
            poolContext.pos(1, -2, 0),
            poolContext.pos(1, 3, 0),
            mixerContext.pos(0, 3, 1)));

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
