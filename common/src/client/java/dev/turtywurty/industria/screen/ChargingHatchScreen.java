package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.menu.ChargingHatchMenu;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ChargingHatchScreen extends AbstractContainerScreen<ChargingHatchMenu> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/dispenser.png");
    private static final int SCROLLBAR_X = 120;
    private static final int SCROLLBAR_Y = 16;
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int SCROLLBAR_HEIGHT = 54;
    private static final int SCROLLBAR_HANDLE_HEIGHT = 12;
    private static final Identifier SCROLLER = Identifier.withDefaultNamespace("container/stonecutter/scroller");
    private static final Identifier SCROLLER_DISABLED = Identifier.withDefaultNamespace("container/stonecutter/scroller_disabled");
    private static final Identifier SCROLLER_BACKGROUND = Identifier.withDefaultNamespace("widget/scroller_background");

    private boolean draggingScrollbar;

    public ChargingHatchScreen(ChargingHatchMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.draggingScrollbar = false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        this.menu.setScrollRow(this.menu.getScrollRow());

        for (int position = 0; position < 9; position++) {
            int index = this.menu.getScrollRow() * 3 + position;
            if(index < this.menu.getHatchSlotCount() && this.menu.isSlotVisible(index))
                continue;

            int x = this.leftPos + 61 + (position % 3) * 18;
            int y = this.topPos + 16 + (position / 3) * 18;
            context.fill(x, y, x + 18, y + 18, 0xFFC6C6C6);
        }

        drawScrollbar(context);

        for (int index = 0; index < this.menu.getHatchSlotCount(); index++) {
            if (!this.menu.isSlotVisible(index))
                continue;

            Slot slot = this.menu.slots.get(index);
            if(index >= this.menu.getProcessingSlotCount()) {
                context.fill(this.leftPos + slot.x, this.topPos + slot.y, this.leftPos + slot.x + 16, this.topPos + slot.y + 16, 0x88000000);
            }

            if (!slot.hasItem())
                continue;

            float progress = this.menu.getMeltingProgress(index);
            if (progress > 0.0F) {
                drawSlotProgress(context, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, progress);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        double localX = x - this.leftPos;
        double localY = y - this.topPos;

        if (((localX >= 61 && localX < 115
                && localY >= 16 && localY < 70) ||
                (localX >= SCROLLBAR_X && localX < SCROLLBAR_X + SCROLLBAR_WIDTH
                        && localY >= SCROLLBAR_Y && localY < SCROLLBAR_Y + SCROLLBAR_HEIGHT))
                && scrollY != 0
                && this.menu.getMaxScrollRow() > 0
                && this.menu.getCarried().isEmpty()) {
            int direction = scrollY > 0 ? -1 : 1;
            this.menu.setScrollRow(this.menu.getScrollRow() + direction);
            return true;
        }

        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = this.leftPos + SCROLLBAR_X;
        int y = this.topPos + SCROLLBAR_Y;

        if (event.button() == 0
                && event.x() >= x && event.x() < x + SCROLLBAR_WIDTH
                && event.y() >= y && event.y() < y + SCROLLBAR_HEIGHT
                && this.menu.getMaxScrollRow() > 0
                && this.menu.getCarried().isEmpty()) {
            this.draggingScrollbar = true;
            scrollToMouse(event.y());
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.draggingScrollbar && event.button() == 0) {
            scrollToMouse(event.y());
            return true;
        }

        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && this.draggingScrollbar) {
            this.draggingScrollbar = false;
            return true;
        }

        return super.mouseReleased(event);
    }

    private void drawSlotProgress(GuiGraphicsExtractor graphics, int x, int y, float progress) {
        float fraction = Math.clamp(progress, 0.0F, 1.0F);
        int remaining = (int) (fraction * 72);
        int color = ARGB.srgbLerp(fraction, 0xFFFF9800, 0xFFFF3030);
        for (int edgeIndex = 0; edgeIndex < 4; edgeIndex++) {
            int length = Math.min(remaining, 18);
            remaining -= length;
            if (length > 0) {
                switch (edgeIndex) {
                    case 0 -> graphics.fill(x, y, x + length, y + 1, color);
                    case 1 -> graphics.fill(x + 17, y, x + 18, y + length, color);
                    case 2 -> graphics.fill(x + 18 - length, y + 17, x + 18, y + 18, color);
                    case 3 -> graphics.fill(x, y + 18 - length, x + 1, y + 18, color);
                }
            }
        }
    }

    private void drawScrollbar(GuiGraphicsExtractor graphics) {
        int x = this.leftPos + SCROLLBAR_X;
        int y = this.topPos + SCROLLBAR_Y;
        int maxScroll = this.menu.getMaxScrollRow();

        int travel = SCROLLBAR_HEIGHT - SCROLLBAR_HANDLE_HEIGHT;
        float fraction = maxScroll > 0
                ? this.menu.getScrollRow() / (float) maxScroll
                : 0;
        int handleY = y + Math.round(fraction * travel);

        ScreenUtils.drawGuiTexture(graphics, SCROLLER_BACKGROUND, x, y, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);
        ScreenUtils.drawGuiTexture(graphics, maxScroll > 0 ? SCROLLER : SCROLLER_DISABLED, x, handleY, SCROLLBAR_WIDTH, SCROLLBAR_HANDLE_HEIGHT);
    }

    private void scrollToMouse(double mouseY) {
        int trackTop = this.topPos + SCROLLBAR_Y;
        int travel = SCROLLBAR_HEIGHT - SCROLLBAR_HANDLE_HEIGHT;

        double fraction = Math.clamp(
                (mouseY - trackTop - SCROLLBAR_HANDLE_HEIGHT / 2.0) / travel,
                0.0, 1.0);

        this.menu.setScrollRow((int) Math.round(fraction * this.menu.getMaxScrollRow()));
    }
}
