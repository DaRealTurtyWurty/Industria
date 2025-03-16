package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screenhandler.ClarifierScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ClarifierScreen extends HandledScreen<ClarifierScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/clarifier.png");

    public ClarifierScreen(ClarifierScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 176;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        addDrawable(new FluidWidget.Builder(this.handler.getBlockEntity().getInputFluidTank())
                .bounds(this.x + 24, this.y + 10, 20, 66)
                .posSupplier(() -> this.handler.getBlockEntity().getPos())
                .build());

        addDrawable(new FluidWidget.Builder(this.handler.getBlockEntity().getOutputFluidTank())
                .bounds(this.x + 132, this.y + 10, 20, 46)
                .posSupplier(() -> this.handler.getBlockEntity().getPos())
                .build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        ScreenUtils.drawTexture(context, TEXTURE, this.x + 76, this.y + 36, 176, 0, this.handler.getProgressScaled(), 17);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
