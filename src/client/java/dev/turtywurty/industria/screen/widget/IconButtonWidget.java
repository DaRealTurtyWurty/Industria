package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class IconButtonWidget extends Button {
    private final Identifier iconTexture;
    private final int u, vNormal, vHover, vDisabled, iconW, iconH, texW, texH;
    private final boolean drawBackground;

    public IconButtonWidget(int x, int y, int width, int height, OnPress onPress, Identifier iconTexture,
                            int u, int vNormal, int vHover, int vDisabled, int iconW, int iconH, int texW, int texH,
                            boolean drawBackground) {
        super(x, y, width, height, net.minecraft.network.chat.Component.empty(), onPress, DEFAULT_NARRATION);
        this.iconTexture = iconTexture;
        this.u = u;
        this.vNormal = vNormal;
        this.vHover = vHover;
        this.vDisabled = vDisabled;
        this.iconW = iconW;
        this.iconH = iconH;
        this.texW = texW;
        this.texH = texH;
        this.drawBackground = drawBackground;
    }

    @Override
    protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        if (this.drawBackground) {
            ScreenUtils.drawGuiTexture(
                    context,
                    SPRITES.get(this.active, isHoveredOrFocused()),
                    getX(),
                    getY(),
                    getWidth(),
                    getHeight(),
                    ARGB.white(this.alpha)
            );
        }

        int iconX = getX() + (getWidth() - iconW) / 2;
        int iconY = getY() + (getHeight() - iconH) / 2;
        int v = !this.active ? this.vDisabled : (isHovered() || isFocused() ? this.vHover : this.vNormal);
        ScreenUtils.drawTexture(
                context,
                this.iconTexture,
                iconX,
                iconY,
                this.u,
                v,
                this.iconW,
                this.iconH,
                this.texW,
                this.texH
        );
    }

    public static class Builder {
        private int x, y, width = 20, height = 20;
        private OnPress onPress = button -> {
        };
        private Identifier iconTexture;
        private int u, vNormal, vHover, vDisabled, iconW = 16, iconH = 16, texW = 16, texH = 16;
        private boolean drawBackground = true;

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder onPress(OnPress onPress) {
            this.onPress = onPress;
            return this;
        }

        public Builder icon(Identifier iconTexture, int u, int v, int iconW, int iconH, int texW, int texH) {
            this.iconTexture = iconTexture;
            this.u = u;
            this.vNormal = v;
            this.vHover = v;
            this.vDisabled = v;
            this.iconW = iconW;
            this.iconH = iconH;
            this.texW = texW;
            this.texH = texH;
            return this;
        }

        public Builder iconStates(Identifier iconTexture, int u, int vNormal, int vHover, int vDisabled, int iconW, int iconH, int texW, int texH) {
            this.iconTexture = iconTexture;
            this.u = u;
            this.vNormal = vNormal;
            this.vHover = vHover;
            this.vDisabled = vDisabled;
            this.iconW = iconW;
            this.iconH = iconH;
            this.texW = texW;
            this.texH = texH;
            return this;
        }

        public Builder drawBackground(boolean drawBackground) {
            this.drawBackground = drawBackground;
            return this;
        }

        public IconButtonWidget build() {
            return new IconButtonWidget(x, y, width, height, onPress, iconTexture, u, vNormal, vHover, vDisabled, iconW, iconH, texW, texH, drawBackground);
        }
    }
}
