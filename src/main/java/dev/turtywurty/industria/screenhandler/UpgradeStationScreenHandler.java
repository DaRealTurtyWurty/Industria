package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.UpgradeStationBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.UpgradeStationOpenPayload;
import dev.turtywurty.industria.recipe.UpgradeStationRecipe;
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

import java.util.ArrayList;
import java.util.List;

public class UpgradeStationScreenHandler extends ScreenHandler {
    private final List<UpgradeStationRecipe> recipes = new ArrayList<>();
    private final UpgradeStationBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    private Runnable contentsChangedListener;

    private final PropertyDelegate properties;

    public UpgradeStationScreenHandler(int syncId, PlayerInventory inventory, UpgradeStationOpenPayload payload) {
        this(syncId, inventory, (UpgradeStationBlockEntity) inventory.player.getWorld().getBlockEntity(payload.pos()), new ArrayPropertyDelegate(1), payload.recipes());
    }

    public UpgradeStationScreenHandler(int syncId, PlayerInventory inventory, UpgradeStationBlockEntity blockEntity, PropertyDelegate properties, List<UpgradeStationRecipe> recipes) {
        super(ScreenHandlerTypeInit.UPGRADE_STATION, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());

        WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = blockEntity.getWrappedInventoryStorage();
        wrappedInventoryStorage.checkSize(10);
        wrappedInventoryStorage.onOpen(inventory.player);

        addPlayerSlots(inventory, 18, 104);
        addBlockEntityInventory(wrappedInventoryStorage);

        checkDataCount(properties, 1);
        addProperties(properties);
        this.properties = properties;

        this.recipes.addAll(recipes);
    }

    private void addBlockEntityInventory(WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage) {
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                addSlot(new Slot(wrappedInventoryStorage.getInventory(0), row + column * 3, 8 + row * 18, 17 + column * 18));
            }
        }

        addSlot(new OutputSlot(wrappedInventoryStorage.getInventory(1), 0, 148, 35));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.UPGRADE_STATION);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        this.blockEntity.getWrappedInventoryStorage().onClose(player);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
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

        if (this.contentsChangedListener != null) {
            this.contentsChangedListener.run();
        }
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
