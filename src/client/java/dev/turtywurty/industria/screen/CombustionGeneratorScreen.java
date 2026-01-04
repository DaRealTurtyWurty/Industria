package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.CombustionGeneratorScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CombustionGeneratorScreen extends AbstractContainerScreen<CombustionGeneratorScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/combustion_generator.png");

    public CombustionGeneratorScreen(CombustionGeneratorScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        long energy = this.menu.getEnergy();
        long maxEnergy = this.menu.getMaxEnergy();
        int energyBarSize = Math.round((float) energy / maxEnergy * 66);
        context.fill(this.leftPos + 144, this.topPos + 10 + 66 - energyBarSize, this.leftPos + 164, this.topPos + 10 + 66, 0xFFD4AF37);

        int burnTime = this.menu.getBurnTime();
        int fuelTime = this.menu.getFuelTime();
        float burnTimePercentage = (float) burnTime / fuelTime;
        int burnTimeSize = Math.round(burnTimePercentage * 14);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 82, this.topPos + 25 + 14 - burnTimeSize, 176, 14 - burnTimeSize, 14, burnTimeSize);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);

        if (isHovering(144, 10, 20, 66, mouseX, mouseY)) {
            long energy = this.menu.getEnergy();
            long maxEnergy = this.menu.getMaxEnergy();
            context.setTooltipForNextFrame(this.font, Component.literal(energy + " / " + maxEnergy + " FE"), mouseX, mouseY);
        }
    }
}
