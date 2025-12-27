package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.screen.fakeworld.FakeWorldScene;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class FakeWorldWidget implements Drawable, Element, Widget, Selectable {
    protected int width;
    protected int height;
    protected final FakeWorldScene scene;
    protected int x;
    protected int y;
    protected boolean focused;
    protected final boolean enableInteraction;
    protected final float dragSensitivity;

    protected FakeWorldWidget(@NotNull FakeWorldScene scene, int x, int y, int width, int height, boolean enableInteraction, float dragSensitivity) {
        this.scene = scene;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.enableInteraction = enableInteraction;
        this.dragSensitivity = dragSensitivity;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        scene.render(context, x, y, width, height, deltaTicks);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (enableInteraction && click.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
            this.scene.rotateCamera((float) (offsetX * this.dragSensitivity), (float) (offsetY * this.dragSensitivity));
            return true;
        }

        return false;
    }

    public void tick() {
        this.scene.tick();
    }

    public void onClose() {
        this.scene.close();
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return Widget.super.getNavigationFocus();
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {}

    public static class Builder {
        private FakeWorldScene scene;
        private int x = 0;
        private int y = 0;
        private int width = 0;
        private int height = 0;
        private boolean enableInteraction = true;
        private float dragSensitivity = 0.35F;

        public Builder scene(FakeWorldScene scene) {
            this.scene = scene;
            return this;
        }

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

        public Builder enableInteraction(boolean enableInteraction) {
            this.enableInteraction = enableInteraction;
            return this;
        }

        public Builder dragSensitivity(float dragSensitivity) {
            this.dragSensitivity = dragSensitivity;
            return this;
        }

        public FakeWorldWidget build() {
            return new FakeWorldWidget(scene, x, y, width, height, enableInteraction, dragSensitivity);
        }
    }
}
