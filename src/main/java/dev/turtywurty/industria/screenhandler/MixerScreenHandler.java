package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.MixerBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
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

public class MixerScreenHandler extends ScreenHandler {
    private final MixerBlockEntity blockEntity;
    private final ScreenHandlerContext context;
    private final PropertyDelegate properties;

    public MixerScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory,
                (MixerBlockEntity) playerInventory.player.getWorld().getBlockEntity(payload.pos()),
                new ArrayPropertyDelegate(2));
    }

    public MixerScreenHandler(int syncId, PlayerInventory playerInventory, MixerBlockEntity blockEntity, PropertyDelegate properties) {
        super(ScreenHandlerTypeInit.MIXER, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());

        WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = blockEntity.getWrappedInventoryStorage();
        wrappedInventoryStorage.checkSize(9);
        wrappedInventoryStorage.onOpen(playerInventory.player);

        SyncingSimpleInventory inputInventory = blockEntity.getInputInventory();
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(inputInventory, column + (row * 3), 58 + column * 18, 26 + row * 18));
            }
        }

        addSlot(new Slot(blockEntity.getOutputInventory(), 0, 143, 35));

        SyncingSimpleInventory bucketInputInventory = blockEntity.getBucketInputInventory();
        SyncingSimpleInventory bucketOutputInventory = blockEntity.getBucketOutputInventory();
        addSlot(new PredicateSlot(bucketInputInventory, 0, 36, 83, stack -> bucketInputInventory.isValid(0, stack)));
        addSlot(new PredicateSlot(bucketOutputInventory, 0, 170, 83, stack -> bucketOutputInventory.isValid(0, stack)));

        addPlayerSlots(playerInventory, 20, 112);

        checkDataCount(properties, 2);
        addProperties(properties);

        this.properties = properties;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if(!slot.hasStack()) {
            return stack;
        }

        ItemStack stackInSlot = slot.getStack();
        stack = stackInSlot.copy();

        if(slotIndex < 9) {
            if(!insertItem(stackInSlot, this.slots.size() - 9, this.slots.size(), true)) {
                if(!insertItem(stackInSlot, this.slots.size() - 36, this.slots.size() - 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            if(!insertItem(stackInSlot, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return stack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        this.blockEntity.getWrappedInventoryStorage().onClose(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.MIXER);
    }

    public MixerBlockEntity getBlockEntity() {
        return this.blockEntity;
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
