package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.screen.tooltip.ItemListTooltipComponent;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class IndustriaIngredientPreviewWidget<T extends Recipe<?>> implements Widget, Drawable {
    private final int slotIndex;
    private final Function<T, IndustriaIngredient> ingredientGetter;

    private int x, y;
    private @Nullable T recipe;
    private long cycleTimeMs;
    private boolean shouldRender = true;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;

    public IndustriaIngredientPreviewWidget(int x, int y, int slotIndex, @NotNull Function<T, IndustriaIngredient> ingredientGetter) {
        this(x, y, slotIndex, 1000, ingredientGetter);
    }

    public IndustriaIngredientPreviewWidget(int x, int y, int slotIndex, long cycleTimeMs, @NotNull Function<T, IndustriaIngredient> ingredientGetter) {
        this.x = x;
        this.y = y;
        this.slotIndex = slotIndex;
        this.cycleTimeMs = cycleTimeMs;
        this.ingredientGetter = ingredientGetter;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.shouldRender || this.recipe == null)
            return;

        IndustriaIngredient ingredient = this.ingredientGetter.apply(this.recipe);
        if (ingredient != null) {
            List<ItemStack> matching = ingredient.getMatchingStacks();
            if (matching.isEmpty())
                return;

            int renderItemIndex = (int) (System.currentTimeMillis() / this.cycleTimeMs % matching.size());

            ItemStack stack = matching.get(renderItemIndex);
            if (stack.isEmpty())
                return;

            context.drawItem(stack, this.x, this.y);
            context.drawStackOverlay(this.textRenderer, stack, this.x, this.y);
            context.fill(this.x, this.y, this.x + 16, this.y + 16, 256, 0x88808080);

            if (!isPointWithinBounds(this.x, this.y, 16, 16, mouseX, mouseY))
                return;

            if (!Screen.hasShiftDown()) {
                context.drawItemTooltip(this.textRenderer, stack, mouseX, mouseY);
                return;
            }

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

    public int getSlotIndex() {
        return this.slotIndex;
    }

    public void setRecipe(@Nullable T recipe) {
        this.recipe = recipe;
    }

    public @Nullable T getRecipe() {
        return this.recipe;
    }

    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }

    public boolean shouldRender() {
        return this.shouldRender;
    }

    public void setCycleTimeMs(long cycleTimeMs) {
        this.cycleTimeMs = cycleTimeMs;
    }

    public long getCycleTimeMs() {
        return this.cycleTimeMs;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
    }

    private static boolean isPointWithinBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }
}
