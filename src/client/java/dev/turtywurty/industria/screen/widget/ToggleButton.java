package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.function.BiConsumer;

public class ToggleButton extends ButtonWidget {
    public static final ButtonTextures DEFAULT_TEXTURES = new ButtonTextures(
            Industria.id("textures/gui/widget/toggle_switch_on.png"),
            Industria.id("textures/gui/widget/toggle_switch_off.png"),
            Industria.id("textures/gui/widget/selected_toggle_switch_on.png"),
            Industria.id("textures/gui/widget/selected_toggle_switch_off.png")
    );
    public static final ButtonTextures DEFAULT_COLOURED_TEXTURES = new ButtonTextures(
            Industria.id("textures/gui/widget/toggle_switch_green_on.png"),
            Industria.id("textures/gui/widget/toggle_switch_red_off.png"),
            Industria.id("textures/gui/widget/selected_toggle_switch_green_on.png"),
            Industria.id("textures/gui/widget/selected_toggle_switch_red_off.png")
    );

    private final ButtonTextures textures;
    private final BiConsumer<ToggleButton, Boolean> onPressed;
    private boolean toggled;

    protected ToggleButton(int x, int y, BiConsumer<ToggleButton, Boolean> onPressed, NarrationSupplier narrationSupplier, boolean defaultToggle, ButtonTextures textures) {
        super(x, y, 32, 16, Text.empty(), $ -> {
        }, narrationSupplier);
        this.toggled = defaultToggle;
        this.textures = textures;
        this.onPressed = onPressed;
    }

    @Override
    public void onPress(AbstractInput input) {
        toggle();
        this.onPressed.accept(this, this.toggled);
    }

    public void toggle() {
        this.toggled = !this.toggled;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ScreenUtils.drawTexture(
                context,
                this.textures.get(this.toggled, isHovered()),
                getX(),
                getY(),
                0, 0,
                getWidth(),
                getHeight(),
                32, 16,
                ColorHelper.getWhite(this.alpha)
        );
    }

    public void setToggled(boolean isToggled) {
        this.toggled = isToggled;
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public ButtonTextures getTextures() {
        return this.textures;
    }

    public static class Builder {
        private final int x, y;
        private BiConsumer<ToggleButton, Boolean> onPressed = (button, toggled) -> {
        };
        private NarrationSupplier narrationSupplier = textSupplier -> Text.empty();
        private boolean defaultToggled;
        private ButtonTextures textures = ToggleButton.DEFAULT_TEXTURES;

        public Builder(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Builder onPress(BiConsumer<ToggleButton, Boolean> onPressed) {
            this.onPressed = onPressed;
            return this;
        }

        public Builder narrationSupplier(NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public Builder toggledByDefault(boolean toggled) {
            this.defaultToggled = toggled;
            return this;
        }

        public Builder textures(ButtonTextures textures) {
            this.textures = textures;
            return this;
        }

        public ToggleButton build() {
            return new ToggleButton(this.x, this.y, this.onPressed, this.narrationSupplier, this.defaultToggled, this.textures);
        }
    }
}
