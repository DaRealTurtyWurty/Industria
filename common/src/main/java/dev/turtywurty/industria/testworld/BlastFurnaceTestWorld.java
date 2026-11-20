package dev.turtywurty.industria.testworld;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.BlastFurnaceBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.multiblocklib.MultiblockLib;
import dev.turtywurty.multiblocklib.MultiblockPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BlastFurnaceTestWorld {
    private BlastFurnaceTestWorld() {
    }

    public static void place(TestWorldContext context) {
        var definition = MultiblockLib.DEFINITION_MANAGER.get(Industria.id("blast_furnace"));
        BlockPos controller = definition.controllerPos();
        BlockPos controllerPos = context.origin().offset(controller);
        var level = context.overworld();
        if (level.getBlockEntity(controllerPos) instanceof BlastFurnaceBlockEntity existing && existing.isFormed())
            return;

        context.constructMultiblockPattern(0, 0, 0, definition);
        MultiblockPlacement.tryForm(level, controllerPos, new ItemStack(ModItems.WRENCH.get()));
        var furnace = context.requireBlockEntity(
            controller.getX(), controller.getY(), controller.getZ(), BlastFurnaceBlockEntity.class);
        if (!furnace.isFormed())
            throw new IllegalStateException("Could not form the test-world blast furnace at " + controllerPos);

        Direction intoFurnace = furnace.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction blowerSide = intoFurnace.getCounterClockWise();
        BlockPos blowerPos = controllerPos.relative(intoFurnace).relative(blowerSide, 2);
        level.setBlockAndUpdate(blowerPos, ModBlocks.HOT_AIR_BLOWER.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, blowerSide.getOpposite()));

        // Keep the furnace idle so the player can load it and see a batch run in the menu.
        if (!level.getBlockState(context.pos(3, 0, -2)).is(Blocks.CHEST)) {
            context.setBlock(3, 0, -2, Blocks.CHEST);
            context.configureBlockEntity(3, 0, -2, ChestBlockEntity.class, chest -> {
                chest.setItem(0, new ItemStack(ModItems.CRUSHED_IRON.get(), 32));
                chest.setItem(1, new ItemStack(Items.CALCITE, 8));
                chest.setItem(2, new ItemStack(ModItems.COAL_COKE.get(), 4));
            });
        }
    }
}
