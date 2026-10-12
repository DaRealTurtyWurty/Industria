package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.menu.FuelboxMenu;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class FuelboxScreen extends AbstractContainerScreen<FuelboxMenu> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/fuelbox.png");

    public FuelboxScreen(FuelboxMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.isBurning()) {
            int width = Mth.ceil(this.menu.getBurnProgress() * 52);
            context.fill(this.leftPos + 62, this.topPos + 59, this.leftPos + 62 + width, this.topPos + 65, 0xFFFF9800);
        }
    }
}
