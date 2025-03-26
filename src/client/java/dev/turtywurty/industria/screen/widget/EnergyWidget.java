package dev.turtywurty.industria.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import team.reborn.energy.api.EnergyStorage;

import java.util.function.Consumer;

// TODO: Redo rendering of energy widget to make it look better
public class EnergyWidget implements Drawable, Widget {
    private final EnergyStorage energyStorage;
    private final int width, height;
    private int x, y;
    private int color;

    public EnergyWidget(EnergyStorage energyStorage, int x, int y, int width, int height, int color) {
        this.energyStorage = energyStorage;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long currentEnergy = this.energyStorage.getAmount();
        long maxEnergy = this.energyStorage.getCapacity();
        long energy = Math.min(maxEnergy, Math.max(0, currentEnergy));
        float percentage = (float) energy / maxEnergy;
        int fillHeight = (int) (this.height * percentage);
        context.fill(x, y + this.height - fillHeight, x + width, y + this.height, color);

        if (isPointWithinBounds(this.x, this.y + this.height - fillHeight, this.width, fillHeight, mouseX, mouseY)) {
            drawTooltip(context, mouseX, mouseY);
        }
    }

    protected void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        context.drawTooltip(MinecraftClient.getInstance().textRenderer,
                Text.literal(energyStorage.getAmount() + " / " + energyStorage.getCapacity() + " FE"),
                mouseX, mouseY);
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
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    public EnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {}

    private static boolean isPointWithinBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }

    public static class Builder {
        private final EnergyStorage energyStorage;
        private int x, y;
        private int width, height;
        private int color;

        public Builder(EnergyStorage energyStorage) {
            this.energyStorage = energyStorage;
        }

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public EnergyWidget build() {
            return new EnergyWidget(energyStorage, x, y, width, height, color);
        }
    }
}
