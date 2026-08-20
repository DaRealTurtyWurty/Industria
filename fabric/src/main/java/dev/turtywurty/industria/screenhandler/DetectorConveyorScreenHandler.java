package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.conveyor.block.impl.entity.DetectorConveyorBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorBlacklistModePayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorFilterStackPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorFilterTagPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorMatchComponentsPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorMatchDurabilityPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorMatchEnchantmentsPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorTagFilteringPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class DetectorConveyorScreenHandler extends IndustriaScreenHandler<DetectorConveyorBlockEntity, BlockPosPayload> {
    public DetectorConveyorScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.DETECTOR_CONVEYOR, syncId, playerInventory, payload, DetectorConveyorBlockEntity.class);
    }

    public DetectorConveyorScreenHandler(int syncId, Inventory playerInventory, DetectorConveyorBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ScreenHandlerTypeInit.DETECTOR_CONVEYOR, syncId, playerInventory, blockEntity, wrappedContainerStorage);
    }

    @Override
    protected int getInventorySize() {
        return 0;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
    }

    @Override
    protected int getPlayerInventoryY() {
        return 108;
    }

    @Override
    protected List<Block> getValidBlocks() {
        return List.of(BlockInit.DETECTOR_CONVEYOR);
    }

    public void setFilterStack(ItemStack stack) {
        this.blockEntity.setFilterStack(stack);
        ClientPlayNetworking.send(new SetConveyorFilterStackPayload(stack));
    }

    public ItemStack getFilterStack() {
        return this.blockEntity.getFilterStack();
    }

    public void setBlacklistMode(boolean blacklistMode) {
        this.blockEntity.setBlacklistMode(blacklistMode);
        ClientPlayNetworking.send(new SetConveyorBlacklistModePayload(blacklistMode));
    }

    public boolean isBlacklistMode() {
        return this.blockEntity.isBlacklistMode();
    }

    public void setMatchDurability(boolean matchDurability) {
        this.blockEntity.setMatchDurability(matchDurability);
        ClientPlayNetworking.send(new SetConveyorMatchDurabilityPayload(matchDurability));
    }

    public boolean isMatchDurability() {
        return this.blockEntity.isMatchDurability();
    }

    public void setMatchEnchantments(boolean matchEnchantments) {
        this.blockEntity.setMatchEnchantments(matchEnchantments);
        ClientPlayNetworking.send(new SetConveyorMatchEnchantmentsPayload(matchEnchantments));
    }

    public boolean isMatchEnchantments() {
        return this.blockEntity.isMatchEnchantments();
    }

    public void setMatchComponents(boolean matchComponents) {
        this.blockEntity.setMatchComponents(matchComponents);
        ClientPlayNetworking.send(new SetConveyorMatchComponentsPayload(matchComponents));
    }

    public boolean isMatchComponents() {
        return this.blockEntity.isMatchComponents();
    }

    public void setFilterTag(TagKey<Item> filterTag) {
        this.blockEntity.setFilterTag(filterTag);
        ClientPlayNetworking.send(new SetConveyorFilterTagPayload(filterTag));
    }

    public TagKey<Item> getFilterTag() {
        return this.blockEntity.getFilterTag();
    }

    public void setTagFiltering(boolean tagFiltering) {
        this.blockEntity.setTagFiltering(tagFiltering);
        ClientPlayNetworking.send(new SetConveyorTagFilteringPayload(tagFiltering));
    }

    public boolean isTagFiltering() {
        return this.blockEntity.isTagFiltering();
    }
}
