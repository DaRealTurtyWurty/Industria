package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screenhandler.FluidPumpScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;

public class FluidPumpScreen extends HandledScreen<FluidPumpScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/fluid_pump.png");

    public FluidPumpScreen(FluidPumpScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        addDrawable(new EnergyWidget.Builder(this.handler.getBlockEntity().getEnergyStorage())
                .bounds(this.x + 10, this.y + 10, 20, 66)
                .color(0xFFD4AF37)
                .build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        SingleFluidStorage tank = this.handler.getBlockEntity().getFluidTank();
        Fluid fluid = tank.variant.getFluid();
        long fluidAmount = tank.getAmount();
        long fluidCapacity = tank.getCapacity();
        int fluidBarHeight = Math.round((float) fluidAmount / fluidCapacity * 60);

        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (fluidRenderHandler == null || fluidAmount <= 0)
            return;

        BlockPos pos = this.handler.getBlockEntity().getPos();
        FluidState fluidState = fluid.getDefaultState();
        World world = this.handler.getBlockEntity().getWorld();

        Sprite stillTexture = fluidRenderHandler.getFluidSprites(world, pos, fluidState)[0];
        int tintColor = fluidRenderHandler.getFluidColor(world, pos, fluidState);

        float red = (tintColor >> 16 & 0xFF) / 255.0F;
        float green = (tintColor >> 8 & 0xFF) / 255.0F;
        float blue = (tintColor & 0xFF) / 255.0F;
        context.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, stillTexture, this.x + 146, this.y + 8 + 60 - fluidBarHeight, 16, fluidBarHeight, ColorHelper.fromFloats(1.0F, red, green, blue));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        if (isPointWithinBounds(146, 8, 16, 60, mouseX, mouseY)) {
            SingleFluidStorage tank = this.handler.getBlockEntity().getFluidTank();
            Fluid fluid = tank.variant.getFluid();
            long fluidAmount = tank.getAmount();
            long fluidCapacity = tank.getCapacity();

            if (fluid != null && fluidAmount > 0) {
                context.drawTooltip(this.textRenderer, Text.translatable(fluid.getDefaultState().getBlockState().getBlock().getTranslationKey()), mouseX, mouseY);
                context.drawTooltip(this.textRenderer, Text.literal(((int) (((float) fluidAmount / FluidConstants.BUCKET) * 1000)) + " / " + ((int) (((float) fluidCapacity / FluidConstants.BUCKET) * 1000)) + " mB"), mouseX, mouseY + 10);
            }
        }
    }
}
