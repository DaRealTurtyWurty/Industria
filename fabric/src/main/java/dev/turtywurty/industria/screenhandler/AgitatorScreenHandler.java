package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.AgitatorBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.AgitatorSetPortModePayload;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import dev.turtywurty.industria.util.AgitatorPortType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class AgitatorScreenHandler extends IndustriaScreenHandler<AgitatorBlockEntity, BlockPosPayload> {
    public AgitatorScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.AGITATOR, 2, syncId, playerInventory, payload, AgitatorBlockEntity.class);
    }

    public AgitatorScreenHandler(int syncId, Inventory playerInventory, AgitatorBlockEntity blockEntity,
                                 WrappedContainerStorage<?> wrappedContainerStorage, ContainerData propertyDelegate) {
        super(ScreenHandlerTypeInit.AGITATOR, syncId, playerInventory, blockEntity, wrappedContainerStorage, propertyDelegate);
    }

    @Override
    protected int getInventorySize() {
        return 5;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        addSlot(new InputModeSlot(this.wrappedContainerStorage.getInventory(0), 0, 14, 42, this.blockEntity, 0));
        addSlot(new InputModeSlot(this.wrappedContainerStorage.getInventory(1), 0, 46, 42, this.blockEntity, 1));
        addSlot(new InputModeSlot(this.wrappedContainerStorage.getInventory(2), 0, 78, 42, this.blockEntity, 2));
        addSlot(new OutputModeSlot(this.wrappedContainerStorage.getInventory(3), 0, 132, 42, this.blockEntity, 0));
        addSlot(new OutputModeSlot(this.wrappedContainerStorage.getInventory(4), 0, 154, 42, this.blockEntity, 1));
    }

    @Override
    protected int getPlayerInventoryY() {
        return 119;
    }

    @Override
    protected List<Block> getValidBlocks() {
        return List.of(BlockInit.AGITATOR);
    }

    public AgitatorPortType getInputMode(int index) {
        return this.blockEntity.getInputMode(index);
    }

    public AgitatorPortType getOutputMode(int index) {
        return this.blockEntity.getOutputMode(index);
    }

    public void setInputMode(int index, AgitatorPortType portType) {
        if (this.blockEntity.setInputMode(index, portType)) {
            ClientPlayNetworking.send(new AgitatorSetPortModePayload(false, index, portType));
        }
    }

    public void setOutputMode(int index, AgitatorPortType portType) {
        if (this.blockEntity.setOutputMode(index, portType)) {
            ClientPlayNetworking.send(new AgitatorSetPortModePayload(true, index, portType));
        }
    }

    public int getProgress() {
        return this.propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return this.propertyDelegate.get(1);
    }

    private static class InputModeSlot extends Slot {
        private final AgitatorBlockEntity blockEntity;
        private final int portIndex;

        public InputModeSlot(SimpleContainer container, int slot, int x, int y, AgitatorBlockEntity blockEntity, int portIndex) {
            super(container, slot, x, y);
            this.blockEntity = blockEntity;
            this.portIndex = portIndex;
        }

        @Override
        public boolean mayPlace(net.minecraft.world.item.ItemStack stack) {
            return isActive();
        }

        @Override
        public boolean isActive() {
            return this.blockEntity.getInputMode(this.portIndex) == AgitatorPortType.ITEM;
        }
    }

    private static class OutputModeSlot extends OutputSlot {
        private final AgitatorBlockEntity blockEntity;
        private final int portIndex;

        public OutputModeSlot(SimpleContainer inventory, int index, int x, int y, AgitatorBlockEntity blockEntity, int portIndex) {
            super(inventory, index, x, y);
            this.blockEntity = blockEntity;
            this.portIndex = portIndex;
        }

        @Override
        public boolean isActive() {
            return this.blockEntity.getOutputMode(this.portIndex) == AgitatorPortType.ITEM;
        }
    }
}
