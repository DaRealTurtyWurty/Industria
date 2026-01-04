package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screenhandler.CrystallizerScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CrystallizerScreen extends AbstractContainerScreen<CrystallizerScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/crystallizer.png");
    public CrystallizerScreen(CrystallizerScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 174);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getWaterFluidStorage())
                .bounds(this.leftPos + 8, this.topPos + 12, 20, 66)
                .posSupplier(() -> this.menu.getBlockEntity().getBlockPos())
                .build());

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getCrystalFluidStorage())
                .bounds(this.leftPos + 31, this.topPos + 12, 20, 66)
                .posSupplier(() -> this.menu.getBlockEntity().getBlockPos())
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 94, this.topPos + 38, 176, 0, this.menu.getProgressScaled(), 17);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        float catalystUsesPercent = this.menu.getCatalystUsesPercent();
        if(catalystUsesPercent > 0) {
            int width = (int) (18 * catalystUsesPercent);
            context.fill(this.leftPos + 52, this.topPos + 27, this.leftPos + 53 + width, this.topPos + 29, 0xaaFF0000);
        }

        renderTooltip(context, mouseX, mouseY);
    }
}
