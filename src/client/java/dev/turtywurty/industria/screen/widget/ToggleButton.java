package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.util.ARGB;

import java.util.function.BiConsumer;

public class ToggleButton extends Button {
    public static final WidgetSprites DEFAULT_TEXTURES = new WidgetSprites(
            Industria.id("textures/gui/widget/toggle_switch_on.png"),
            Industria.id("textures/gui/widget/toggle_switch_off.png"),
            Industria.id("textures/gui/widget/selected_toggle_switch_on.png"),
            Industria.id("textures/gui/widget/selected_toggle_switch_off.png")
    );
    public static final WidgetSprites DEFAULT_COLOURED_TEXTURES = new WidgetSprites(
            Industria.id("textures/gui/widget/toggle_switch_green_on.png"),
            Industria.id("textures/gui/widget/toggle_switch_red_off.png"),
            Industria.id("textures/gui/widget/selected_toggle_switch_green_on.png"),
            Industria.id("textures/gui/widget/selected_toggle_switch_red_off.png")
    );

    private final WidgetSprites textures;
    private final BiConsumer<ToggleButton, Boolean> onPressed;
    private boolean toggled;

    protected ToggleButton(int x, int y, BiConsumer<ToggleButton, Boolean> onPressed, CreateNarration narrationSupplier, boolean defaultToggle, WidgetSprites textures) {
        super(x, y, 32, 16, net.minecraft.network.chat.Component.empty(), $ -> {
        }, narrationSupplier);
        this.toggled = defaultToggle;
        this.textures = textures;
        this.onPressed = onPressed;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        toggle();
        this.onPressed.accept(this, this.toggled);
    }

    public void toggle() {
        this.toggled = !this.toggled;
    }

    @Override
    protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        ScreenUtils.drawTexture(
                context,
                this.textures.get(this.toggled, isHovered()),
                getX(),
                getY(),
                0, 0,
                getWidth(),
                getHeight(),
                32, 16,
                ARGB.white(this.alpha)
        );
    }

    public void setToggled(boolean isToggled) {
        this.toggled = isToggled;
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public WidgetSprites getTextures() {
        return this.textures;
    }

    public static class Builder {
        private final int x, y;
        private BiConsumer<ToggleButton, Boolean> onPressed = (button, toggled) -> {
        };
        private CreateNarration narrationSupplier = textSupplier -> net.minecraft.network.chat.Component.empty();
        private boolean defaultToggled;
        private WidgetSprites textures = ToggleButton.DEFAULT_TEXTURES;

        public Builder(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Builder onPress(BiConsumer<ToggleButton, Boolean> onPressed) {
            this.onPressed = onPressed;
            return this;
        }

        public Builder narrationSupplier(CreateNarration narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public Builder toggledByDefault(boolean toggled) {
            this.defaultToggled = toggled;
            return this;
        }

        public Builder textures(WidgetSprites textures) {
            this.textures = textures;
            return this;
        }

        public ToggleButton build() {
            return new ToggleButton(this.x, this.y, this.onPressed, this.narrationSupplier, this.defaultToggled, this.textures);
        }
    }
}
