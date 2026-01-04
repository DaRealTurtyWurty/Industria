package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.GasWidget;
import dev.turtywurty.industria.screen.widget.util.Orientation;
import dev.turtywurty.industria.screenhandler.ArcFurnaceScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ArcFurnaceScreen extends AbstractContainerScreen<ArcFurnaceScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/arc_furnace.png");

    public ArcFurnaceScreen(ArcFurnaceScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 201);

        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        addRenderableOnly(new EnergyWidget.Builder(this.menu.getBlockEntity().getEnergyStorage())
                .bounds(this.leftPos + 8, this.topPos + 18, 12, 52)
                .build());

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getFluidStorage())
                .bounds(this.leftPos + 26, this.topPos + 94, 138, 10)
                .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                .orientation(Orientation.HORIZONTAL)
                .build());

        addRenderableOnly(new GasWidget.Builder(this.menu.getBlockEntity().getGasStorage())
                .bounds(this.leftPos + 26, this.topPos + 80, 138, 10)
                .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                .orientation(Orientation.HORIZONTAL)
                .build());

        // TODO: Add triple toggle button for the mode
    }

    @Override
    protected void renderBg(GuiGraphics context, float deltaTicks, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 83, this.topPos + 37, 176, 14, this.menu.getProgressScaled(), 17);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        renderTooltip(context, mouseX, mouseY);
    }
}
