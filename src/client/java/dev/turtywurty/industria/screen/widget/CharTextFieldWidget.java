package dev.turtywurty.industria.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class CharTextFieldWidget extends TextFieldWidget {
    public CharTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder) {
        super(textRenderer, x, y, width, height, placeholder);
        setMaxLength(1);
        setChangedListener(this::trimToSingleChar);
    }

    private void trimToSingleChar(String value) {
        if (value == null)
            return;

        if (value.length() > 1) {
            setText(value.substring(0, 1));
        }
    }
}
