package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.screen.widget.util.Orientation;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidWidget implements Renderable, LayoutElement {
    private final SingleFluidStorage fluidTank;
    private final Supplier<BlockPos> posSupplier;
    private final Orientation orientation;

    private final int width, height;
    private int x, y;

    public FluidWidget(SingleFluidStorage fluidTank, int x, int y, int width, int height, Supplier<BlockPos> posSupplier, Orientation orientation) {
        this.fluidTank = fluidTank;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.posSupplier = posSupplier;
        this.orientation = orientation;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Fluid fluid = this.fluidTank.variant.getFluid();
        long fluidAmount = this.fluidTank.getAmount();

        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (fluidRenderHandler == null || fluidAmount <= 0)
            return;

        BlockPos pos = this.posSupplier.get();
        FluidState fluidState = fluid.defaultFluidState();
        Level world = Minecraft.getInstance().level;

        TextureAtlasSprite stillTexture = fluidRenderHandler.getFluidSprites(world, pos, fluidState)[0];
        int tintColor = fluidRenderHandler.getFluidColor(world, pos, fluidState);

        float red = (tintColor >> 16 & 0xFF) / 255.0F;
        float green = (tintColor >> 8 & 0xFF) / 255.0F;
        float blue = (tintColor & 0xFF) / 255.0F;

        long fluidCapacity = this.fluidTank.getCapacity();
        float percentage = (float) fluidAmount / fluidCapacity;
        int fillX = this.x, fillY = this.y, fillWidth = this.width, fillHeight = this.height;
        if (orientation == Orientation.VERTICAL) {
            fillHeight = (int) (height * percentage);
            fillY = y + height - fillHeight;
        } else { // HORIZONTAL
            fillWidth = (int) (width * percentage);
        }

        ScreenUtils.renderTiledSprite(context, RenderPipelines.GUI_TEXTURED, stillTexture, fillX, fillY, fillWidth, fillHeight, ARGB.colorFromFloat(1.0F, red, green, blue));

        if (isPointWithinBounds(fillX, fillY, fillWidth, fillHeight, mouseX, mouseY)) {
            drawTooltip(context, mouseX, mouseY);
        }
    }

    protected void drawTooltip(GuiGraphics context, int mouseX, int mouseY) {
        Fluid fluid = this.fluidTank.variant.getFluid();

        long fluidAmount = this.fluidTank.getAmount();
        long fluidCapacity = this.fluidTank.getCapacity();

        Font textRenderer = Minecraft.getInstance().font;
        if (fluid != null && fluidAmount > 0) {
            List<Component> texts = List.of(
                    Component.translatable(fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId()),
                    Component.literal(((int) (((float) fluidAmount / FluidConstants.BUCKET) * 1000)) + " / " + ((int) (((float) fluidCapacity / FluidConstants.BUCKET) * 1000)) + " mB"));
            context.setComponentTooltipForNextFrame(textRenderer, texts, mouseX, mouseY);
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
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
    }

    private static boolean isPointWithinBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }

    public static class Builder {
        private final SingleFluidStorage fluidTank;
        private Supplier<BlockPos> posSupplier = () -> null;
        private int x, y;
        private int width, height;
        private Orientation orientation = Orientation.VERTICAL;

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

        public Builder orientation(Orientation orientation) {
            this.orientation = orientation;
            return this;
        }

        public FluidWidget build() {
            return new FluidWidget(fluidTank, x, y, width, height, posSupplier, orientation);
        }
    }
}
