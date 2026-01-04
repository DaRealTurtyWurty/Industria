package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screenhandler.FluidPumpScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class FluidPumpScreen extends AbstractContainerScreen<FluidPumpScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/fluid_pump.png");

    public FluidPumpScreen(FluidPumpScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        addRenderableOnly(new EnergyWidget.Builder(this.menu.getBlockEntity().getEnergyStorage())
                .bounds(this.leftPos + 10, this.topPos + 10, 20, 66)
                .color(0xFFD4AF37)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        SingleFluidStorage tank = this.menu.getBlockEntity().getFluidTank();
        Fluid fluid = tank.variant.getFluid();
        long fluidAmount = tank.getAmount();
        long fluidCapacity = tank.getCapacity();
        int fluidBarHeight = Math.round((float) fluidAmount / fluidCapacity * 60);

        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (fluidRenderHandler == null || fluidAmount <= 0)
            return;

        BlockPos pos = this.menu.getBlockEntity().getBlockPos();
        FluidState fluidState = fluid.defaultFluidState();
        Level world = this.menu.getBlockEntity().getLevel();

        TextureAtlasSprite stillTexture = fluidRenderHandler.getFluidSprites(world, pos, fluidState)[0];
        int tintColor = fluidRenderHandler.getFluidColor(world, pos, fluidState);

        float red = (tintColor >> 16 & 0xFF) / 255.0F;
        float green = (tintColor >> 8 & 0xFF) / 255.0F;
        float blue = (tintColor & 0xFF) / 255.0F;
        context.blitSprite(RenderPipelines.GUI_TEXTURED, stillTexture, this.leftPos + 146, this.topPos + 8 + 60 - fluidBarHeight, 16, fluidBarHeight, ARGB.colorFromFloat(1.0F, red, green, blue));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);

        if (isHovering(146, 8, 16, 60, mouseX, mouseY)) {
            SingleFluidStorage tank = this.menu.getBlockEntity().getFluidTank();
            Fluid fluid = tank.variant.getFluid();
            long fluidAmount = tank.getAmount();
            long fluidCapacity = tank.getCapacity();

            if (fluid != null && fluidAmount > 0) {
                context.setTooltipForNextFrame(this.font, Component.translatable(fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId()), mouseX, mouseY);
                context.setTooltipForNextFrame(this.font, Component.literal(((int) (((float) fluidAmount / FluidConstants.BUCKET) * 1000)) + " / " + ((int) (((float) fluidCapacity / FluidConstants.BUCKET) * 1000)) + " mB"), mouseX, mouseY + 10);
            }
        }
    }
}
