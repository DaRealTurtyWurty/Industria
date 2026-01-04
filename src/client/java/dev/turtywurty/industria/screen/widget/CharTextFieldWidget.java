package dev.turtywurty.industria.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class CharTextFieldWidget extends EditBox {
    public CharTextFieldWidget(Font textRenderer, int x, int y, int width, int height, Component placeholder) {
        super(textRenderer, x, y, width, height, placeholder);
        setMaxLength(1);
        setResponder(this::trimToSingleChar);
    }

    private void trimToSingleChar(String value) {
        if (value == null)
            return;

        if (value.length() > 1) {
            setValue(value.substring(0, 1));
        }
    }
}
