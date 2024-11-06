package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class SelectRecipeWidget<T extends Recipe<?>> extends ClickableWidget {
    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/stonecutter/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.ofVanilla("container/stonecutter/scroller_disabled");
    private static final Identifier RECIPE_SELECTED_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe_selected");
    private static final Identifier RECIPE_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe_highlighted");
    private static final Identifier RECIPE_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe");

    private final int scrollBarX, scrollBarY, scrollBarWidth, scrollBarHandleHeight;

    private final List<T> recipes = new ArrayList<>();
    private @Nullable T selectedRecipe;
    private final Function<T, ItemStack> outputFunction;
    private final BiConsumer<SelectRecipeWidget<T>, Integer> onRecipeSelected;
    private final List<Consumer<T>> recipeListeners = new ArrayList<>();

    private final int columnCount, rowCount;

    private float scrollAmount;
    private boolean mouseClicked;
    private int scrollOffset;
    private boolean canCraft;

    public SelectRecipeWidget(int x, int y, int width, int height, List<T> recipes, int selectedRecipeIndex, Function<T, ItemStack> outputFunction, BiConsumer<SelectRecipeWidget<T>, Integer> onRecipeSelected, boolean canCraft, int scrollBarX, int scrollBarY, int scrollBarWidth, int scrollBarHandleHeight, int columnCount, int rowCount) {
        super(x, y, width, height, Text.empty());

        setRecipes(recipes);
        setSelectedRecipeIndex(selectedRecipeIndex);

        this.outputFunction = outputFunction;
        this.onRecipeSelected = onRecipeSelected;
        this.canCraft = canCraft;

        this.scrollBarX = scrollBarX;
        this.scrollBarY = scrollBarY;
        this.scrollBarWidth = scrollBarWidth;
        this.scrollBarHandleHeight = scrollBarHandleHeight;

        this.columnCount = columnCount;
        this.rowCount = rowCount;
    }

    public @Nullable T getSelectedRecipe() {
        return this.selectedRecipe;
    }

    public void setSelectedRecipe(@Nullable T selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
        this.recipeListeners.forEach(listener -> listener.accept(this.selectedRecipe));
    }

    public void setSelectedRecipeIndex(int selectedRecipeIndex) {
        setSelectedRecipe(selectedRecipeIndex >= this.recipes.size() ? null : this.recipes.get(MathHelper.clamp(selectedRecipeIndex, 0, this.recipes.isEmpty() ? 0 : this.recipes.size() - 1)));
    }

    public int getSelectedRecipeIndex() {
        return this.recipes.indexOf(this.selectedRecipe);
    }

    public List<T> getRecipes() {
        return this.recipes;
    }

    public void setRecipes(List<T> recipes) {
        List<T> oldRecipes = new ArrayList<>(this.recipes);
        this.recipes.clear();
        this.recipes.addAll(recipes);

        if (this.recipes.size() != oldRecipes.size() || !new HashSet<>(this.recipes).containsAll(oldRecipes)) {
            setSelectedRecipe(this.recipes.isEmpty() ? null : this.recipes.getFirst());
        }
    }

    public void setCanCraft(boolean canCraft) {
        this.canCraft = canCraft;
    }

    public boolean canCraft() {
        return this.canCraft;
    }

    public void resetScroll() {
        this.scrollAmount = 0.0F;
        this.scrollOffset = 0;
    }

    public void addRecipeListener(Consumer<T> listener) {
        this.recipeListeners.add(listener);
    }

    public void removeRecipeListener(Consumer<T> listener) {
        this.recipeListeners.remove(listener);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int scrollBarOffset = (int) (41.0F * this.scrollAmount);
        ScreenUtils.drawGuiTexture(context,
                shouldScroll() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE,
                this.scrollBarX, this.scrollBarY + scrollBarOffset,
                this.scrollBarWidth, this.scrollBarHandleHeight);

        int recipeStartX = getX();
        int recipeStartY = getY();
        int scrollOffset = this.scrollOffset + 12;
        renderRecipeBackground(context, mouseX, mouseY, recipeStartX, recipeStartY, scrollOffset);
        renderRecipeIcons(context, recipeStartX, recipeStartY, scrollOffset);
    }

    private boolean shouldScroll() {
        return this.canCraft && this.recipes.size() > this.columnCount * this.rowCount;
    }

    private void renderRecipeBackground(DrawContext context, int mouseX, int mouseY, int x, int y, int scrollOffset) {
        for (int index = this.scrollOffset; index < scrollOffset && index < this.recipes.size(); index++) {
            int column = index - this.scrollOffset;
            int xPos = x + column % this.columnCount * 16;

            int row = column / this.columnCount;
            int yPos = y + row * 18 + 2;

            Identifier identifier;
            if (index == getSelectedRecipeIndex()) {
                identifier = RECIPE_SELECTED_TEXTURE;
            } else if (mouseX >= xPos && mouseY >= yPos && mouseX < xPos + 16 && mouseY < yPos + 18) {
                identifier = RECIPE_HIGHLIGHTED_TEXTURE;
            } else {
                identifier = RECIPE_TEXTURE;
            }

            ScreenUtils.drawGuiTexture(context, identifier, xPos, yPos - 1, 16, 18);
        }
    }

    private void renderRecipeIcons(DrawContext context, int x, int y, int scrollOffset) {
        for (int index = this.scrollOffset; index < scrollOffset && index < this.recipes.size(); index++) {
            int column = index - this.scrollOffset;
            int xPos = x + column % this.columnCount * 16;

            int row = column / this.columnCount;
            int yPos = y + row * 18 + 2;

            T recipe = this.recipes.get(index);
            ItemStack output = this.outputFunction.apply(recipe);
            context.drawItem(output, xPos, yPos);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.mouseClicked = false;
        if (this.canCraft) {
            int xPos = getX();
            int yPos = getY();
            int scrollY = this.scrollOffset + this.columnCount * this.rowCount;

            for (int index = this.scrollOffset; index < scrollY; index++) {
                int column = index - this.scrollOffset;
                double x = mouseX - (double) (xPos + column % this.columnCount * 16);
                double y = mouseY - (double) (yPos + column / this.columnCount * 18);
                if (x >= 0 && y >= 0 && x < 16 && y < 18 && isValidRecipeIndex(index)) {
                    this.onRecipeSelected.accept(this, index);
                    return true;
                }
            }

            xPos = this.scrollBarX;
            yPos = this.scrollBarY - this.scrollBarHandleHeight / 2;
            if (mouseX >= (double) xPos && mouseX < (double) (xPos + this.scrollBarWidth) && mouseY >= (double) yPos && mouseY < (double) (yPos + this.height)) {
                this.mouseClicked = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isValidRecipeIndex(int index) {
        return index >= 0 && index < this.recipes.size();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.mouseClicked && shouldScroll()) {
            int startY = this.scrollBarY - 1;
            int endY = startY + this.height;
            this.scrollAmount = ((float) mouseY - (float) startY - 7.5F) / ((float) (endY - startY) - this.scrollBarHandleHeight);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int) ((double) (this.scrollAmount * (float) getMaxScroll()) + 0.5) * this.columnCount;
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    private int getMaxScroll() {
        return (this.recipes.size() + this.columnCount - 1) / this.columnCount - this.rowCount;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            if (shouldScroll()) {
                int maxScroll = getMaxScroll();
                float scrollFactor = (float) verticalAmount / (float) maxScroll;
                this.scrollAmount = MathHelper.clamp(this.scrollAmount - scrollFactor, 0.0F, 1.0F);
                this.scrollOffset = (int) ((double) (this.scrollAmount * (float) maxScroll) + 0.5) * this.columnCount;
            }
        }

        return true;
    }

    public static class Builder<T extends Recipe<?>> {
        private int x;
        private int y;
        private int width;
        private int height;
        private final List<T> recipes = new ArrayList<>();
        private int selectedRecipeIndex = 0;
        private Function<T, ItemStack> outputFunction = recipe -> ItemStack.EMPTY;
        private BiConsumer<SelectRecipeWidget<T>, Integer> onRecipeSelected = (widget, index) -> {};
        private boolean canCraft = false;
        private int scrollBarX;
        private int scrollBarY;
        private int scrollBarWidth = 12;
        private int scrollBarHandleHeight = 15;
        private int columnCount = 4;
        private int rowCount = 3;

        public Builder<T> x(int x) {
            this.x = x;
            return this;
        }

        public Builder<T> y(int y) {
            this.y = y;
            return this;
        }

        public Builder<T> position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder<T> width(int width) {
            this.width = width;
            return this;
        }

        public Builder<T> height(int height) {
            this.height = height;
            return this;
        }

        public Builder<T> size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder<T> bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder<T> recipes(Collection<T> recipes) {
            this.recipes.clear();
            this.recipes.addAll(recipes);
            return this;
        }

        public Builder<T> selectedRecipeIndex(int selectedRecipeIndex) {
            this.selectedRecipeIndex = selectedRecipeIndex;
            return this;
        }

        public Builder<T> outputFunction(@NotNull Function<T, ItemStack> outputFunction) {
            this.outputFunction = outputFunction;
            return this;
        }

        public Builder<T> onRecipeSelected(@NotNull BiConsumer<SelectRecipeWidget<T>, Integer> onRecipeSelected) {
            this.onRecipeSelected = onRecipeSelected;
            return this;
        }

        public Builder<T> canCraft(boolean canCraft) {
            this.canCraft = canCraft;
            return this;
        }

        public Builder<T> scrollBarX(int scrollBarX) {
            this.scrollBarX = scrollBarX;
            return this;
        }

        public Builder<T> scrollBarY(int scrollBarY) {
            this.scrollBarY = scrollBarY;
            return this;
        }

        public Builder<T> scrollBarPosition(int scrollBarX, int scrollBarY) {
            this.scrollBarX = scrollBarX;
            this.scrollBarY = scrollBarY;
            return this;
        }

        public Builder<T> scrollBarWidth(int scrollBarWidth) {
            this.scrollBarWidth = scrollBarWidth;
            return this;
        }

        public Builder<T> scrollBarHandleHeight(int scrollBarHandleHeight) {
            this.scrollBarHandleHeight = scrollBarHandleHeight;
            return this;
        }

        public Builder<T> scrollBarSize(int scrollBarWidth, int scrollBarHandleHeight) {
            this.scrollBarWidth = scrollBarWidth;
            this.scrollBarHandleHeight = scrollBarHandleHeight;
            return this;
        }

        public Builder<T> scrollBarBounds(int scrollBarX, int scrollBarY, int scrollBarWidth, int scrollBarHandleHeight) {
            this.scrollBarX = scrollBarX;
            this.scrollBarY = scrollBarY;
            this.scrollBarWidth = scrollBarWidth;
            this.scrollBarHandleHeight = scrollBarHandleHeight;
            return this;
        }

        public Builder<T> columnCount(int columnCount) {
            this.columnCount = columnCount;
            return this;
        }

        public Builder<T> rowCount(int rowCount) {
            this.rowCount = rowCount;
            return this;
        }

        public Builder<T> gridSize(int columnCount, int rowCount) {
            this.columnCount = columnCount;
            this.rowCount = rowCount;
            return this;
        }

        public SelectRecipeWidget<T> build() {
            if(this.width <= 0) {
                this.width = (this.scrollBarX <= 0 ? 2 : this.scrollBarX - this.x + this.scrollBarWidth);
            }

            if(this.height <= 0) {
                this.height = 18 * this.rowCount + 2;
            }

            if(this.scrollBarX <= 0) {
                this.scrollBarX = this.x + this.width - this.scrollBarWidth;
            }

            if(this.scrollBarY <= 0) {
                this.scrollBarY = this.y;
            }

            return new SelectRecipeWidget<>(this.x, this.y, this.width, this.height, this.recipes, this.selectedRecipeIndex, this.outputFunction, this.onRecipeSelected, this.canCraft, this.scrollBarX, this.scrollBarY, this.scrollBarWidth, this.scrollBarHandleHeight, this.columnCount, this.rowCount);
        }
    }
}
