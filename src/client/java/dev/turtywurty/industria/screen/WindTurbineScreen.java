package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.WindTurbineScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class WindTurbineScreen extends HandledScreen<WindTurbineScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/wind_turbine.png");

    public WindTurbineScreen(WindTurbineScreenHandler handler, PlayerInventory inventory, Text title) {
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

        int energySize = MathHelper.ceil(this.handler.getEnergyPercent() * 66);
        context.fill(this.x + 144, this.y + 10 + 66 - energySize, this.x + 144 + 20, this.y + 10 + 66, 0xFFD4AF37);

        if (this.client == null || this.client.world == null)
            return;

        int energyOutputSize = MathHelper.ceil(this.handler.getEnergyPerTickPercent() * 21);
        context.drawTexture(TEXTURE, this.x + 36, this.y + 33, 176, 0, energyOutputSize, 21);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        if (isPointWithinBounds(144, 10, 20, 66, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, Text.literal("Energy: " + this.handler.getEnergy() + " / " + this.handler.getMaxEnergy() + " FE"), mouseX, mouseY);
        }

        if (isPointWithinBounds(36, 33, 21, 21, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, List.of(
                    Text.literal("Energy Output: %s FE/t".formatted(this.handler.getEnergyPerTick())),
                    Text.literal("Wind Capture: %d%%".formatted((int) MathHelper.clamp(this.handler.getEnergyPerTickPercent() * 100, 0, 100)))
            ), mouseX, mouseY);
        }
    }
}
