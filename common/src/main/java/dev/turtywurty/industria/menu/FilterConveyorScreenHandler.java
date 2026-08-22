package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.conveyor.block.impl.entity.FilterConveyorBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.network.conveyor.*;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class FilterConveyorScreenHandler extends IndustriaScreenHandler<FilterConveyorBlockEntity, BlockPosPayload> {
    private static final NetworkService NETWORK = NetworkService.get();

    public FilterConveyorScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ModMenuTypes.FILTER_CONVEYOR.get(), syncId, playerInventory, payload, FilterConveyorBlockEntity.class);
    }

    public FilterConveyorScreenHandler(int syncId, Inventory playerInventory, FilterConveyorBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ModMenuTypes.FILTER_CONVEYOR.get(), syncId, playerInventory, blockEntity, wrappedContainerStorage);
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
        return List.of(ModBlocks.FILTER_CONVEYOR.get());
    }

    public void setFilterStack(ItemStack stack) {
        this.blockEntity.setFilterStack(stack);
        NETWORK.sendToServer(new SetConveyorFilterStackPayload(stack));
    }

    public ItemStack getFilterStack() {
        return this.blockEntity.getFilterStack();
    }

    public void setBlacklistMode(boolean blacklistMode) {
        this.blockEntity.setBlacklistMode(blacklistMode);
        NETWORK.sendToServer(new SetConveyorBlacklistModePayload(blacklistMode));
    }

    public boolean isBlacklistMode() {
        return this.blockEntity.isBlacklistMode();
    }

    public void setMatchDurability(boolean matchDurability) {
        this.blockEntity.setMatchDurability(matchDurability);
        NETWORK.sendToServer(new SetConveyorMatchDurabilityPayload(matchDurability));
    }

    public boolean isMatchDurability() {
        return this.blockEntity.isMatchDurability();
    }

    public void setMatchEnchantments(boolean matchEnchantments) {
        this.blockEntity.setMatchEnchantments(matchEnchantments);
        NETWORK.sendToServer(new SetConveyorMatchEnchantmentsPayload(matchEnchantments));
    }

    public boolean isMatchEnchantments() {
        return this.blockEntity.isMatchEnchantments();
    }

    public void setMatchComponents(boolean matchComponents) {
        this.blockEntity.setMatchComponents(matchComponents);
        NETWORK.sendToServer(new SetConveyorMatchComponentsPayload(matchComponents));
    }

    public boolean isMatchComponents() {
        return this.blockEntity.isMatchComponents();
    }

    public void setFilterTag(TagKey<Item> filterTag) {
        this.blockEntity.setFilterTag(filterTag);
        NETWORK.sendToServer(new SetConveyorFilterTagPayload(filterTag));
    }

    public TagKey<Item> getFilterTag() {
        return this.blockEntity.getFilterTag();
    }

    public void setTagFiltering(boolean tagFiltering) {
        this.blockEntity.setTagFiltering(tagFiltering);
        NETWORK.sendToServer(new SetConveyorTagFilteringPayload(tagFiltering));
    }

    public boolean isTagFiltering() {
        return this.blockEntity.isTagFiltering();
    }
}
