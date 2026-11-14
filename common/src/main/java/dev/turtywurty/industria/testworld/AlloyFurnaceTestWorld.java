package dev.turtywurty.industria.testworld;

import dev.turtywurty.industria.blockentity.AlloyFurnaceBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class AlloyFurnaceTestWorld {
    private AlloyFurnaceTestWorld() {
    }

    public static void place(TestWorldContext context) {
        var block = ModBlocks.ALLOY_FURNACE.get();
        var state = block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        context.fillAir(0, 0, 0, 1, 2, 1);
        context.setBlock(0, 0, 0, state);
        block.setPlacedBy(context.overworld(), context.origin(), state, null, new ItemStack(block));

        context.configureBlockEntity(0, 0, 0, AlloyFurnaceBlockEntity.class, furnace -> {
            var storage = furnace.getWrappedContainerStorage();
            storage.getInventory(AlloyFurnaceBlockEntity.INPUT_SLOT_0).setItem(0, new ItemStack(Items.COPPER_INGOT, 48));
            storage.getInventory(AlloyFurnaceBlockEntity.INPUT_SLOT_1).setItem(0, new ItemStack(ModItems.TIN_INGOT.get(), 16));
            storage.getInventory(AlloyFurnaceBlockEntity.FUEL_SLOT).setItem(0, new ItemStack(Items.COAL, 16));
        });
    }
}
