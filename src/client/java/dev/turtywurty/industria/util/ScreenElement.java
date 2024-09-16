package dev.turtywurty.industria.util;

import net.minecraft.client.gui.DrawContext;

@SuppressWarnings("unchecked")
public abstract class ScreenElement implements ScreenRenderable {
    protected int x, y, z;
    protected int width = 16, height = 16;
    protected float alpha = 1.0F;

    public static ScreenElement wrap(ScreenRenderable renderable) {
        return new ElementWrapper(renderable);
    }

    public static ScreenElement wrap(int x, int y, ScreenRenderable renderable) {
        return new ElementWrapper(x, y, renderable);
    }

    public ScreenElement(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public ScreenElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public <T extends ScreenElement> T x(int x) {
        this.x = x;
        return (T) this;
    }

    public <T extends ScreenElement> T y(int y) {
        this.y = y;
        return (T) this;
    }

    public <T extends ScreenElement> T z(int z) {
        this.z = z;
        return (T) this;
    }

    public <T extends ScreenElement> T width(int width) {
        this.width = width;
        return (T) this;
    }

    public <T extends ScreenElement> T height(int height) {
        this.height = height;
        return (T) this;
    }

    public <T extends ScreenElement> T pos(int x, int y) {
        this.x = x;
        this.y = y;
        return (T) this;
    }

    public <T extends ScreenElement> T pos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return (T) this;
    }

    public <T extends ScreenElement> T size(int width, int height) {
        this.width = width;
        this.height = height;
        return (T) this;
    }

    public <T extends ScreenElement> T bounds(int x0, int y0, int x1, int y1) {
        this.x = x0;
        this.y = y0;
        this.width = x1 - x0;
        this.height = y1 - y0;
        return (T) this;
    }

    public <T extends ScreenElement> T alpha(float alpha) {
        this.alpha = alpha;
        return (T) this;
    }

    public static class ElementWrapper extends ScreenElement {
        private final ScreenRenderable renderable;

        public ElementWrapper(int x, int y, ScreenRenderable renderable) {
            super(x, y);
            this.renderable = renderable;
        }

        public ElementWrapper(ScreenRenderable renderable) {
            super(0, 0);
            this.renderable = renderable;
        }

        @Override
        public void render(DrawContext context, double mouseX, double mouseY, float partialTicks) {
            this.renderable.render(context, mouseX, mouseY, partialTicks);
        }
    }
}
