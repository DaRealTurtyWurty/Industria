package dev.turtywurty.industria.screen.widget;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.util.enums.EnumValueCacher;
import dev.turtywurty.industria.util.enums.StringRepresentable;
import dev.turtywurty.industria.util.enums.TraversableEnum;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Consumer;

public class SelectEnumButton<T extends Enum<?> & TraversableEnum<T> & EnumValueCacher<T> & StringRepresentable> extends ButtonWidget {
    private static final Identifier PLAIN_TEXTURE = Industria.id("textures/gui/widget/select_enum_button.png");
    private static final Identifier HOVERED_TEXTURE = Industria.id("textures/gui/widget/select_enum_button_focused.png");
    private static final Identifier DISABLED_TEXTURE = Industria.id("textures/gui/widget/select_enum_button_disabled.png");

    private final Map<T, Identifier> textureMap;
    private final Consumer<T> onNewValue;
    private final int columns;

    private T value;
    private boolean hoveredLastFrame;

    public SelectEnumButton(T startValue, Consumer<T> onNewValue, int columns, int x, int y, int width, int height, Map<T, Identifier> textureMap) {
        super(x, y, width, height, Text.empty(), null, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.value = startValue;
        this.onNewValue = onNewValue;

        this.columns = columns;

        this.textureMap = ImmutableMap.copyOf(textureMap);
    }

    @Override
    public void onPress() {
        if (Screen.hasShiftDown())
            this.value = this.value.previous();
        else
            this.value = this.value.next();

        this.onNewValue.accept(this.value);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mouseXI = (int) mouseX, mouseYI = (int) mouseY;
        if (isMouseOverSelectionArea((int) mouseX, (int) mouseY) && this.hoveredLastFrame) {
            int startX = getX() - (this.width * this.columns / 2);
            int startY = getBottom();

            int column = (mouseXI - startX) / this.width;
            int row = (mouseYI - startY) / this.height;
            int ordinal = column + (row * this.columns);

            T value = this.value.getValues()[ordinal];
            if (value != this.value) {
                this.value = value;
                this.onNewValue.accept(value);
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
        context.drawGuiTexture(TEXTURES.get(this.active, isSelected()), getX(), getY(), this.width, this.height);
        context.drawTexture(this.textureMap.get(this.value), getX() + 2, getY() + 2, 0, 0, 16, 16, 16, 16);

        if (this.hoveredLastFrame) {
            int startX = getX() - (this.width * this.columns / 2);
            int startY = getBottom();
            for (int ordinal = 0; ordinal < this.value.getValues().length; ordinal++) {
                T value = this.value.getValues()[ordinal];
                int column = ordinal % 3;
                int row = ordinal / 3;

                int x = startX + (this.width * column);
                int y = startY + (this.height * row);

                context.drawTexture(PLAIN_TEXTURE, x, y, 0, 0, this.width, this.height, this.width, this.height);
                context.drawTexture(this.textureMap.get(value), x + 2, y + 2, 0, 0, 16, 16, 16, 16);
                if (value == this.value) {
                    context.drawTexture(HOVERED_TEXTURE, x, y, 0, 0, this.width, this.height, this.width, this.height);
                }

                // TODO: Disable-able values
            }
        }

        this.hoveredLastFrame = isHovered() || (this.hoveredLastFrame && isMouseOverSelectionArea(mouseX, mouseY + 1));
    }

    private boolean isMouseOverSelectionArea(int mouseX, int mouseY) {
        return mouseX >= getX() - (this.width * this.columns / 2) &&
                mouseX < getX() + (this.width * this.columns / 2) &&
                mouseY >= getBottom() &&
                mouseY < getBottom() + (this.height * (this.value.getValues().length / this.columns));
    }
}
