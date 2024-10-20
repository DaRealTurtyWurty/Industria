package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.AlloyFurnaceBlockEntity;
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

public class AlloyFurnaceScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private final AlloyFurnaceBlockEntity blockEntity;
    private final PropertyDelegate propertyDelegate;

    public AlloyFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory, (AlloyFurnaceBlockEntity) playerInventory.player.getWorld().getBlockEntity(payload.pos()), new ArrayPropertyDelegate(4));
    }

    public AlloyFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, AlloyFurnaceBlockEntity blockEntity, PropertyDelegate propertyDelegate) {
        super(ScreenHandlerTypeInit.ALLOY_FURNACE, syncId);

        this.context = ScreenHandlerContext.create(playerInventory.player.getWorld(), blockEntity.getPos());
        this.blockEntity = blockEntity;
        this.propertyDelegate = propertyDelegate;

        WrappedInventoryStorage<SimpleInventory> inventory = blockEntity.getWrappedStorage();
        inventory.checkSize(4);
        checkDataCount(this.propertyDelegate, 4);

        inventory.onOpen(playerInventory.player);

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addBlockEntitySlots();

        addProperties(propertyDelegate);
    }

    private void addBlockEntitySlots() {
        WrappedInventoryStorage<SimpleInventory> inventory = this.blockEntity.getWrappedStorage();
        addSlot(new Slot(inventory.getInventory(AlloyFurnaceBlockEntity.INPUT_SLOT_0), 0, 42, 17));
        addSlot(new Slot(inventory.getInventory(AlloyFurnaceBlockEntity.INPUT_SLOT_1), 0, 70, 17));
        addSlot(new Slot(inventory.getInventory(AlloyFurnaceBlockEntity.FUEL_SLOT), 0, 56, 53) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return this.inventory.isValid(0, stack);
            }
        });
        addSlot(new OutputSlot(inventory.getInventory(AlloyFurnaceBlockEntity.OUTPUT_SLOT), 0, 116, 35));
    }

    private void addPlayerInventory(PlayerInventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory inventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.blockEntity.getWrappedStorage().onClose(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = this.slots.get(slot);

        SimpleInventory inputInventory = this.blockEntity.getInventory();

        if (slotObject.hasStack()) {
            ItemStack stackInSlot = slotObject.getStack();
            stack = stackInSlot.copy();

            if (slot < inputInventory.size()) {
                if (!insertItem(stackInSlot, inputInventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(stackInSlot, 0, inputInventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slotObject.setStack(ItemStack.EMPTY);
            } else {
                slotObject.markDirty();
            }
        }

        return stack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.ALLOY_FURNACE);
    }

    public int getProgress() {
        return this.propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return this.propertyDelegate.get(1);
    }

    public int getBurnTime() {
        return this.propertyDelegate.get(2);
    }

    public int getMaxBurnTime() {
        return this.propertyDelegate.get(3);
    }

    public float getProgressPercent() {
        float progress = getProgress();
        float maxProgress = getMaxProgress();
        if (maxProgress == 0 || progress == 0)
            return 0.0F;

        return MathHelper.clamp(progress / maxProgress, 0.0F, 1.0F);
    }

    public float getBurnTimePercent() {
        float progress = getBurnTime();
        float maxProgress = getMaxBurnTime();
        if (maxProgress == 0 || progress == 0)
            return 0.0F;

        return MathHelper.clamp(progress / maxProgress, 0.0F, 1.0F);
    }
}
