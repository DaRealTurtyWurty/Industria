package dev.turtywurty.industria.screen.widget;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.util.enums.EnumValueCacher;
import dev.turtywurty.industria.util.enums.StringRepresentable;
import dev.turtywurty.industria.util.enums.TextEnum;
import dev.turtywurty.industria.util.enums.TraversableEnum;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.function.Consumer;

public class SelectEnumButton<T extends Enum<?> & TraversableEnum<T> & EnumValueCacher<T> & TextEnum & StringRepresentable> extends ButtonWidget {
    private static final Identifier PLAIN_TEXTURE = Industria.id("textures/gui/widget/select_enum_button.png");
    private static final Identifier HOVERED_TEXTURE = Industria.id("textures/gui/widget/select_enum_button_focused.png");
    private static final Identifier DISABLED_OVERLAY_TEXTURE = Industria.id("textures/gui/widget/select_enum_button_disabled.png");

    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    private final Map<T, Identifier> textureMap;
    private final Consumer<T> onNewValue;
    private final int columns;
    private final Set<T> disabledValues = new HashSet<>();

    private T value;
    private boolean hoveredLastFrame;

    @SafeVarargs
    public SelectEnumButton(T startValue, Consumer<T> onNewValue, int columns, int x, int y, int width, int height, Map<T, Identifier> textureMap, T... disabledValues) {
        super(x, y, width, height, Text.empty(), null, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.value = startValue;
        this.onNewValue = onNewValue;
        this.columns = columns;
        this.textureMap = ImmutableMap.copyOf(textureMap);
        this.disabledValues.addAll(Arrays.asList(disabledValues));
    }

    @SafeVarargs
    public final void setDisabledValues(T... disabledValues) {
        this.disabledValues.clear();
        this.disabledValues.addAll(Arrays.asList(disabledValues));

        setActiveness();
    }

    public void disableValue(T value) {
        this.disabledValues.add(value);
        setActiveness();
    }

    public void enableValue(T value) {
        this.disabledValues.remove(value);
        setActiveness();
    }

    public void clearDisabledValues() {
        this.disabledValues.clear();
        setActiveness();
    }

    public T getValue() {
        return this.value;
    }

    public boolean isHoveredLastFrame() {
        return this.hoveredLastFrame;
    }

    @Override
    public void onPress() {
        if (Screen.hasShiftDown())
            this.value = findPreviousEnabled(this.value);
        else
            this.value = findNextEnabled(this.value);

        this.onNewValue.accept(this.value);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverSelectionArea(mouseX, mouseY) && this.hoveredLastFrame) {
            int ordinal = getOrdinal((int) mouseX, (int) mouseY);

            T current = this.value.getValues()[ordinal];
            if (current != this.value && !this.disabledValues.contains(current)) {
                this.value = current;
                this.onNewValue.accept(current);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        context.drawGuiTexture(TEXTURES.get(this.active, this.hoveredLastFrame), getX(), getY(), this.width, this.height);
        context.drawTexture(this.textureMap.get(this.value), getX() + 2, getY() + 2, 0, 0, 16, 16, 16, 16);

        if (this.hoveredLastFrame) {
            int startX = getSelectionLeft();
            int startY = getBottom();
            for (int ordinal = 0; ordinal < this.value.getValues().length; ordinal++) {
                T current = this.value.getValues()[ordinal];
                int column = ordinal % 3;
                int row = ordinal / 3;

                int x = startX + (this.width * column);
                int y = startY + (this.height * row);

                context.drawTexture(current == this.value ? HOVERED_TEXTURE : PLAIN_TEXTURE, x, y, 0, 0, this.width, this.height, this.width, this.height);
                context.drawTexture(this.textureMap.get(current), x + 2, y + 2, 0, 0, 16, 16, 16, 16);

                if (this.disabledValues.contains(current)) {
                    context.drawTexture(DISABLED_OVERLAY_TEXTURE, x, y, 0, 0, this.width, this.height, this.width, this.height);
                }
            }

            // draw hover tooltip
            if(isMouseOverSelectionArea(mouseX, mouseY)) {
                int ordinal = getOrdinal(mouseX, mouseY);

                T current = this.value.getValues()[ordinal];
                context.drawTooltip(this.textRenderer, current.getAsText(), mouseX, mouseY);
            }
        }

        if(isHovered()) {
            context.drawTooltip(this.textRenderer, this.value.getAsText(), mouseX, mouseY);
        }

        this.hoveredLastFrame = isHovered() || (this.hoveredLastFrame && isMouseOverSelectionArea(mouseX, mouseY + 1));
    }

    private T findNextEnabled(T value) {
        T next = value.next();
        while(this.disabledValues.contains(next)) {
            next = next.next();
        }

        return next;
    }

    private T findPreviousEnabled(T value) {
        T previous = value.previous();
        while(this.disabledValues.contains(previous)) {
            previous = previous.previous();
        }

        return previous;
    }

    private int getSelectionLeft() {
        return getX() - (this.width * MathHelper.floor(this.columns / 2f));
    }

    private int getOrdinal(int x, int y) {
        int startX = getSelectionLeft();
        int startY = getBottom();
        int localX = x - startX;
        if(localX < 0)
            return -1;

        int column = localX / this.width;
        int row = (y - startY) / this.height;
        return column + (row * this.columns);
    }

    private boolean isMouseOverSelectionArea(double mouseX, double mouseY) {
        int startY = getBottom();
        int numValues = this.value.getValues().length;
        int endY = startY + (this.height * MathHelper.ceil(numValues / (float) this.columns));
        if(mouseY < startY || mouseY > endY) return false;

        int ordinal = getOrdinal((int) mouseX, (int) mouseY);
        return ordinal < numValues && ordinal >= 0;
    }

    private void setActiveness() {
        this.active = this.disabledValues.size() != this.value.getValues().length;
    }
}
