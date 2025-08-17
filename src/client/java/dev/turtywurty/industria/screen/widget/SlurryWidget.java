package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.fabricslurryapi.api.Slurry;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.fabricslurryapi.client.handler.SlurryRenderHandler;
import dev.turtywurty.fabricslurryapi.client.handler.SlurryRenderHandlerRegistry;
import dev.turtywurty.industria.screen.widget.util.Orientation;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SlurryWidget implements Drawable, Widget {
    private final SingleSlurryStorage slurryTank;
    private final Supplier<BlockPos> posSupplier;
    private final Orientation orientation;

    private final int width, height;
    private int x, y;

    public SlurryWidget(SingleSlurryStorage slurryTank, int x, int y, int width, int height, Supplier<BlockPos> posSupplier, Orientation orientation) {
        this.slurryTank = slurryTank;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.posSupplier = posSupplier;
        this.orientation = orientation;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Slurry slurry = this.slurryTank.variant.getSlurry();
        long amount = this.slurryTank.getAmount();

        SlurryRenderHandler slurryRenderHandler = SlurryRenderHandlerRegistry.get(slurry);
        if (slurryRenderHandler == null || amount <= 0)
            return;

        BlockPos pos = this.posSupplier.get();
        World world = MinecraftClient.getInstance().world;

        Sprite stillTexture = slurryRenderHandler.getSprite(world, pos);
        int tintColor = slurryRenderHandler.getColor(world, pos);

        float red = (tintColor >> 16 & 0xFF) / 255.0F;
        float green = (tintColor >> 8 & 0xFF) / 255.0F;
        float blue = (tintColor & 0xFF) / 255.0F;

        long capacity = this.slurryTank.getCapacity();
        float percentage = (float) amount / capacity;
        int fillX = this.x, fillY = this.y, fillWidth = this.width, fillHeight = this.height;
        if (orientation == Orientation.VERTICAL) {
            fillHeight = (int) (height * percentage);
            fillY = y + height - fillHeight;
        } else { // HORIZONTAL
            fillWidth = (int) (width * percentage);
        }

        ScreenUtils.renderTiledSprite(context, RenderPipelines.GUI_TEXTURED, stillTexture, fillX, fillY, fillWidth, fillHeight, ColorHelper.fromFloats(1.0F, red, green, blue));

        if (isPointWithinBounds(fillX, fillY, fillWidth, fillHeight, mouseX, mouseY)) {
            drawTooltip(context, mouseX, mouseY);
        }
    }

    protected void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        Slurry slurry = this.slurryTank.variant.getSlurry();

        long amount = this.slurryTank.getAmount();
        long capacity = this.slurryTank.getCapacity();

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (slurry != null && amount > 0) {
            context.drawTooltip(textRenderer, Text.translatable("slurry." + slurry.id().split(":")[0] + "." + slurry.id().split(":")[1]), mouseX, mouseY);
            context.drawTooltip(textRenderer, Text.literal(((int) (((float) amount / FluidConstants.BUCKET) * 1000)) + " / " + ((int) (((float) capacity / FluidConstants.BUCKET) * 1000)) + " mB"), mouseX, mouseY + 10);
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

    public SingleSlurryStorage getSlurryTank() {
        return slurryTank;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
    }

    private static boolean isPointWithinBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }

    public static class Builder {
        private final SingleSlurryStorage slurryTank;
        private Supplier<BlockPos> posSupplier = () -> null;
        private int x, y;
        private int width, height;
        private Orientation orientation = Orientation.VERTICAL;

        public Builder(SingleSlurryStorage slurryTank) {
            this.slurryTank = slurryTank;
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

        public SlurryWidget build() {
            return new SlurryWidget(slurryTank, x, y, width, height, posSupplier, orientation);
        }
    }
}
