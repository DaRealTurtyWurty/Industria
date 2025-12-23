package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.screen.tooltip.ItemListTooltipComponent;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class IndustriaIngredientPreviewWidget<T extends Recipe<?>> extends ClickableWidget {
    private final int slotIndex;
    private final Function<T, IndustriaIngredient> ingredientGetter;

    private @Nullable T recipe;
    private long cycleTimeMs;
    private boolean shouldRender = true;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;

    public IndustriaIngredientPreviewWidget(int x, int y, int slotIndex, @NotNull Function<T, IndustriaIngredient> ingredientGetter) {
        this(x, y, slotIndex, 1000, ingredientGetter);
    }

    public IndustriaIngredientPreviewWidget(int x, int y, int slotIndex, long cycleTimeMs, @NotNull Function<T, IndustriaIngredient> ingredientGetter) {
        super(x, y, 16, 16, Text.empty());
        this.slotIndex = slotIndex;
        this.cycleTimeMs = cycleTimeMs;
        this.ingredientGetter = ingredientGetter;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
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

            context.drawItem(stack, getX(), getY());
            context.drawStackOverlay(this.textRenderer, stack, getX(), getY());
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x8808080);

            if (!isPointWithinBounds(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY))
                return;

            if (!this.client.isShiftPressed()) {
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

            context.drawTooltipImmediately(this.textRenderer, componentList, mouseX, mouseY, HoveredTooltipPositioner.INSTANCE, null);
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
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
    }

    private static boolean isPointWithinBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }
}
