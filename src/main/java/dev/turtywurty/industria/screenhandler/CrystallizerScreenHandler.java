package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.CrystallizerBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;

public class CrystallizerScreenHandler extends ScreenHandler {
    private final CrystallizerBlockEntity blockEntity;
    private final ScreenHandlerContext context;
    private final PropertyDelegate properties;

    public CrystallizerScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (CrystallizerBlockEntity) playerInv.player.getWorld().getBlockEntity(payload.pos()), new ArrayPropertyDelegate(4));
    }

    public CrystallizerScreenHandler(int syncId, PlayerInventory playerInv, CrystallizerBlockEntity blockEntity, PropertyDelegate properties) {
        super(ScreenHandlerTypeInit.CRYSTALLIZER, syncId);
        this.blockEntity = blockEntity;
        checkDataCount(properties, 4);

        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());

        WrappedInventoryStorage<?> inventoryStorage = blockEntity.getWrappedInventoryStorage();
        inventoryStorage.checkSize(3);
        inventoryStorage.onOpen(playerInv.player);

        addSlot(new PredicateSlot(blockEntity.getCatalystInventory(), 0, 54, 12, $ -> !blockEntity.isRunning()));
        addSlot(new OutputSlot(blockEntity.getOutputInventory(), 0, 148, 27));
        addSlot(new OutputSlot(blockEntity.getByproductInventory(), 0, 148, 51));

        addPlayerSlots(playerInv, 8, 92);

        this.properties = properties;
        addProperties(this.properties);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        blockEntity.getWrappedInventoryStorage().onClose(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if(slot.hasStack()) {
            ItemStack slotStack = slot.getStack().copy();

            if(slotIndex < 3) {
                if(!insertItem(slotStack, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(slotStack, 0, 3, false)) {
                return ItemStack.EMPTY;
            }

            if(slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.CRYSTALLIZER);
    }

    public int getProgress() {
        return properties.get(0);
    }

    public int getMaxProgress() {
        return properties.get(1);
    }

    public float getProgressPercent() {
        float progress = getProgress();
        float maxProgress = getMaxProgress();
        if(maxProgress == 0 || progress == 0) {
            return 0.0f;
        }

        return MathHelper.clamp(progress / maxProgress, 0.0f, 1.0f);
    }

    public int getProgressScaled() {
        return (int) (getProgressPercent() * 24);
    }

    public int getCatalystUses() {
        return properties.get(2);
    }

    public int getMaxCatalystUses() {
        return properties.get(3);
    }

    public float getCatalystUsesPercent() {
        float uses = getCatalystUses();
        float maxUses = getMaxCatalystUses();
        if(maxUses == 0 || uses == 0) {
            return 0.0f;
        }

        return MathHelper.clamp(uses / maxUses, 0.0f, 1.0f);
    }

    public CrystallizerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
