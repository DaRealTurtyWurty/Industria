package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.menu.CokeOvenMenu;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import dev.turtywurty.industria.blockentity.CokeOvenBlockEntity;

public class CokeOvenScreen extends AbstractContainerScreen<CokeOvenMenu> {
    private static final Identifier FURNACE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier FLAME = Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final Identifier ARROW = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final Identifier TANK = Industria.id("textures/gui/container/fluid_tank.png");

    public CokeOvenScreen(CokeOvenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        CokeOvenBlockEntity oven = this.menu.getBlockEntity();
        addRenderableOnly(new FluidWidget.Builder(oven.getOutputTank())
            .bounds(this.leftPos + 147, this.topPos + 18, 20, 46)
            .posSupplier(oven::getBlockPos)
            .build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        ScreenUtils.drawTexture(graphics, FURNACE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        ScreenUtils.drawTexture(graphics, TANK, this.leftPos + 146, this.topPos + 17, 77, 13, 22, 48);

        int flameHeight = Mth.ceil(this.menu.getBurnTimePercent() * 14);
        if (flameHeight > 0) {
            int start = 14 - flameHeight;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FLAME, 14, 14, 0, start,
                this.leftPos + 56, this.topPos + 36 + start, 14, flameHeight);
        }

        int arrowWidth = Mth.ceil(this.menu.getProgressPercent() * 24);
        if (arrowWidth > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ARROW, 24, 16, 0, 0,
                this.leftPos + 79, this.topPos + 34, arrowWidth, 16);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        String temperature = this.menu.getTemperature() + " °C";
        graphics.text(this.font, temperature, 110 - this.font.width(temperature) / 2, 60, 0xFF404040, false);
    }
}
