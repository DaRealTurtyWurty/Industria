package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.SlurryWidget;
import dev.turtywurty.industria.screenhandler.DigesterScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class DigesterScreen extends AbstractContainerScreen<DigesterScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/digester.png");

    public DigesterScreen(DigesterScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 174);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        addRenderableOnly(new EnergyWidget.Builder(this.menu.getBlockEntity().getEnergyStorage())
                .bounds(this.leftPos + 8, this.topPos + 10, 12, 66)
                .color(0xFFD4AF37)
                .build());

        addRenderableOnly(new SlurryWidget.Builder(this.menu.getBlockEntity().getInputSlurryStorage())
                .bounds(this.leftPos + 24, this.topPos + 10, 20, 46)
                .posSupplier(() -> this.menu.getBlockEntity().getBlockPos())
                .build());

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getOutputFluidStorage())
                .bounds(this.leftPos + 132, this.topPos + 10, 20, 46)
                .posSupplier(() -> this.menu.getBlockEntity().getBlockPos())
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 76, this.topPos + 36, 176, 0, this.menu.getProgressScaled(), 17);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }
}
