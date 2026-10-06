package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.MoldingTableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class MoldingTableMenu extends IndustriaScreenHandler<MoldingTableBlockEntity, BlockPosPayload> {
    public MoldingTableMenu(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ModMenuTypes.MOLDING_TABLE.get(), syncId, playerInventory, payload, MoldingTableBlockEntity.class);
    }

    public MoldingTableMenu(int syncId, Inventory playerInventory, MoldingTableBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ModMenuTypes.MOLDING_TABLE.get(), syncId, playerInventory, blockEntity, wrappedContainerStorage);
    }

    @Override
    protected int getInventorySize() {
        return 1;
    }

    @Override
    protected int getPlayerInventoryY() {
        return 132;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        addSlot(new Slot(this.wrappedContainerStorage.getInventory(0), 0, 8, 59) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(TagList.Items.BLANK_MOLDS);
            }

            @Override
            public int getMaxStackSize(ItemStack itemStack) {
                return 1;
            }
        });
    }

    @Override
    protected List<Block> getValidBlocks() {
        return List.of(ModBlocks.MOLDING_TABLE.get());
    }
}
