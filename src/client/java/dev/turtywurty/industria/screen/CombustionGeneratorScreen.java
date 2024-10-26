package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.CombustionGeneratorScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CombustionGeneratorScreen extends HandledScreen<CombustionGeneratorScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/combustion_generator.png");

    public CombustionGeneratorScreen(CombustionGeneratorScreenHandler handler, PlayerInventory inventory, Text title) {
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

        long energy = this.handler.getEnergy();
        long maxEnergy = this.handler.getMaxEnergy();
        int energyBarSize = Math.round((float) energy / maxEnergy * 66);
        context.fill(this.x + 144, this.y + 10 + 66 - energyBarSize, this.x + 164, this.y + 10 + 66, 0xFFD4AF37);

        int burnTime = this.handler.getBurnTime();
        int fuelTime = this.handler.getFuelTime();
        float burnTimePercentage = (float) burnTime / fuelTime;
        int burnTimeSize = Math.round(burnTimePercentage * 14);
        ScreenUtils.drawTexture(context, TEXTURE, this.x + 82, this.y + 25 + 14 - burnTimeSize, 176, 14 - burnTimeSize, 14, burnTimeSize);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        if (isPointWithinBounds(144, 10, 20, 66, mouseX, mouseY)) {
            long energy = this.handler.getEnergy();
            long maxEnergy = this.handler.getMaxEnergy();
            context.drawTooltip(this.textRenderer, Text.literal(energy + " / " + maxEnergy + " FE"), mouseX, mouseY);
        }
    }
}
