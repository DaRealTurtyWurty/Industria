package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.ContainmentConveyorScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class ContainmentConveyorScreen extends AbstractContainerScreen<ContainmentConveyorScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/containment_conveyor.png");
    public static final String PROGRESS_TOOLTIP_KEY = "container." + Industria.MOD_ID + ".containment_conveyor.progress";
    public static final Component PROGRESS_TOOLTIP_TEXT = Component.translatable(PROGRESS_TOOLTIP_KEY);

    public ContainmentConveyorScreen(ContainmentConveyorScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int progress = Mth.ceil(this.menu.getProgressPercent() * 66.0F);
        int progressTop = this.topPos + 10 + 66 - progress;
        int fillColor = this.menu.isCapturingEntity() ? 0xFF4FA37B : 0xFF5A5A5A;
        context.fill(this.leftPos + 144, progressTop, this.leftPos + 164, this.topPos + 76, fillColor);

        ScreenUtils.drawTextTruncated(context, this.menu.getStatusText(), this.leftPos + 8, this.topPos + 16, 120, 0x404040, false);
        if (this.menu.isCapturingEntity()) {
            ScreenUtils.drawTextTruncated(context, this.menu.getContainingEntityName(), this.leftPos + 8, this.topPos + 28, 120, 0x404040, false);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractTooltip(context, mouseX, mouseY);

        if (isHovering(144, 10, 20, 66, mouseX, mouseY)) {
            int percent = Math.round(this.menu.getProgressPercent() * 100.0F);
            context.setTooltipForNextFrame(this.font, Component.translatable(PROGRESS_TOOLTIP_KEY, percent), mouseX, mouseY);
            if (this.menu.isCapturingEntity()) {
                context.setTooltipForNextFrame(this.font, this.menu.getContainingEntityName(), mouseX, mouseY + 10);
            }
        }
    }
}
