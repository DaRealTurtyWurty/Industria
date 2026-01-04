package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screenhandler.ClarifierScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ClarifierScreen extends AbstractContainerScreen<ClarifierScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/clarifier.png");

    public ClarifierScreen(ClarifierScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 176);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getInputFluidTank())
                .bounds(this.leftPos + 24, this.topPos + 10, 20, 66)
                .posSupplier(() -> this.menu.getBlockEntity().getBlockPos())
                .build());

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getOutputFluidTank())
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
