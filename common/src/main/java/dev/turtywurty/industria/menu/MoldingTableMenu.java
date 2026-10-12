package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.MoldingTableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.item.MoldItem;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.molding.MoldPattern;
import dev.turtywurty.industria.molding.MoldPatterns;
import dev.turtywurty.industria.network.BlockPosPayload;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class MoldingTableMenu extends IndustriaScreenHandler<MoldingTableBlockEntity, BlockPosPayload> {
    public MoldingTableMenu(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ModMenuTypes.MOLDING_TABLE.get(), syncId, playerInventory, payload, MoldingTableBlockEntity.class);
    }

    public MoldingTableMenu(
        int syncId,
        Inventory playerInventory,
        MoldingTableBlockEntity blockEntity,
        WrappedContainerStorage<?> wrappedContainerStorage
    ) {
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

    public void completeMold(ServerPlayer player, Identifier moldId) {
        if (player.containerMenu != this || !stillValid(player))
            return;

        Optional<MoldPattern> pattern = MoldPatterns.find(moldId);
        Optional<MoldItem> moldItem = MoldItem.fromBlank(getSlot(0).getItem());
        if (pattern.isEmpty() || moldItem.isEmpty())
            return;

        List<ItemStack> results = moldItem.get().createStacks(pattern.get());
        getSlot(0).remove(1);
        for (ItemStack result : results) {
            player.getInventory().placeItemBackInInventory(result, Prediction.SERVER_ONLY);
        }

        getSlot(0).setChanged();
        broadcastChanges();
    }
}
