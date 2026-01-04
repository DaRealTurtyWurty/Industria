package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.AlloyFurnaceScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class AlloyFurnaceScreen extends AbstractContainerScreen<AlloyFurnaceScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/alloy_furnace.png");

    public AlloyFurnaceScreen(AlloyFurnaceScreenHandler handler, Inventory inventory, Component title) {
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
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 79, this.topPos + 34, 176, 14, progress, 17);

        int burnTime = Mth.ceil(this.menu.getBurnTimePercent() * 14);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 56, this.topPos + 36 + 14 - burnTime, 176, 14 - burnTime, 14, burnTime);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }
}
