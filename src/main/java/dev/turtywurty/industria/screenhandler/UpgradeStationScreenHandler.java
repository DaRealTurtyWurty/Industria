package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.UpgradeStationBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.ClientWrappedInventoryStorage;
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

// TODO: Figure out a way to still let this use IndustriaScreenHandler
public class UpgradeStationScreenHandler extends ScreenHandler {
    private final List<UpgradeStationRecipe> recipes = new ArrayList<>();
    private final WrappedInventoryStorage<?> wrappedInventoryStorage;
    private final UpgradeStationBlockEntity blockEntity;
    private final ScreenHandlerContext context;
    private final PropertyDelegate properties;

    private Runnable contentsChangedListener;

    public UpgradeStationScreenHandler(int syncId, PlayerInventory inventory, UpgradeStationOpenPayload payload) {
        this(syncId,
                inventory,
                (UpgradeStationBlockEntity) inventory.player.getEntityWorld().getBlockEntity(payload.pos()),
                ClientWrappedInventoryStorage.copyOf(((UpgradeStationBlockEntity) inventory.player.getEntityWorld().getBlockEntity(payload.pos())).getWrappedInventoryStorage()),
                new ArrayPropertyDelegate(1),
                payload.recipes());
    }

    public UpgradeStationScreenHandler(int syncId, PlayerInventory inventory, UpgradeStationBlockEntity blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage, PropertyDelegate properties, List<UpgradeStationRecipe> recipes) {
        super(ScreenHandlerTypeInit.UPGRADE_STATION, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());
        this.wrappedInventoryStorage = wrappedInventoryStorage;

        wrappedInventoryStorage.checkSize(10);
        wrappedInventoryStorage.onOpen(inventory.player);

        addPlayerSlots(inventory, 18, 104);
        addBlockEntitySlots();

        checkDataCount(properties, 1);
        addProperties(properties);
        this.properties = properties;

        this.recipes.addAll(recipes);
    }

    private void addBlockEntitySlots() {
        SimpleInventory inputInventory = this.wrappedInventoryStorage.getInventory(0);
        if (inputInventory == null)
            throw new IllegalStateException("Input inventory is null");

        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                addSlot(new Slot(inputInventory, row + column * 3, 8 + row * 18, 17 + column * 18));
            }
        }

        inputInventory.addListener(sender -> {
            if (this.contentsChangedListener != null) {
                this.contentsChangedListener.run();
            }
        });

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
        this.wrappedInventoryStorage.onClose(player);
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
