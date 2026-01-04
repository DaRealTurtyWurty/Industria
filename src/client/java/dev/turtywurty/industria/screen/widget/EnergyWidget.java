package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.screen.widget.util.Orientation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import team.reborn.energy.api.EnergyStorage;

import java.util.function.Consumer;

// TODO: Use texture for the energy bar instead of a solid color fill
public class EnergyWidget implements Renderable, LayoutElement {
    private final EnergyStorage energyStorage;
    private final int width, height;
    private int x, y;
    private int color;
    private final Orientation orientation;

    public EnergyWidget(EnergyStorage energyStorage, int x, int y, int width, int height, int color, Orientation orientation) {
        this.energyStorage = energyStorage;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.orientation = orientation;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        long currentEnergy = this.energyStorage.getAmount();
        long maxEnergy = this.energyStorage.getCapacity();
        long energy = Math.min(maxEnergy, Math.max(0, currentEnergy));
        float percentage = (float) energy / maxEnergy;

        int fillX = this.x, fillY = this.y, fillWidth = this.width, fillHeight = this.height;
        if (orientation == Orientation.VERTICAL) {
            fillHeight = (int) (height * percentage);
            fillY = y + height - fillHeight;
        } else { // HORIZONTAL
            fillWidth = (int) (width * percentage);
        }

        context.fill(fillX, fillY, fillX + fillWidth, fillY + fillHeight, this.color);

        if (isPointWithinBounds(fillX, fillY, fillWidth, fillHeight, mouseX, mouseY)) {
            drawTooltip(context, mouseX, mouseY);
        }
    }

    protected void drawTooltip(GuiGraphics context, int mouseX, int mouseY) {
        context.setTooltipForNextFrame(Minecraft.getInstance().font,
                Component.literal(energyStorage.getAmount() + " / " + energyStorage.getCapacity() + " FE"),
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
    public void visitWidgets(Consumer<AbstractWidget> consumer) {}

    private static boolean isPointWithinBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }

    public static class Builder {
        private final EnergyStorage energyStorage;
        private int x, y;
        private int width, height;
        private int color;
        private Orientation orientation = Orientation.VERTICAL;

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

        public Builder orientation(Orientation orientation) {
            this.orientation = orientation;
            return this;
        }

        public EnergyWidget build() {
            return new EnergyWidget(energyStorage, x, y, width, height, color, orientation);
        }
    }
}