package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.UpgradeStationRecipe;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.IndustriaIngredientPreviewWidget;
import dev.turtywurty.industria.screen.widget.SelectRecipeWidget;
import dev.turtywurty.industria.screenhandler.UpgradeStationScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class UpgradeStationScreen extends AbstractContainerScreen<UpgradeStationScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/upgrade_station.png");

    private SelectRecipeWidget<UpgradeStationRecipe> recipeSelector;
    private final List<IndustriaIngredientPreviewWidget<UpgradeStationRecipe>> ingredientWidgets = new ArrayList<>();
    private boolean isDirty = false;

    public UpgradeStationScreen(UpgradeStationScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 196, 186);
        handler.setContentsChangedListener(() -> this.isDirty = true);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        addRenderableOnly(new EnergyWidget.Builder(this.menu.getBlockEntity().getEnergyStorage())
                .bounds(this.leftPos + 168, this.topPos + 10, 20, 66)
                .color(0xFFD4AF37)
                .build());

        this.recipeSelector = addRenderableWidget(new SelectRecipeWidget.Builder<UpgradeStationRecipe>()
                .position(this.leftPos + 65, this.topPos + 15)
                .scrollBarPosition(this.leftPos + 132, this.topPos + 15)
                .recipes(this.menu.getAvailableRecipes())
                .canCraft(this.menu.canCraft())
                .selectedRecipeIndex(this.menu.getSelectedRecipeIndex())
                .onRecipeSelected((widget, index) -> {
                    if (this.minecraft == null || this.minecraft.gameMode == null)
                        return;

                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, index);
                    widget.setSelectedRecipeIndex(index);
                })
                .outputFunction(UpgradeStationRecipe::output)
                .columnCount(4)
                .rowCount(3)
                .lockable()
                .build());

        this.recipeSelector.addRecipeListener(recipe -> {
            for (IndustriaIngredientPreviewWidget<UpgradeStationRecipe> widget : this.ingredientWidgets) {
                widget.setRecipe(recipe);
            }
        });

        this.ingredientWidgets.clear();
        for (int index = 0; index < 9; index++) {
            if (index == 4)
                continue;

            final int xPos = this.leftPos + 8 + (index % 3 * 18);
            final int yPos = this.topPos + 17 + (index / 3 * 18);

            final int finalIndex = index;
            this.ingredientWidgets.add(
                    addRenderableOnly(new IndustriaIngredientPreviewWidget<>(xPos, yPos,
                            finalIndex,
                            theRecipe -> theRecipe.getIngredient(finalIndex))));
        }

        int selectedRecipeIndex = this.recipeSelector.getSelectedRecipeIndex();
        if (selectedRecipeIndex >= 0 && selectedRecipeIndex < this.menu.getAvailableRecipeCount()) {
            UpgradeStationRecipe recipe = this.recipeSelector.getSelectedRecipe();
            for (IndustriaIngredientPreviewWidget<UpgradeStationRecipe> widget : this.ingredientWidgets) {
                widget.setRecipe(recipe);
                widget.setShouldRender(!this.menu.getSlot(36 + widget.getSlotIndex()).hasItem());
            }
        }
    }

    @Override
    protected void containerTick() {
        if(this.isDirty) {
            if (this.recipeSelector != null) {
                this.recipeSelector.setCanCraft(this.menu.canCraft());
                if (!this.recipeSelector.canCraft()) {
                    this.recipeSelector.resetScroll();
                }

                this.recipeSelector.setRecipes(this.menu.getAvailableRecipes());

                for (IndustriaIngredientPreviewWidget<UpgradeStationRecipe> widget : this.ingredientWidgets) {
                    widget.setRecipe(this.recipeSelector.getSelectedRecipe());
                    widget.setShouldRender(!this.menu.getSlot(36 + widget.getSlotIndex()).hasItem());
                }
            }

            this.isDirty = false;
        }
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int progress = Mth.ceil((this.menu.getProgress() / 500f) * 147);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 25, this.topPos + 77, 0, 186, progress, 17);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if(!super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
        }

        return true;
    }
}
