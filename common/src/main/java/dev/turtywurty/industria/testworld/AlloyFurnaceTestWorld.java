package dev.turtywurty.industria.testworld;

import dev.turtywurty.industria.blockentity.AlloyFurnaceBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.turtywurty.industria.block.AlloyFurnaceBlock;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.state.BlockState;

public final class AlloyFurnaceTestWorld {
    private AlloyFurnaceTestWorld() {
    }

    public static void place(TestWorldContext context) {
        AlloyFurnaceBlock block = ModBlocks.ALLOY_FURNACE.get();
        BlockState state = block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        context.fillAir(0, 0, 0, 1, 2, 1);
        context.setBlock(0, 0, 0, state);
        block.setPlacedBy(context.overworld(), context.origin(), state, null, new ItemStack(block));

        context.configureBlockEntity(0, 0, 0, AlloyFurnaceBlockEntity.class, furnace -> {
            WrappedContainerStorage<SimpleContainer> storage = furnace.getWrappedContainerStorage();
            storage.getInventory(AlloyFurnaceBlockEntity.INPUT_SLOT_0).setItem(0,
                new ItemStack(Items.COPPER_INGOT, 48));
            storage.getInventory(AlloyFurnaceBlockEntity.INPUT_SLOT_1).setItem(0,
                new ItemStack(ModItems.TIN_INGOT.get(), 16));
            storage.getInventory(AlloyFurnaceBlockEntity.FUEL_SLOT).setItem(0, new ItemStack(Items.COAL, 16));
        });
    }
}
