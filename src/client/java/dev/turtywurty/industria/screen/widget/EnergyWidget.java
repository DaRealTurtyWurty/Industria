package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.screen.widget.util.Orientation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import team.reborn.energy.api.EnergyStorage;

import java.util.Locale;
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
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        long currentEnergy = this.energyStorage.getAmount();
        long maxEnergy = this.energyStorage.getCapacity();
        long energy = Math.clamp(currentEnergy, 0, maxEnergy);
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

    protected void drawTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        boolean showFullAmount = Minecraft.getInstance().hasShiftDown();
        String amount = showFullAmount ? Long.toString(this.energyStorage.getAmount()) : formatCompact(this.energyStorage.getAmount());
        String capacity = showFullAmount ? Long.toString(this.energyStorage.getCapacity()) : formatCompact(this.energyStorage.getCapacity());

        context.setTooltipForNextFrame(Minecraft.getInstance().font,
                Component.literal(amount + " / " + capacity + " FE"),
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
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
    }

    private static String formatCompact(long value) {
        long absValue = Math.abs(value);
        if (absValue < 1000)
            return Long.toString(value);

        String[] suffixes = {"k", "m", "b", "t", "q"};
        double compactValue = value;
        int suffixIndex = -1;
        while (Math.abs(compactValue) >= 1000 && suffixIndex < suffixes.length - 1) {
            compactValue /= 1000.0;
            suffixIndex++;
        }

        if (Math.abs(compactValue) >= 100 || compactValue == Math.rint(compactValue))
            return String.format(Locale.ROOT, "%.0f%s", compactValue, suffixes[suffixIndex]);

        if (Math.abs(compactValue) >= 10)
            return String.format(Locale.ROOT, "%.1f%s", compactValue, suffixes[suffixIndex]);

        return String.format(Locale.ROOT, "%.2f%s", compactValue, suffixes[suffixIndex]);
    }

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
