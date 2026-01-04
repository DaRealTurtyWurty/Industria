package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.gasapi.api.GasVariantAttributeHandler;
import dev.turtywurty.gasapi.api.GasVariantAttributes;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.gasapi.handler.GasRenderHandler;
import dev.turtywurty.gasapi.handler.GasRenderHandlerRegistry;
import dev.turtywurty.industria.screen.widget.util.Orientation;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GasWidget implements Renderable, LayoutElement {
    private final SingleGasStorage gasTank;
    private final Supplier<BlockPos> posSupplier;
    private final Orientation orientation;

    private final int width, height;
    private int x, y;

    public GasWidget(SingleGasStorage gasTank, int x, int y, int width, int height, Supplier<BlockPos> posSupplier, Orientation orientation) {
        this.gasTank = gasTank;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.posSupplier = posSupplier;
        this.orientation = orientation;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Gas gas = this.gasTank.variant.getGas();
        long gasAmount = this.gasTank.getAmount();

        GasRenderHandler gasRenderHandler = GasRenderHandlerRegistry.get(gas);
        if (gasRenderHandler == null || gasAmount <= 0)
            return;

        BlockPos pos = this.posSupplier.get();
        Level world = Minecraft.getInstance().level;

        int color = gasRenderHandler.getColor(world, pos);

        long gasCapacity = this.gasTank.getCapacity();
        float percentage = (float) gasAmount / gasCapacity;
        int fillX = this.x, fillY = this.y, fillWidth = this.width, fillHeight = this.height;
        if (this.orientation == Orientation.VERTICAL) {
            fillY = this.y + (int) ((1 - percentage) * this.height);
            fillHeight = (int) (this.height * percentage);
        } else { // HORIZONTAL
            fillWidth = (int) (this.width * percentage);
        }

        context.fill(fillX, fillY, fillX + fillWidth, fillY + fillHeight, color);

        if (isPointWithinBounds(fillX, fillY, fillWidth, fillHeight, mouseX, mouseY)) {
            drawTooltip(context, mouseX, mouseY);
        }
    }

    protected void drawTooltip(GuiGraphics context, int mouseX, int mouseY) {
        long gasAmount = this.gasTank.getAmount();
        long gasCapacity = this.gasTank.getCapacity();

        Gas gas = this.gasTank.variant.getGas();
        GasVariantAttributeHandler gasVariantAttributeHandler = GasVariantAttributes.getHandler(gas);
        if (gasVariantAttributeHandler == null)
            return;

        Font textRenderer = Minecraft.getInstance().font;
        if (gas != null && gasAmount > 0) {
            context.setComponentTooltipForNextFrame(textRenderer, List.of(
                            gasVariantAttributeHandler.getName(this.gasTank.variant),
                            Component.literal((((float) gasAmount / FluidConstants.BUCKET) * 1000) + " / " + ((int) (((float) gasCapacity / FluidConstants.BUCKET) * 1000)) + " mB")),
                    mouseX, mouseY);
        }
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

    public SingleGasStorage getGasTank() {
        return gasTank;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
    }

    private static boolean isPointWithinBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }

    public static class Builder {
        private final SingleGasStorage gasTank;
        private Supplier<BlockPos> posSupplier = () -> null;
        private int x, y;
        private int width, height;
        private Orientation orientation = Orientation.VERTICAL;

        public Builder(SingleGasStorage gasTank) {
            this.gasTank = gasTank;
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

        public Builder posSupplier(Supplier<BlockPos> posSupplier) {
            this.posSupplier = posSupplier;
            return this;
        }

        public Builder orientation(Orientation orientation) {
            this.orientation = orientation;
            return this;
        }

        public GasWidget build() {
            return new GasWidget(gasTank, x, y, width, height, posSupplier, orientation);
        }
    }
}
