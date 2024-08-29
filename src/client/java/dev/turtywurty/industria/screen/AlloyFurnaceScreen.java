package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.AlloyFurnaceScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class AlloyFurnaceScreen extends HandledScreen<AlloyFurnaceScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/alloy_furnace.png");

    public AlloyFurnaceScreen(AlloyFurnaceScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int progress = MathHelper.ceil(this.handler.getProgressPercent() * 24);
        context.drawTexture(TEXTURE, this.x + 79, this.y + 34, 176, 14, progress, 17);

        int burnTime = MathHelper.ceil(this.handler.getBurnTimePercent() * 14);
        context.drawTexture(TEXTURE, this.x + 56, this.y + 36 + 14 - burnTime, 176, 14 - burnTime, 14, burnTime);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
