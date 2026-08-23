package dev.turtywurty.industria.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public interface MotorRPMHandler {
    default EditBox createWidget(Font textRenderer, int x, int y, int width, int height) {
        var widget = new EditBox(textRenderer, x, y, width, height, Component.empty());
        widget.setMaxLength(6);
        widget.setValue(String.valueOf(getTargetRPM()));
        widget.setResponder(value -> {
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
        EditBox targetRPMField = getTargetRPMField();

        int fieldRPM = 0;
        try {
            fieldRPM = Integer.parseInt(targetRPMField.getValue());
        } catch (NumberFormatException ignored) {
            targetRPMField.setValue("0");
        }

        int targetRPM = getTargetRPM();
        if (targetRPM != fieldRPM) {
            targetRPMField.setValue(String.valueOf(targetRPM));
        }
    }

    int getTargetRPM();

    void setTargetRPM(int targetRPM);

    EditBox getTargetRPMField();
}
