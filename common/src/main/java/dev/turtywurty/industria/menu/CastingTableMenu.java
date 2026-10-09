package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.CastingTableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModDataComponentTypes;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.item.MoldItem;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class CastingTableMenu extends IndustriaScreenHandler<CastingTableBlockEntity, BlockPosPayload> {
    public CastingTableMenu(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ModMenuTypes.CASTING_TABLE.get(), syncId, playerInventory, payload, CastingTableBlockEntity.class);
    }

    public CastingTableMenu(int syncId, Inventory playerInventory, CastingTableBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ModMenuTypes.CASTING_TABLE.get(), syncId, playerInventory, blockEntity, wrappedContainerStorage);
    }

    @Override
    protected int getInventorySize() {
        return 3;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SimpleContainer inputInventory = this.wrappedContainerStorage.getInventory(0);
        if(inputInventory != null) {
            for (int index = 0; index < 2; index++) {
                addSlot(new Slot(inputInventory, index, 44 + (index * 18), 35) {
                    @Override
                    public boolean mayPlace(ItemStack itemStack) {
                        return itemStack.getItem() instanceof MoldItem && itemStack.has(ModDataComponentTypes.MOLD.get());
                    }

                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }
                });
            }
        }

        SimpleContainer outputInventory = this.wrappedContainerStorage.getInventory(1);
        if(outputInventory != null) {
            addSlot(new Slot(outputInventory, 0, 134, 35) {
                @Override
                public boolean mayPlace(ItemStack itemStack) {
                    return false;
                }
            });
        }
    }

    @Override
    protected List<Block> getValidBlocks() {
        return List.of(ModBlocks.CASTING_TABLE.get());
    }
}
