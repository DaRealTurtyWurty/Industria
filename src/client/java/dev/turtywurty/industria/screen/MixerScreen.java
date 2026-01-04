package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.SlurryWidget;
import dev.turtywurty.industria.screenhandler.MixerScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MixerScreen extends AbstractContainerScreen<MixerScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/mixer.png");

    public MixerScreen(MixerScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 200, 194);
        this.inventoryLabelX = 20;
        this.inventoryLabelY = this.imageHeight - 92;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        addRenderableOnly(new EnergyWidget.Builder(this.menu.getBlockEntity().getEnergyStorage())
                .bounds(this.leftPos + 8, this.topPos + 10, 16, 89)
                .color(0xFFD4AF37)
                .build());

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getInputFluidTank())
                .bounds(this.leftPos + 34, this.topPos + 10, 20, 66)
                .posSupplier(() -> this.menu.getBlockEntity().getBlockPos())
                .build());

        addRenderableOnly(new SlurryWidget.Builder(this.menu.getBlockEntity().getOutputSlurryTank())
                .bounds(this.leftPos + 168, this.topPos + 10, 20, 66)
                .posSupplier(() -> this.menu.getBlockEntity().getBlockPos())
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 113, this.topPos + 36, 200, 0, this.menu.getProgressScaled(), 17);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }
}
