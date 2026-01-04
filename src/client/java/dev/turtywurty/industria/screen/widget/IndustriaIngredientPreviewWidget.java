package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.screen.tooltip.ItemListTooltipComponent;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class IndustriaIngredientPreviewWidget<T extends Recipe<?>> extends AbstractWidget {
    private final int slotIndex;
    private final Function<T, IndustriaIngredient> ingredientGetter;

    private @Nullable T recipe;
    private long cycleTimeMs;
    private boolean shouldRender = true;

    private final Minecraft client = Minecraft.getInstance();
    private final Font textRenderer = client.font;

    public IndustriaIngredientPreviewWidget(int x, int y, int slotIndex, @NotNull Function<T, IndustriaIngredient> ingredientGetter) {
        this(x, y, slotIndex, 1000, ingredientGetter);
    }

    public IndustriaIngredientPreviewWidget(int x, int y, int slotIndex, long cycleTimeMs, @NotNull Function<T, IndustriaIngredient> ingredientGetter) {
        super(x, y, 16, 16, Component.empty());
        this.slotIndex = slotIndex;
        this.cycleTimeMs = cycleTimeMs;
        this.ingredientGetter = ingredientGetter;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
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

            context.renderItem(stack, getX(), getY());
            context.renderItemDecorations(this.textRenderer, stack, getX(), getY());
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x8808080);

            if (!isPointWithinBounds(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY))
                return;

            if (!this.client.hasShiftDown()) {
                context.setTooltipForNextFrame(this.textRenderer, stack, mouseX, mouseY);
                return;
            }

            var itemListComponent = new ItemListTooltipComponent(matching, 5);
            itemListComponent.onRenderTick(context, mouseX, mouseY);

            List<ClientTooltipComponent> componentList = Screen.getTooltipFromItem(this.client, stack).stream()
                    .map(Component::getVisualOrderText)
                    .map(ClientTooltipComponent::create)
                    .collect(Util.toMutableList());

            stack.getTooltipImage().ifPresent((data) -> componentList.add(componentList.isEmpty() ? 0 : 1, ClientTooltipComponent.create(data)));
            componentList.addFirst(itemListComponent);

            context.renderTooltip(this.textRenderer, componentList, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
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
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
    }

    private static boolean isPointWithinBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }
}
