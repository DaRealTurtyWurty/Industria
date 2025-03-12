package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.DigesterBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
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

public class DigesterScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private final DigesterBlockEntity blockEntity;
    private final PropertyDelegate properties;

    public DigesterScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory, (DigesterBlockEntity) playerInventory.player.getWorld().getBlockEntity(payload.pos()), new ArrayPropertyDelegate(2));
    }

    public DigesterScreenHandler(int syncId, PlayerInventory playerInventory, DigesterBlockEntity blockEntity, PropertyDelegate properties) {
        super(ScreenHandlerTypeInit.DIGESTER, syncId);
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());
        this.blockEntity = blockEntity;

        checkDataCount(properties, 2);

        WrappedInventoryStorage<?> wrappedInventoryStorage = blockEntity.getWrappedInventoryStorage();
        wrappedInventoryStorage.checkSize(2);
        wrappedInventoryStorage.onOpen(playerInventory.player);

        PredicateSimpleInventory inputFluidInventory = blockEntity.getInputSlurryInventory();
        PredicateSimpleInventory outputFluidInventory = blockEntity.getOutputFluidInventory();
        addSlot(new PredicateSlot(inputFluidInventory, 0, 26, 60));
        addSlot(new PredicateSlot(outputFluidInventory, 0, 134, 60));

        addPlayerSlots(playerInventory, 8, 92);

        addProperties(properties);
        this.properties = properties;
    }

    public DigesterBlockEntity getBlockEntity() {
        return this.blockEntity;
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

        if (slotIndex < 2) {
            if (!insertItem(stackInSlot, this.slots.size() - 9, this.slots.size(), true)) {
                if (!insertItem(stackInSlot, this.slots.size() - 36, this.slots.size() - 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (!insertItem(stackInSlot, 0, 2, false)) {
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
        return canUse(this.context, player, BlockInit.DIGESTER);
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
}