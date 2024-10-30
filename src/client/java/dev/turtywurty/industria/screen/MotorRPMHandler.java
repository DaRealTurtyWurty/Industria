package dev.turtywurty.industria.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public interface MotorRPMHandler {
    default TextFieldWidget createWidget(TextRenderer textRenderer, int x, int y, int width, int height) {
        var widget = new TextFieldWidget(textRenderer, x, y, width, height, Text.empty());
        widget.setMaxLength(6);
        widget.setText(String.valueOf(getTargetRPM()));
        widget.setChangedListener(value -> {
            try {
                int targetRPM = Integer.parseInt(value);
                setTargetRPM(targetRPM);
            } catch (NumberFormatException ignored) {
                setTargetRPM(0);
            }
        });

        return widget;
    }

    default void listenForUpdates() {
        TextFieldWidget targetRPMField = getTargetRPMField();

        int fieldRPM = 0;
        try {
            fieldRPM = Integer.parseInt(targetRPMField.getText());
        } catch (NumberFormatException ignored) {
            targetRPMField.setText("0");
        }

        int targetRPM = getTargetRPM();
        if (targetRPM != fieldRPM) {
            targetRPMField.setText(String.valueOf(targetRPM));
        }
    }

    int getTargetRPM();

    void setTargetRPM(int targetRPM);

    TextFieldWidget getTargetRPMField();
}
