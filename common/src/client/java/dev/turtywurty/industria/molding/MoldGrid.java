package dev.turtywurty.industria.molding;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MoldGrid extends AbstractWidget implements AutoCloseable {
    public static final Component TITLE = Component.translatable("gui." + Industria.MOD_ID + ".molding_table.molds");

    private static final Identifier RECIPE_SELECTED_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final Identifier RECIPE_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final Identifier RECIPE_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/recipe");

    private static final Identifier SCROLLER = Identifier.withDefaultNamespace("container/stonecutter/scroller");
    private static final Identifier SCROLLER_DISABLED = Identifier.withDefaultNamespace("container/stonecutter/scroller_disabled");

    private static final int COLUMNS = 3;
    private static final int CELL_SIZE = 18;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    private static final int SCROLLBAR_RIGHT_INSET = 4;

    private final List<MoldPattern> patterns;
    private final Consumer<MoldPattern> onSelected;
    private final List<DynamicTexture> silhouettes = new ArrayList<>();

    private int selectedIndex = -1;
    private int scrollRow = 0;
    private boolean draggingScrollbar;

    public MoldGrid(int x, int y, int width, int height, List<MoldPattern> patterns, Consumer<MoldPattern> onSelected) {
        super(x, y, width, height, TITLE);
        this.patterns = List.copyOf(patterns);
        this.onSelected = onSelected;

        for (MoldPattern pattern : this.patterns) {
            silhouettes.add(new DynamicTexture(
                    () -> "Mold silhouette " + pattern.id(),
                    MoldGeometry.createSilhouette(pattern, 96, 96)));
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int scrollbarX = getScrollbarX();
        int handleOffset = getMaxScrollRow() == 0
                ? 0
                : Math.round((scrollRow / (float) getMaxScrollRow()) * (getScrollbarTrackHeight() - SCROLLBAR_HEIGHT));
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED, getMaxScrollRow() > 0 ? SCROLLER : SCROLLER_DISABLED,
                scrollbarX, getY() + handleOffset,
                SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT
        );

        int hoveredIndex = getPatternAt(mouseX, mouseY);
        for (int cell = 0; cell < (getHeight() / CELL_SIZE) * COLUMNS; cell++) {
            int patternIndex = scrollRow * COLUMNS + cell;
            if (patternIndex >= patterns.size())
                break;

            int x = getX() + (cell % COLUMNS) * CELL_SIZE;
            int y = getY() + (cell / COLUMNS) * CELL_SIZE;
            Identifier texture = patternIndex == selectedIndex
                    ? RECIPE_SELECTED_SPRITE
                    : hoveredIndex == patternIndex
                    ? RECIPE_HIGHLIGHTED_SPRITE
                    : RECIPE_SPRITE;

            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED, texture,
                    x, y,
                    CELL_SIZE, CELL_SIZE
            );

            DynamicTexture silhouette = silhouettes.get(patternIndex);
            graphics.blit(silhouette.getTextureView(), silhouette.getSampler(),
                    x + 1, y + 1, x + 17, y + 17,
                    0.0f, 1.0f, 0.0f, 1.0f);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (!canScroll(x, y, scrollY))
            return false;

        int maxScrollRow = getMaxScrollRow();
        scrollRow = Math.clamp(scrollRow - (int) Math.signum(scrollY), 0, maxScrollRow);
        return true;
    }

    private boolean canScroll(double mouseX, double mouseY, double scrollY) {
        return this.visible && this.active && isMouseOver(mouseX, mouseY) && getMaxScrollRow() > 0 && scrollY != 0;
    }

    private int getMaxScrollRow() {
        int totalRows = (patterns.size() + COLUMNS - 1) / COLUMNS;
        int visibleRows = getHeight() / CELL_SIZE;
        return Math.max(0, totalRows - visibleRows);
    }

    @Override
    public void close() {
        for (DynamicTexture silhouette : silhouettes) {
            silhouette.close();
        }
    }

    private int getPatternAt(double mouseX, double mouseY) {
        double relativeX = mouseX - getX();
        double relativeY = mouseY - getY();
        if (relativeX < 0 || relativeY < 0 || relativeX >= COLUMNS * CELL_SIZE || relativeY >= (getHeight() / CELL_SIZE) * CELL_SIZE)
            return -1;

        int column = (int) (relativeX / CELL_SIZE);
        int row = (int) (relativeY / CELL_SIZE);

        int index = (scrollRow + row) * COLUMNS + column;
        if (index >= 0 && index < patterns.size())
            return index;

        return -1;
    }

    public void selectPattern(MoldPattern pattern) {
        int index = patterns.indexOf(pattern);
        if (index == -1 || index == this.selectedIndex)
            return;

        selectedIndex = index;
        onSelected.accept(pattern);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (event.x() >= getScrollbarX()
                && event.x() < getScrollbarX() + SCROLLBAR_WIDTH
                && getMaxScrollRow() > 0
                && event.y() >= getY()
                && event.y() < getY() + getScrollbarTrackHeight()) {
            draggingScrollbar = true;
            scrollToMouse(event.y());
            return;
        }

        int index = getPatternAt(event.x(), event.y());
        if (index != -1) {
            selectPattern(patterns.get(index));
        }
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (draggingScrollbar) {
            scrollToMouse(event.y());
        }
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        draggingScrollbar = false;
    }

    private void scrollToMouse(double mouseY) {
        if (getMaxScrollRow() == 0 || getScrollbarTrackHeight() <= SCROLLBAR_HEIGHT)
            return;

        double fraction = (mouseY - getY() - SCROLLBAR_HEIGHT * 0.5)
                / (getScrollbarTrackHeight() - SCROLLBAR_HEIGHT);
        fraction = Math.clamp(fraction, 0.0, 1.0);
        scrollRow = (int) Math.round(fraction * getMaxScrollRow());
    }

    private int getScrollbarX() {
        return getX() + getWidth() - SCROLLBAR_RIGHT_INSET - SCROLLBAR_WIDTH;
    }

    private int getScrollbarTrackHeight() {
        return getHeight() - 1;
    }

    public static class Builder {
        private int x, y, width, height;
        private List<MoldPattern> patterns = new ArrayList<>();
        private Consumer<MoldPattern> onSelected = pattern -> {
        };

        public Builder setPosition(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder setPatterns(List<MoldPattern> patterns) {
            this.patterns = List.copyOf(patterns);
            return this;
        }

        public Builder setOnSelected(Consumer<MoldPattern> onSelected) {
            this.onSelected = onSelected;
            return this;
        }

        public MoldGrid build() {
            return new MoldGrid(x, y, width, height, patterns, onSelected);
        }
    }
}
