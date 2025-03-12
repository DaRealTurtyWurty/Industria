package dev.turtywurty.industria.screen.widget;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidWidget implements Drawable, Widget {
    private final SingleFluidStorage fluidTank;
    private final Supplier<BlockPos> posSupplier;

    private final int width, height;
    private int x, y;

    public FluidWidget(SingleFluidStorage fluidTank, int x, int y, int width, int height, Supplier<BlockPos> posSupplier) {
        this.fluidTank = fluidTank;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.posSupplier = posSupplier;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Fluid fluid = this.fluidTank.variant.getFluid();
        long fluidAmount = this.fluidTank.getAmount();
        long fluidCapacity = this.fluidTank.getCapacity();
        int fluidBarHeight = Math.round((float) fluidAmount / fluidCapacity * this.height);

        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (fluidRenderHandler == null || fluidAmount <= 0)
            return;

        BlockPos pos = this.posSupplier.get();
        FluidState fluidState = fluid.getDefaultState();
        World world = MinecraftClient.getInstance().world;

        Sprite stillTexture = fluidRenderHandler.getFluidSprites(world, pos, fluidState)[0];
        int tintColor = fluidRenderHandler.getFluidColor(world, pos, fluidState);

        float red = (tintColor >> 16 & 0xFF) / 255.0F;
        float green = (tintColor >> 8 & 0xFF) / 255.0F;
        float blue = (tintColor & 0xFF) / 255.0F;
        context.drawSpriteStretched(RenderLayer::getGuiTextured, stillTexture, this.x, this.y + this.height - fluidBarHeight, this.width, fluidBarHeight, ColorHelper.fromFloats(1.0F, red, green, blue));

        if (isPointWithinBounds(this.x, this.y, this.width, this.height, mouseX, mouseY)) {
            drawTooltip(context, mouseX, mouseY);
        }
    }

    protected void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        Fluid fluid = this.fluidTank.variant.getFluid();

        long fluidAmount = this.fluidTank.getAmount();
        long fluidCapacity = this.fluidTank.getCapacity();

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (fluid != null && fluidAmount > 0) {
            context.drawTooltip(textRenderer, Text.translatable(fluid.getDefaultState().getBlockState().getBlock().getTranslationKey()), mouseX, mouseY);
            context.drawTooltip(textRenderer, Text.literal(((int) (((float) fluidAmount / FluidConstants.BUCKET) * 1000)) + " / " + ((int) (((float) fluidCapacity / FluidConstants.BUCKET) * 1000)) + " mB"), mouseX, mouseY + 10);
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

    public SingleFluidStorage getFluidTank() {
        return fluidTank;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
    }

    private static boolean isPointWithinBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }

    public static class Builder {
        private final SingleFluidStorage fluidTank;
        private Supplier<BlockPos> posSupplier = () -> null;
        private int x, y;
        private int width, height;

        public Builder(SingleFluidStorage fluidTank) {
            this.fluidTank = fluidTank;
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

        public FluidWidget build() {
            return new FluidWidget(fluidTank, x, y, width, height, this.posSupplier);
        }
    }
}
