package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.screen.widget.util.Orientation;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.FluidRenderUtils;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class FluidWidget implements Renderable, LayoutElement {
    private Supplier<FluidRenderUtils.GuiFluidRenderData> renderDataOverrideSupplier;

    private final Supplier<FluidStack> fluidSupplier;
    private final LongSupplier capacitySupplier;
    private final Supplier<BlockPos> posSupplier;
    private final Orientation orientation;

    private final int width, height;
    private int x, y;

    public FluidWidget(
        SyncingFluidStorage fluidTank,
        int x,
        int y,
        int width,
        int height,
        Supplier<BlockPos> posSupplier,
        Orientation orientation
    ) {
        this(() -> null, fluidTank::getFluidStack, fluidTank::getCapacity, x, y, width, height, posSupplier,
            orientation);
    }

    public FluidWidget(
        Supplier<FluidRenderUtils.GuiFluidRenderData> renderDataOverrideSupplier,
        Supplier<FluidStack> fluidSupplier,
        LongSupplier capacitySupplier,
        int x,
        int y,
        int width,
        int height,
        Supplier<BlockPos> posSupplier,
        Orientation orientation
    ) {
        this.renderDataOverrideSupplier = renderDataOverrideSupplier;
        this.fluidSupplier = fluidSupplier;
        this.capacitySupplier = capacitySupplier;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.posSupplier = posSupplier;
        this.orientation = orientation;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Fluid fluid = this.fluidSupplier.get().variant().value();
        long fluidAmount = this.fluidSupplier.get().amount();

        if (fluidAmount <= 0)
            return;

        Level world = Minecraft.getInstance().level;
        BlockPos pos = this.posSupplier.get();
        FluidRenderUtils.GuiFluidRenderData renderData = this.renderDataOverrideSupplier.get();
        if (renderData == null) {
            renderData = FluidRenderUtils.getRenderData(fluid, world, pos);
        }

        if (renderData == null)
            return;

        long fluidCapacity = this.capacitySupplier.getAsLong();
        float percentage = Math.clamp((float) fluidAmount / fluidCapacity, 0.0f, 1.0f);
        int fillX = this.x, fillY = this.y, fillWidth = this.width, fillHeight = this.height;
        if (orientation == Orientation.VERTICAL) {
            fillHeight = (int) (height * percentage);
            fillY = y + height - fillHeight;
        } else { // HORIZONTAL
            fillWidth = (int) (width * percentage);
        }

        ScreenUtils.renderTiledSprite(context, RenderPipelines.GUI_TEXTURED, renderData.stillSprite(), fillX, fillY,
            fillWidth, fillHeight, renderData.tintColor());

        if (isPointWithinBounds(fillX, fillY, fillWidth, fillHeight, mouseX, mouseY)) {
            drawTooltip(context, mouseX, mouseY);
        }
    }

    protected void drawTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        Fluid fluid = this.fluidSupplier.get().variant().value();

        long fluidAmount = this.fluidSupplier.get().amount();
        long fluidCapacity = this.capacitySupplier.getAsLong();

        Font textRenderer = Minecraft.getInstance().font;
        if (fluid != null && fluidAmount > 0) {
            List<Component> texts = List.of(
                Component.translatable(fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId()),
                Component.literal(Math.round(fluidAmount * 1000.0 / FluidAmounts.BUCKET) + " / "
                    + Math.round(fluidCapacity * 1000.0 / FluidAmounts.BUCKET) + " mB"));
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

    public Orientation getOrientation() {
        return orientation;
    }

    public Supplier<FluidRenderUtils.GuiFluidRenderData> getRenderDataOverrideSupplier() {
        return renderDataOverrideSupplier;
    }

    public Supplier<FluidStack> getFluidSupplier() {
        return fluidSupplier;
    }

    public LongSupplier getCapacitySupplier() {
        return capacitySupplier;
    }

    public void setRenderDataOverrideSupplier(
        @Nullable Supplier<FluidRenderUtils.GuiFluidRenderData> renderDataOverrideSupplier
    ) {
        this.renderDataOverrideSupplier = renderDataOverrideSupplier == null
            ? () -> null
            : renderDataOverrideSupplier;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
    }

    private static boolean isPointWithinBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }

    public static class Builder {
        private final Supplier<FluidStack> fluidSupplier;
        private final LongSupplier capacitySupplier;
        private Supplier<FluidRenderUtils.GuiFluidRenderData> renderDataOverrideSupplier = () -> null;
        private Supplier<BlockPos> posSupplier = () -> null;
        private int x, y;
        private int width, height;
        private Orientation orientation = Orientation.VERTICAL;

        public Builder(Supplier<FluidStack> fluidSupplier, LongSupplier capacitySupplier) {
            this.fluidSupplier = fluidSupplier;
            this.capacitySupplier = capacitySupplier;
        }

        public Builder(SyncingFluidStorage fluidTank) {
            this.fluidSupplier = fluidTank::getFluidStack;
            this.capacitySupplier = fluidTank::getCapacity;
        }

        public Builder renderDataOverrideSupplier(
            Supplier<FluidRenderUtils.GuiFluidRenderData> renderDataOverrideSupplier
        ) {
            this.renderDataOverrideSupplier = renderDataOverrideSupplier == null
                ? () -> null
                : renderDataOverrideSupplier;
            return this;
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
            return new FluidWidget(renderDataOverrideSupplier, fluidSupplier, capacitySupplier, x, y, width, height,
                posSupplier, orientation);
        }
    }
}
