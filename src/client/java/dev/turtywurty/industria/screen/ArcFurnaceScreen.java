package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.GasWidget;
import dev.turtywurty.industria.screen.widget.util.Orientation;
import dev.turtywurty.industria.screenhandler.ArcFurnaceScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ArcFurnaceScreen extends HandledScreen<ArcFurnaceScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/arc_furnace.png");

    public ArcFurnaceScreen(ArcFurnaceScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 176;
        this.backgroundHeight = 201;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        addDrawable(new EnergyWidget.Builder(this.handler.getBlockEntity().getEnergyStorage())
                .bounds(this.x + 8, this.y + 18, 12, 52)
                .build());

        addDrawable(new FluidWidget.Builder(this.handler.getBlockEntity().getFluidStorage())
                .bounds(this.x + 26, this.y + 94, 138, 10)
                .posSupplier(this.handler.getBlockEntity()::getPos)
                .orientation(Orientation.HORIZONTAL)
                .build());

        addDrawable(new GasWidget.Builder(this.handler.getBlockEntity().getGasStorage())
                .bounds(this.x + 26, this.y + 80, 138, 10)
                .posSupplier(this.handler.getBlockEntity()::getPos)
                .orientation(Orientation.HORIZONTAL)
                .build());

        // TODO: Add triple toggle button for the mode
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        ScreenUtils.drawTexture(context, TEXTURE, this.x + 83, this.y + 37, 176, 14, this.handler.getProgressScaled(), 17);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
