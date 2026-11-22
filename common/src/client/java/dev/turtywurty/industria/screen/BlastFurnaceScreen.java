package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.menu.BlastFurnaceMenu;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import dev.turtywurty.industria.blockentity.BlastFurnaceBlockEntity;
import net.minecraft.world.inventory.Slot;

public class BlastFurnaceScreen extends AbstractContainerScreen<BlastFurnaceMenu> {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier PROGRESS = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final int LABEL_COLOR = 0xFF404040;
    private static final int TANK_X = 145;
    private static final int TANK_Y = 34;
    private static final int TANK_WIDTH = 20;
    private static final int TANK_HEIGHT = 26;

    private static final Component CHARGE = Component.translatable("container.industria.blast_furnace.charge");
    private static final Component COKE = Component.translatable("container.industria.blast_furnace.coke");
    private static final Component SLAG = Component.translatable("container.industria.blast_furnace.slag");
    private static final Component MOLTEN = Component.translatable("container.industria.blast_furnace.molten");
    private static final Component BLOWER_CONNECTED = Component
        .translatable("container.industria.blast_furnace.blower_connected");
    private static final Component BLOWER_MISSING = Component
        .translatable("container.industria.blast_furnace.blower_missing");

    public BlastFurnaceScreen(BlastFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        BlastFurnaceBlockEntity furnace = this.menu.getBlockEntity();
        addRenderableOnly(new FluidWidget.Builder(furnace.getMoltenOutput())
            .bounds(this.leftPos + TANK_X, this.topPos + TANK_Y, TANK_WIDTH, TANK_HEIGHT)
            .posSupplier(furnace::getBlockPos)
            .build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        ScreenUtils.drawTexture(graphics, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth,
            this.imageHeight);
        graphics.fill(this.leftPos + 7, this.topPos + 16, this.leftPos + 169, this.topPos + 72, 0xFFC6C6C6);
        for (int i = 0; i < 4; i++) {
            Slot slot = this.menu.getSlot(i);
            ScreenUtils.drawTexture(graphics, BACKGROUND,
                this.leftPos + slot.x - 1, this.topPos + slot.y - 1, 55, 16, 18, 18);
        }

        int tankX = this.leftPos + TANK_X;
        int tankY = this.topPos + TANK_Y;
        graphics.fill(tankX - 1, tankY - 1, tankX + TANK_WIDTH + 1, tankY + TANK_HEIGHT + 1, 0xFF626262);
        graphics.fill(tankX, tankY, tankX + TANK_WIDTH, tankY + TANK_HEIGHT, 0xFF313131);

        int arrowX = this.leftPos + 88;
        int arrowY = this.topPos + 36;
        ScreenUtils.drawTexture(graphics, BACKGROUND, arrowX, arrowY, 79, 34, 24, 16);
        int progress = Mth.ceil(this.menu.getBatchProgress() * 24);
        if (progress > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS, 24, 16, 0, 0,
                arrowX, arrowY, progress, 16);
        }

        int fuelX = this.leftPos + this.menu.getSlot(2).x;
        int fuelY = this.topPos + 55;
        graphics.fill(fuelX, fuelY, fuelX + 16, fuelY + 3, 0xFF656565);
        int fuel = Mth.ceil(this.menu.getBurnProgress() * 16);
        if (fuel > 0) {
            graphics.fill(fuelX, fuelY, fuelX + fuel, fuelY + 3, 0xFFE6882F);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        drawLabel(graphics, CHARGE, 32, 23, LABEL_COLOR);
        drawLabel(graphics, COKE, 73, 23, LABEL_COLOR);
        drawLabel(graphics, SLAG, 124, 23, LABEL_COLOR);
        drawLabel(graphics, MOLTEN, 155, 23, LABEL_COLOR);
        boolean connected = this.menu.hasConnectedBlower();
        drawLabel(graphics, connected ? BLOWER_CONNECTED : BLOWER_MISSING,
            this.imageWidth / 2, 64, connected ? 0xFF265F36 : 0xFF873C25);
    }

    private void drawLabel(GuiGraphicsExtractor graphics, Component text, int centerX, int y, int color) {
        graphics.text(this.font, text, centerX - this.font.width(text) / 2, y, color, false);
    }
}
