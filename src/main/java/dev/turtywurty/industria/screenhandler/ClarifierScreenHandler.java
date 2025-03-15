package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.ClarifierBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;

public class ClarifierScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private final ClarifierBlockEntity blockEntity;
    private final PropertyDelegate properties;

    public ClarifierScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (ClarifierBlockEntity) playerInv.player.getWorld().getBlockEntity(payload.pos()), new ArrayPropertyDelegate(2));
    }

    public ClarifierScreenHandler(int syncId, PlayerInventory playerInv, ClarifierBlockEntity blockEntity, PropertyDelegate properties) {
        super(ScreenHandlerTypeInit.CLARIFIER, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());

        checkDataCount(properties, 2);

        WrappedInventoryStorage<?> wrappedInventoryStorage = blockEntity.getWrappedInventoryStorage();
        wrappedInventoryStorage.checkSize(1);
        wrappedInventoryStorage.onOpen(playerInv.player);

        SimpleInventory inventory = wrappedInventoryStorage.getInventory(0);
        addSlot(new OutputSlot(inventory, 0, 134,60));

        addPlayerSlots(playerInv, 8, 84);

        this.properties = properties;
        addProperties(this.properties);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.blockEntity.getWrappedInventoryStorage().onClose(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack()) {
            return stack;
        }

        ItemStack stackInSlot = slot.getStack();
        stack = stackInSlot.copy();

        if (slotIndex == 0) {
            if (!insertItem(stackInSlot, this.slots.size() - 9, this.slots.size(), true)) {
                if (!insertItem(stackInSlot, this.slots.size() - 36, this.slots.size() - 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (!insertItem(stackInSlot, 0, 0, false)) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return stack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.CLARIFIER);
    }

    public int getProgress() {
        return this.properties.get(0);
    }

    public int getMaxProgress() {
        return this.properties.get(1);
    }

    public float getProgressPercent() {
        float progress = getProgress();
        float maxProgress = getMaxProgress();
        if (maxProgress == 0 || progress == 0)
            return 0.0F;

        return MathHelper.clamp(progress / maxProgress, 0.0F, 1.0F);
    }

    public int getProgressScaled() {
        return MathHelper.ceil(getProgressPercent() * 24);
    }

    public ClarifierBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
