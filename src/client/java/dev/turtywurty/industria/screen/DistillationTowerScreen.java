package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.util.Orientation;
import dev.turtywurty.industria.screenhandler.DistillationTowerScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class DistillationTowerScreen extends AbstractContainerScreen<DistillationTowerScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/distillation_tower.png");

    public DistillationTowerScreen(DistillationTowerScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        addRenderableOnly(new EnergyWidget.Builder(this.menu.getBlockEntity().getEnergyStorage())
                .bounds(this.leftPos + 26, this.topPos + 17, 9, 52)
                .color(0xFFD4AF37)
                .orientation(Orientation.VERTICAL)
                .build());

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getInputFluidTank())
                .bounds(this.leftPos + 44, this.topPos + 17, 16, 52)
                .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                .build());

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getPrimaryOutputFluidTank())
                .bounds(this.leftPos + 116, this.topPos + 17, 16, 52)
                .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                .build());

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getSecondaryOutputFluidTank())
                .bounds(this.leftPos + 134, this.topPos + 17, 16, 52)
                .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                .build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 76, this.topPos + 33, 176, 0, this.menu.getProgressScaled(), 17);
    }
}
