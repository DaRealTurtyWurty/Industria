package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.UpgradeStationBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.ClientWrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.UpgradeStationOpenPayload;
import dev.turtywurty.industria.recipe.UpgradeStationRecipe;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// TODO: Figure out a way to still let this use IndustriaScreenHandler
public class UpgradeStationScreenHandler extends AbstractContainerMenu {
    private final List<UpgradeStationRecipe> recipes = new ArrayList<>();
    private final WrappedContainerStorage<?> wrappedContainerStorage;
    private final UpgradeStationBlockEntity blockEntity;
    private final ContainerLevelAccess context;
    private final ContainerData properties;

    private Runnable contentsChangedListener;

    public UpgradeStationScreenHandler(int syncId, Inventory inventory, UpgradeStationOpenPayload payload) {
        this(syncId,
                inventory,
                (UpgradeStationBlockEntity) inventory.player.level().getBlockEntity(payload.pos()),
                ClientWrappedContainerStorage.copyOf(((UpgradeStationBlockEntity) inventory.player.level().getBlockEntity(payload.pos())).getWrappedContainerStorage()),
                new SimpleContainerData(1),
                payload.recipes());
    }

    public UpgradeStationScreenHandler(int syncId, Inventory inventory, UpgradeStationBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage, ContainerData properties, List<UpgradeStationRecipe> recipes) {
        super(ScreenHandlerTypeInit.UPGRADE_STATION, syncId);

        this.blockEntity = blockEntity;
        this.context = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.wrappedContainerStorage = wrappedContainerStorage;

        wrappedContainerStorage.checkSize(10);
        wrappedContainerStorage.onOpen(inventory.player);

        addStandardInventorySlots(inventory, 18, 104);
        addBlockEntitySlots();

        checkContainerDataCount(properties, 1);
        addDataSlots(properties);
        this.properties = properties;

        this.recipes.addAll(recipes);
    }

    private void addBlockEntitySlots() {
        SimpleContainer inputInventory = this.wrappedContainerStorage.getInventory(0);
        if (inputInventory == null)
            throw new IllegalStateException("Input inventory is null");

        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                addSlot(new Slot(inputInventory, row + column * 3, 8 + row * 18, 17 + column * 18));
            }
        }

        addSlot(new OutputSlot(wrappedContainerStorage.getInventory(1), 0, 148, 35));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        // TODO: Confirm this is okay
        if (this.contentsChangedListener != null) {
            this.contentsChangedListener.run();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, BlockInit.UPGRADE_STATION);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.wrappedContainerStorage.onClose(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        int previousRecipeIndex = this.blockEntity.getSelectedRecipeIndex();
        this.blockEntity.setSelectedRecipeIndex(id);
        return this.blockEntity.getSelectedRecipeIndex() != previousRecipeIndex;
    }

    public UpgradeStationBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public int getAvailableRecipeCount() {
        return this.recipes.size();
    }

    public List<UpgradeStationRecipe> getAvailableRecipes() {
        return this.recipes;
    }

    public void setAvailableRecipes(List<UpgradeStationRecipe> recipes) {
        this.recipes.clear();
        this.recipes.addAll(recipes);
    }

    public void setContentsChangedListener(Runnable onInventoryChange) {
        this.contentsChangedListener = onInventoryChange;
    }

    public int getProgress() {
        return this.properties.get(0);
    }

    public boolean canCraft() {
        return !this.recipes.isEmpty() && getProgress() <= 0;
    }

    public int getSelectedRecipeIndex() {
        return this.blockEntity.getSelectedRecipeIndex();
    }
}
