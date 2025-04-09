package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.GasWidget;
import dev.turtywurty.industria.screenhandler.ElectrolyzerScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ElectrolyzerScreen extends HandledScreen<ElectrolyzerScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/electrolyzer.png");

    public ElectrolyzerScreen(ElectrolyzerScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.backgroundWidth = 200;
        this.backgroundHeight = 221;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
        this.playerInventoryTitleX = 20;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        addDrawable(new EnergyWidget.Builder(this.handler.getBlockEntity().getEnergyStorage())
                .bounds(this.x + 8, this.y + 17, 184, 8)
                .color(0xFFD4AF37)
                .orientation(EnergyWidget.Orientation.HORIZONTAL)
                .build());

        addDrawable(new FluidWidget.Builder(this.handler.getBlockEntity().getElectrolyteFluidStorage())
                .bounds(this.x + 58, this.y + 81, 84, 32)
                .posSupplier(this.handler.getBlockEntity()::getPos)
                .build());

        addDrawable(new FluidWidget.Builder(this.handler.getBlockEntity().getOutputFluidStorage())
                .bounds(this.x + 148, this.y + 30, 20, 80)
                .posSupplier(this.handler.getBlockEntity()::getPos)
                .build());

        addDrawable(new GasWidget.Builder(this.handler.getBlockEntity().getOutputGasStorage())
                .bounds(this.x + 172, this.y + 30, 20, 80)
                .posSupplier(this.handler.getBlockEntity()::getPos)
                .build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        ScreenUtils.drawTexture(context, TEXTURE, this.x + 89, this.y + 44, 200, 0, this.handler.getProgressScaled(), 17);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
