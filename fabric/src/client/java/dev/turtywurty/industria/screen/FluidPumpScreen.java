package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screenhandler.FluidPumpScreenHandler;
import dev.turtywurty.industria.util.FluidRenderUtils;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

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
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        SingleFluidStorage tank = this.menu.getBlockEntity().getFluidTank();
        Fluid fluid = tank.variant.getFluid();
        long fluidAmount = tank.getAmount();
        long fluidCapacity = tank.getCapacity();
        int fluidBarHeight = Math.round((float) fluidAmount / fluidCapacity * 60);

        if (fluidAmount <= 0)
            return;

        BlockPos pos = this.menu.getBlockEntity().getBlockPos();
        Level world = this.menu.getBlockEntity().getLevel();
        FluidRenderUtils.GuiFluidRenderData renderData = FluidRenderUtils.getRenderData(fluid, world, pos);
        if (renderData == null)
            return;

        context.blitSprite(RenderPipelines.GUI_TEXTURED, renderData.stillSprite(), this.leftPos + 146, this.topPos + 8 + 60 - fluidBarHeight, 16, fluidBarHeight, renderData.tintColor());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractTooltip(context, mouseX, mouseY);

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
