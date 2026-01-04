package dev.turtywurty.industria.screen.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemListTooltipComponent implements ClientTooltipComponent {
    private final List<ItemStack> stacks;
    private final int columns;
    private int selectedIndex = 0;

    public ItemListTooltipComponent(List<ItemStack> stacks, int columns) {
        this.stacks = stacks;
        this.columns = columns;
    }

    public ItemListTooltipComponent(List<ItemStack> stacks) {
        this(stacks, 5);
    }

    @Override
    public int getHeight(Font textRenderer) {
        int rows = (this.stacks.size() + this.columns - 1) / this.columns;
        return rows * 20 + 8;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return Math.max(96, (Math.min(this.stacks.size(), this.columns) * 20) + this.columns - 1);
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return true;
    }

    @Override
    public void renderImage(Font textRenderer, int x, int y, int width, int height, GuiGraphics context) {
        if(this.stacks.isEmpty())
            return;

        x = x + 2;
        y = y + 2;

        int itemsWidth = Math.min(this.stacks.size(), this.columns) * 20;
        int itemsHeight = ((this.stacks.size() + this.columns - 1) / this.columns) * 20;
        context.fill(x - 2, y - 2, x + itemsWidth + 2, y + itemsHeight + 2, 0x20FFFFFF);
        context.fill(x, y, x + itemsWidth, y + itemsHeight, 0x80606080);

        for (int index = 0; index < this.stacks.size(); index++) {
            int column = index % this.columns;
            int row = index / this.columns;

            int xPos = x + column * 20 + 1;
            int yPos = y + row * 20 + 1;

            if (index == this.selectedIndex && this.stacks.size() > 1) {
                context.fill(xPos, yPos, xPos + 18, yPos + 18, 0x80FFFFFF);
            }

            ItemStack stack = this.stacks.get(index);
            context.renderItem(stack, xPos + 1, yPos + 1);
            context.renderItemDecorations(textRenderer, stack, xPos + 1, yPos + 1);
        }
    }

    public void onRenderTick(GuiGraphics context, int mouseX, int mouseY) {
        this.selectedIndex = (int) (System.currentTimeMillis() / 1000L % this.stacks.size());
    }
}