package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.SlurryWidget;
import dev.turtywurty.industria.screenhandler.MixerScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MixerScreen extends HandledScreen<MixerScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/mixer.png");

    public MixerScreen(MixerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 200;
        this.backgroundHeight = 194;
        this.playerInventoryTitleX = 20;
        this.playerInventoryTitleY = this.backgroundHeight - 92;
    }

    @Override
    protected void init() {
        super.init();

        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        addDrawable(new EnergyWidget.Builder(this.handler.getBlockEntity().getEnergyStorage())
                .bounds(this.x + 8, this.y + 10, 16, 89)
                .color(0xFFD4AF37)
                .build());

        addDrawable(new FluidWidget.Builder(this.handler.getBlockEntity().getInputFluidTank())
                .bounds(this.x + 34, this.y + 10, 20, 66)
                .posSupplier(() -> this.handler.getBlockEntity().getPos())
                .build());

        addDrawable(new SlurryWidget.Builder(this.handler.getBlockEntity().getOutputSlurryTank())
                .bounds(this.x + 168, this.y + 10, 20, 66)
                .posSupplier(() -> this.handler.getBlockEntity().getPos())
                .build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        ScreenUtils.drawTexture(context, TEXTURE, this.x + 113, this.y + 36, 200, 0, this.handler.getProgressScaled(), 17);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
