package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.GasWidget;
import dev.turtywurty.industria.screen.widget.util.Orientation;
import dev.turtywurty.industria.screenhandler.ElectrolyzerScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ElectrolyzerScreen extends AbstractContainerScreen<ElectrolyzerScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/electrolyzer.png");

    public ElectrolyzerScreen(ElectrolyzerScreenHandler handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title, 200, 221);
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = 20;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        addRenderableOnly(new EnergyWidget.Builder(this.menu.getBlockEntity().getEnergyStorage())
                .bounds(this.leftPos + 8, this.topPos + 17, 184, 8)
                .color(0xFFD4AF37)
                .orientation(Orientation.HORIZONTAL)
                .build());

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getElectrolyteFluidStorage())
                .bounds(this.leftPos + 58, this.topPos + 81, 84, 32)
                .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                .build());

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getOutputFluidStorage())
                .bounds(this.leftPos + 148, this.topPos + 30, 20, 80)
                .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                .build());

        addRenderableOnly(new GasWidget.Builder(this.menu.getBlockEntity().getOutputGasStorage())
                .bounds(this.leftPos + 172, this.topPos + 30, 20, 80)
                .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 89, this.topPos + 44, 200, 0, this.menu.getProgressScaled(), 17);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }
}
