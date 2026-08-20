package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.ThermalGeneratorScreenHandler;
import dev.turtywurty.industria.util.FluidRenderUtils;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public class ThermalGeneratorScreen extends AbstractContainerScreen<ThermalGeneratorScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/thermal_generator.png");

    public ThermalGeneratorScreen(ThermalGeneratorScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        long energy = this.menu.getEnergy();
        long maxEnergy = this.menu.getMaxEnergy();
        int energyBarHeight = Math.round((float) energy / maxEnergy * 60);
        context.fill(this.leftPos + 8, this.topPos + 8 + 60 - energyBarHeight, this.leftPos + 16, this.topPos + 68, 0xFFD4AF37);

        Fluid fluid = this.menu.getFluid();
        long fluidAmount = this.menu.getFluidAmount();
        long fluidCapacity = this.menu.getFluidCapacity();
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
            Fluid fluid = this.menu.getFluid();
            long fluidAmount = this.menu.getFluidAmount();
            long fluidCapacity = this.menu.getFluidCapacity();
            if (fluid != null && fluidAmount > 0) {
                context.setTooltipForNextFrame(this.font, Component.translatable(fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId()), mouseX, mouseY);
                context.setTooltipForNextFrame(this.font, Component.literal(fluidAmount + " / " + fluidCapacity + " mB"), mouseX, mouseY + 10);
            }
        }

        if (isHovering(8, 8, 8, 60, mouseX, mouseY)) {
            long energy = this.menu.getEnergy();
            long maxEnergy = this.menu.getMaxEnergy();
            context.setTooltipForNextFrame(this.font, Component.literal(energy + " / " + maxEnergy + " FE"), mouseX, mouseY);
        }
    }
}
