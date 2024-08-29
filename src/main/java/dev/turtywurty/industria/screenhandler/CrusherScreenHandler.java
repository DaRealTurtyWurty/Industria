package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.CrusherBlockEntity;
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

public class CrusherScreenHandler extends ScreenHandler {
    private final CrusherBlockEntity blockEntity;
    private final ScreenHandlerContext context;
    private final PropertyDelegate propertyDelegate;

    public CrusherScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (CrusherBlockEntity) playerInv.player.getWorld().getBlockEntity(payload.pos()), new ArrayPropertyDelegate(2));
    }

    public CrusherScreenHandler(int syncId, PlayerInventory playerInv, CrusherBlockEntity blockEntity, PropertyDelegate propertyDelegate) {
        super(ScreenHandlerTypeInit.CRUSHER, syncId);
        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());
        this.propertyDelegate = propertyDelegate;

        WrappedInventoryStorage<SimpleInventory> wrappedStorage = blockEntity.getWrappedInventoryStorage();
        wrappedStorage.checkSize(3);
        wrappedStorage.onOpen(playerInv.player);
        checkDataCount(propertyDelegate, 2);

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
        addBlockEntityInventory();

        addProperties(propertyDelegate);
    }

    private void addPlayerInventory(PlayerInventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
    }

    private void addBlockEntityInventory() {
        WrappedInventoryStorage<SimpleInventory> wrappedStorage = this.blockEntity.getWrappedInventoryStorage();
        addSlot(new Slot(wrappedStorage.getInventory(CrusherBlockEntity.INPUT_SLOT), 0, 44, 35));
        addSlot(new OutputSlot(wrappedStorage.getInventory(CrusherBlockEntity.OUTPUT_SLOT), 0, 98, 35));
        addSlot(new OutputSlot(wrappedStorage.getInventory(CrusherBlockEntity.OUTPUT_SLOT), 1, 116, 35));
    }

    private void addPlayerHotbar(PlayerInventory inventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
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
        return canUse(this.context, player, BlockInit.CRUSHER);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.blockEntity.getWrappedInventoryStorage().onClose(player);
    }

    public int getProgress() {
        return this.propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return this.propertyDelegate.get(1);
    }

    public float getProgressPercent() {
        float progress = getProgress();
        float maxProgress = getMaxProgress();
        if(maxProgress == 0 || progress == 0) {
            return 0.0f;
        }

        return MathHelper.clamp(progress / maxProgress, 0.0f, 1.0f);
    }

    public long getEnergy() {
        return this.blockEntity.getEnergy().getAmount();
    }

    public long getMaxEnergy() {
        return this.blockEntity.getEnergy().getCapacity();
    }

    public float getEnergyPercent() {
        long energy = getEnergy();
        long maxEnergy = getMaxEnergy();
        if(maxEnergy == 0 || energy == 0) {
            return 0.0f;
        }

        return MathHelper.clamp(energy / (float) maxEnergy, 0.0f, 1.0f);
    }

    public CrusherBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
