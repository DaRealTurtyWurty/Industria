package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.UpgradeStationRecipe;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.IndustriaIngredientPreviewWidget;
import dev.turtywurty.industria.screen.widget.SelectRecipeWidget;
import dev.turtywurty.industria.screenhandler.UpgradeStationScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class UpgradeStationScreen extends HandledScreen<UpgradeStationScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/upgrade_station.png");

    private SelectRecipeWidget<UpgradeStationRecipe> recipeSelector;
    private final List<IndustriaIngredientPreviewWidget<UpgradeStationRecipe>> ingredientWidgets = new ArrayList<>();
    private boolean isDirty = false;

    public UpgradeStationScreen(UpgradeStationScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        handler.setContentsChangedListener(() -> this.isDirty = true);

        this.backgroundWidth = 196;
        this.backgroundHeight = 186;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        addDrawable(new EnergyWidget.Builder(this.handler.getBlockEntity().getEnergyStorage())
                .bounds(this.x + 168, this.y + 10, 20, 66)
                .color(0xFFD4AF37)
                .build());

        this.recipeSelector = addDrawableChild(new SelectRecipeWidget.Builder<UpgradeStationRecipe>()
                .position(this.x + 65, this.y + 15)
                .scrollBarPosition(this.x + 132, this.y + 15)
                .recipes(this.handler.getAvailableRecipes())
                .canCraft(this.handler.canCraft())
                .selectedRecipeIndex(this.handler.getSelectedRecipeIndex())
                .onRecipeSelected((widget, index) -> {
                    if (this.client == null || this.client.interactionManager == null)
                        return;

                    this.client.interactionManager.clickButton(this.handler.syncId, index);
                    widget.setSelectedRecipeIndex(index);
                })
                .outputFunction(UpgradeStationRecipe::output)
                .columnCount(4)
                .rowCount(3)
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

            final int xPos = this.x + 8 + (index % 3 * 18);
            final int yPos = this.y + 17 + (index / 3 * 18);

            final int finalIndex = index;
            this.ingredientWidgets.add(
                    addDrawable(new IndustriaIngredientPreviewWidget<>(xPos, yPos,
                            finalIndex,
                            theRecipe -> theRecipe.getIngredient(finalIndex))));
        }

        int selectedRecipeIndex = this.recipeSelector.getSelectedRecipeIndex();
        if (selectedRecipeIndex >= 0 && selectedRecipeIndex < this.handler.getAvailableRecipeCount()) {
            UpgradeStationRecipe recipe = this.recipeSelector.getSelectedRecipe();
            for (IndustriaIngredientPreviewWidget<UpgradeStationRecipe> widget : this.ingredientWidgets) {
                widget.setRecipe(recipe);
                widget.setShouldRender(!this.handler.getSlot(36 + widget.getSlotIndex()).hasStack());
            }
        }
    }

    @Override
    protected void handledScreenTick() {
        if(this.isDirty) {
            if (this.recipeSelector != null) {
                this.recipeSelector.setCanCraft(this.handler.canCraft());
                if (!this.recipeSelector.canCraft()) {
                    this.recipeSelector.resetScroll();
                }

                this.recipeSelector.setRecipes(this.handler.getAvailableRecipes());

                for (IndustriaIngredientPreviewWidget<UpgradeStationRecipe> widget : this.ingredientWidgets) {
                    widget.setRecipe(this.recipeSelector.getSelectedRecipe());
                    widget.setShouldRender(!this.handler.getSlot(36 + widget.getSlotIndex()).hasStack());
                }
            }

            this.isDirty = false;
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int progress = MathHelper.ceil((this.handler.getProgress() / 500f) * 147);
        ScreenUtils.drawTexture(context, TEXTURE, this.x + 25, this.y + 77, 0, 186, progress, 17);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if(!super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
        }

        return true;
    }
}
