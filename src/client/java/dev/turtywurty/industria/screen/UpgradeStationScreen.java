package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.UpgradeStationRecipe;
import dev.turtywurty.industria.screen.tooltip.ItemListTooltipComponent;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.SelectRecipeWidget;
import dev.turtywurty.industria.screenhandler.UpgradeStationScreenHandler;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class UpgradeStationScreen extends HandledScreen<UpgradeStationScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/upgrade_station.png");

    private SelectRecipeWidget<UpgradeStationRecipe> recipeSelector;

    public UpgradeStationScreen(UpgradeStationScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        handler.setContentsChangedListener(() -> {
            if(this.recipeSelector != null) {
                this.recipeSelector.setCanCraft(this.handler.canCraft());
                if (!this.recipeSelector.canCraft()) {
                    this.recipeSelector.resetScroll();
                }

                this.recipeSelector.setRecipes(this.handler.getAvailableRecipes());
            }
        });

        this.backgroundWidth = 196;
        this.backgroundHeight = 186;
        this.playerInventoryTitleY = this.backgroundHeight - 92;
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
                    if(this.client == null || this.client.interactionManager == null)
                        return;

                    this.client.interactionManager.clickButton(this.handler.syncId, index);
                    widget.setSelectedRecipeIndex(index);
                })
                .outputFunction(UpgradeStationRecipe::output)
                .columnCount(4)
                .rowCount(3)
                .build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int progress = MathHelper.ceil((this.handler.getProgress() / 500f) * 147);
        ScreenUtils.drawTexture(context, TEXTURE, this.x + 25, this.y + 82, 0, 186, progress, 17);

        renderSelectedRecipePreview(context);
    }

    private void renderSelectedRecipePreview(DrawContext context) {
        int selectedRecipeIndex = this.handler.getSelectedRecipeIndex();
        List<UpgradeStationRecipe> availableRecipes = this.handler.getAvailableRecipes();
        if (selectedRecipeIndex >= 0 && selectedRecipeIndex < this.handler.getAvailableRecipeCount()) {
            UpgradeStationRecipe recipe = availableRecipes.get(selectedRecipeIndex);

            if(this.client == null)
                return;

            for (int index = 0; index < 9; index++) {
                if(index == 4 || !this.handler.getSlot(36 + index).getStack().isEmpty())
                    continue;

                IndustriaIngredient ingredient = recipe.getIngredient(index);
                if(ingredient.isEmpty())
                    continue;

                List<ItemStack> matching = ingredient.getMatchingStacks();
                if(matching.isEmpty())
                    continue;

                int renderItemIndex = (int) (System.currentTimeMillis() / 1000L % matching.size());

                int xPos = this.x + 8 + index % 3 * 18;
                int yPos = this.y + 17 + index / 3 * 18;

                ItemStack stack = matching.get(renderItemIndex);
                context.drawItem(stack, xPos, yPos);
                context.drawStackOverlay(this.textRenderer, stack, xPos - 1, yPos - 1);
                context.fill(xPos, yPos, xPos + 16, yPos + 16, 256, 0x88808080);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        renderSelectedRecipePreviewTooltip(context, mouseX, mouseY);
    }

    private void renderSelectedRecipePreviewTooltip(DrawContext context, int mouseX, int mouseY) {
        int selectedRecipeIndex = this.handler.getSelectedRecipeIndex();
        List<UpgradeStationRecipe> availableRecipes = this.handler.getAvailableRecipes();
        if (selectedRecipeIndex >= 0 && selectedRecipeIndex < this.handler.getAvailableRecipeCount()) {
            UpgradeStationRecipe recipe = availableRecipes.get(selectedRecipeIndex);

            if(this.client == null)
                return;

            for (int index = 0; index < 9; index++) {
                if(index == 4 || !this.handler.getSlot(36 + index).getStack().isEmpty())
                    continue;

                IndustriaIngredient ingredient = recipe.getIngredient(index);
                if(ingredient.isEmpty())
                    continue;

                List<ItemStack> matching = ingredient.getMatchingStacks();
                if(matching.isEmpty())
                    continue;

                int renderItemIndex = (int) (System.currentTimeMillis() / 1000L % matching.size());

                int xPos = 8 + index % 3 * 18;
                int yPos = 17 + index / 3 * 18;

                ItemStack stack = matching.get(renderItemIndex);
                if (isPointWithinBounds(xPos, yPos, 16, 16, mouseX, mouseY)) {
                    if(!Screen.hasShiftDown()) {
                        context.drawItemTooltip(this.textRenderer, stack, mouseX, mouseY);
                    } else {
                        var itemListComponent = new ItemListTooltipComponent(matching, 5);
                        itemListComponent.onRenderTick(context, mouseX, mouseY);

                        List<TooltipComponent> componentList = Screen.getTooltipFromItem(this.client, stack).stream()
                                .map(Text::asOrderedText)
                                .map(TooltipComponent::of)
                                .collect(Util.toArrayList());

                        stack.getTooltipData().ifPresent((data) -> componentList.add(componentList.isEmpty() ? 0 : 1, TooltipComponent.of(data)));
                        componentList.addFirst(itemListComponent);

                        context.drawTooltip(this.textRenderer, componentList, mouseX, mouseY, HoveredTooltipPositioner.INSTANCE, null);
                    }
                }
            }
        }
    }
}
