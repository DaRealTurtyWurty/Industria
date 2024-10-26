package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.CrusherScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class CrusherScreen extends HandledScreen<CrusherScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/crusher.png");

    public CrusherScreen(CrusherScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int progress = MathHelper.ceil(this.handler.getProgressPercent() * 24);
        ScreenUtils.drawTexture(context, TEXTURE, this.x + 67, this.y + 35, 176, 0, progress, 17);

        int energy = MathHelper.ceil(this.handler.getEnergyPercent() * 66);
        context.fill(this.x + 144, this.y + 10 + 66 - energy, this.x + 144 + 20, this.y + 10 + 66, 0xFFD4AF37);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        if(isPointWithinBounds(144, 10, 20, 66, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, Text.literal("Energy: " + this.handler.getEnergy() + " / " + this.handler.getMaxEnergy() + " FE"), mouseX, mouseY);
        }
    }
}
