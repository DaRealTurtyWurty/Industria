package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.CrusherScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class CrusherScreen extends AbstractContainerScreen<CrusherScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/crusher.png");

    public CrusherScreen(CrusherScreenHandler handler, Inventory inventory, Component title) {
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

        int progress = Mth.ceil(this.menu.getProgressPercent() * 24);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 67, this.topPos + 35, 176, 0, progress, 17);

        int energy = Mth.ceil(this.menu.getEnergyPercent() * 66);
        context.fill(this.leftPos + 144, this.topPos + 10 + 66 - energy, this.leftPos + 144 + 20, this.topPos + 10 + 66, 0xFFD4AF37);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);

        if(isHovering(144, 10, 20, 66, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, Component.literal("Energy: " + this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        }
    }
}
