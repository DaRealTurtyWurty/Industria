package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screenhandler.CrystallizerScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CrystallizerScreen extends HandledScreen<CrystallizerScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/crystallizer.png");
    public CrystallizerScreen(CrystallizerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 176;
        this.backgroundWidth = 174;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        addDrawable(new FluidWidget.Builder(this.handler.getBlockEntity().getWaterFluidStorage())
                .bounds(this.x + 8, this.y + 12, 20, 66)
                .posSupplier(() -> this.handler.getBlockEntity().getPos())
                .build());

        addDrawable(new FluidWidget.Builder(this.handler.getBlockEntity().getCrystalFluidStorage())
                .bounds(this.x + 31, this.y + 12, 20, 66)
                .posSupplier(() -> this.handler.getBlockEntity().getPos())
                .build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        ScreenUtils.drawTexture(context, TEXTURE, this.x + 94, this.y + 38, 176, 0, this.handler.getProgressScaled(), 17);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        float catalystUsesPercent = this.handler.getCatalystUsesPercent();
        if(catalystUsesPercent > 0) {
            int width = (int) (18 * catalystUsesPercent);
            context.fill(this.x + 52, this.y + 27, this.x + 53 + width, this.y + 29, 0xaaFF0000);
        }

        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
