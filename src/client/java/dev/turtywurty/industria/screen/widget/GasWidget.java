package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.gasapi.api.GasVariantAttributeHandler;
import dev.turtywurty.gasapi.api.GasVariantAttributes;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.gasapi.handler.GasRenderHandler;
import dev.turtywurty.gasapi.handler.GasRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GasWidget implements Drawable, Widget {
    private final SingleGasStorage gasTank;
    private final Supplier<BlockPos> posSupplier;

    private final int width, height;
    private int x, y;

    public GasWidget(SingleGasStorage gasTank, int x, int y, int width, int height, Supplier<BlockPos> posSupplier) {
        this.gasTank = gasTank;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.posSupplier = posSupplier;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Gas gas = this.gasTank.variant.getGas();
        long gasAmount = this.gasTank.getAmount();
        long gasCapacity = this.gasTank.getCapacity();
        int gasBarHeight = Math.round((float) gasAmount / gasCapacity * this.height);

        GasRenderHandler gasRenderHandler = GasRenderHandlerRegistry.get(gas);
        if (gasRenderHandler == null || gasAmount <= 0)
            return;

        BlockPos pos = this.posSupplier.get();
        World world = MinecraftClient.getInstance().world;

        int color = gasRenderHandler.getColor(world, pos);
        context.fill(this.x, this.y, this.x + this.width, this.y + gasBarHeight, color);

        if (isPointWithinBounds(this.x, this.y, this.width, gasBarHeight, mouseX, mouseY)) {
            drawTooltip(context, mouseX, mouseY);
        }
    }

    protected void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        long gasAmount = this.gasTank.getAmount();
        long gasCapacity = this.gasTank.getCapacity();

        Gas gas = this.gasTank.variant.getGas();
        GasVariantAttributeHandler gasVariantAttributeHandler = GasVariantAttributes.getHandler(gas);
        if (gasVariantAttributeHandler == null)
            return;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (gas != null && gasAmount > 0) {
            context.drawTooltip(textRenderer, List.of(
                            gasVariantAttributeHandler.getName(this.gasTank.variant),
                            Text.literal((((float) gasAmount / FluidConstants.BUCKET) * 1000) + " / " + ((int) (((float) gasCapacity / FluidConstants.BUCKET) * 1000)) + " mB")),
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
    public void forEachChild(Consumer<ClickableWidget> consumer) {
    }

    private static boolean isPointWithinBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }

    public static class Builder {
        private final SingleGasStorage gasTank;
        private Supplier<BlockPos> posSupplier = () -> null;
        private int x, y;
        private int width, height;

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

        public GasWidget build() {
            return new GasWidget(gasTank, x, y, width, height, this.posSupplier);
        }
    }
}
