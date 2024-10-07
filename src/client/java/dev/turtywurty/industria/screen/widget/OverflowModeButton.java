package dev.turtywurty.industria.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.network.ChangeDrillOverflowModePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class OverflowModeButton extends ButtonWidget {
    private static final Map<DrillBlockEntity.OverflowMethod, ButtonTextures> BUTTON_TEXTURES = new HashMap<>();
    private static final Map<DrillBlockEntity.OverflowMethod, Identifier> OVERLAY_TEXTURES = new HashMap<>();

    private boolean hoveredLastFrame;

    static {
        for (DrillBlockEntity.OverflowMethod overflowMethod : DrillBlockEntity.OverflowMethod.VALUES) {
            String path = "textures/gui/widget/button_overflow_" + overflowMethod.getSerializedName();
            BUTTON_TEXTURES.put(overflowMethod, new ButtonTextures(
                    Industria.id(path + ".png"),
                    Industria.id(path + "_disabled.png"),
                    Industria.id(path + "_focused.png"),
                    Industria.id(path + "_disabled_focused.png")
            ));

            OVERLAY_TEXTURES.put(overflowMethod, Industria.id(path + "_overlay.png"));
        }
    }

    private final DrillBlockEntity blockEntity;

    public OverflowModeButton(DrillBlockEntity blockEntity, int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty(), OverflowModeButton::onPressed, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.blockEntity = blockEntity;
    }

    private static void onPressed(ButtonWidget widget) {
        if (!(widget instanceof OverflowModeButton button))
            return;

        DrillBlockEntity.OverflowMethod overflowMethod = button.blockEntity.getOverflowMethod();
        if (Screen.hasShiftDown())
            overflowMethod = overflowMethod.previous();
        else
            overflowMethod = overflowMethod.next();

        button.blockEntity.setOverflowMethod(overflowMethod);
        ClientPlayNetworking.send(new ChangeDrillOverflowModePayload(overflowMethod));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mouseXI = (int) mouseX, mouseYI = (int) mouseY;
        if (isMouseOverSelectionArea((int) mouseX, (int) mouseY) && this.hoveredLastFrame) {
            int startX = getX() - getWidth();
            int startY = getBottom();

            int column = (mouseXI - startX) / getWidth();
            int row = (mouseYI - startY) / getHeight();
            int ordinal = column + (row * 3);
            DrillBlockEntity.OverflowMethod overflowMethod = DrillBlockEntity.OverflowMethod.VALUES[ordinal];
            if(overflowMethod != this.blockEntity.getOverflowMethod()) {
                this.blockEntity.setOverflowMethod(overflowMethod);
                ClientPlayNetworking.send(new ChangeDrillOverflowModePayload(overflowMethod));
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        context.drawGuiTexture(TEXTURES.get(this.active, isSelected()), getX(), getY(), getWidth(), getHeight());
        context.drawTexture(OVERLAY_TEXTURES.get(blockEntity.getOverflowMethod()), getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());

        if (this.hoveredLastFrame) {
            int startX = getX() - getWidth();
            int startY = getBottom();
            for (int ordinal = 0; ordinal < DrillBlockEntity.OverflowMethod.VALUES.length; ordinal++) {
                DrillBlockEntity.OverflowMethod overflowMethod = DrillBlockEntity.OverflowMethod.VALUES[ordinal];
                int column = ordinal % 3;
                int row = ordinal / 3;

                context.drawTexture(BUTTON_TEXTURES.get(overflowMethod).get(true, this.blockEntity.getOverflowMethod() == overflowMethod),
                        startX + (getWidth() * column), startY + (getHeight() * row), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
            }
        }

        this.hoveredLastFrame = isHovered() || (this.hoveredLastFrame && isMouseOverSelectionArea(mouseX, mouseY + 1));
    }

    private boolean isMouseOverSelectionArea(int mouseX, int mouseY) {
        return mouseX > getX() - getWidth() &&
                mouseX < getRight() + getWidth() &&
                mouseY > getBottom() &&
                mouseY < getBottom() + getHeight();
    }
}
