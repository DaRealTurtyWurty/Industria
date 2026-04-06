package dev.turtywurty.industria.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class AutoCompleteEditBox<T> extends EditBox {
    private static final int DROPDOWN_BACKGROUND_COLOR = 0xFF111111;
    private static final int DROPDOWN_BORDER_COLOR = 0xFF8B8B8B;
    private static final int DROPDOWN_HIGHLIGHT_COLOR = 0xFF3B5F99;
    private static final int DROPDOWN_TEXT_COLOR = 0xFFE0E0E0;
    private static final int DROPDOWN_SCROLLBAR_TRACK_COLOR = 0xFF2A2A2A;
    private static final int DROPDOWN_SCROLLBAR_HANDLE_COLOR = 0xFF7A7A7A;
    private static final int DROPDOWN_PADDING = 4;
    private static final int DROPDOWN_BORDER_WIDTH = 1;
    private static final int DROPDOWN_SCROLLBAR_WIDTH = 6;
    private static final int MIN_VISIBLE_SUGGESTIONS = 1;

    private final Font font;
    private final AutoCompleteProvider<T> provider;
    private final Function<T, String> suggestionToString;
    private final List<T> suggestions = new ArrayList<>();

    private Consumer<String> responder = _ -> {
    };
    private Consumer<T> suggestionSelectedListener = _ -> {
    };

    private int maxVisibleSuggestions;
    private int highlightedSuggestionIndex = -1;
    private int scrollOffset;
    private boolean scrollingSuggestions;
    private boolean selectingSuggestion;
    private boolean suggestionsOpen;
    private @Nullable T selectedSuggestion;

    public AutoCompleteEditBox(Font font, int x, int y, int width, int height, Component narration,
                               AutoCompleteProvider<T> provider, int maxVisibleSuggestions) {
        this(font, x, y, width, height, null, narration, provider, maxVisibleSuggestions);
    }

    public static <T> Builder<T> builder(Font font, AutoCompleteProvider<T> provider) {
        return new Builder<>(font, provider);
    }

    public AutoCompleteEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox oldBox,
                               Component narration, AutoCompleteProvider<T> provider, int maxVisibleSuggestions) {
        super(font, x, y, width, height, oldBox, narration);
        this.font = font;
        this.provider = provider;
        this.suggestionToString = Objects.requireNonNullElseGet(provider.suggestionToString(), () -> Objects::toString);
        this.maxVisibleSuggestions = Math.max(MIN_VISIBLE_SUGGESTIONS, maxVisibleSuggestions);
        super.setResponder(this::handleValueChanged);
        refreshSuggestions();
    }

    @Override
    public void setResponder(Consumer<String> responder) {
        this.responder = responder;
        super.setResponder(this::handleValueChanged);
    }

    public void setSuggestionSelectedListener(Consumer<T> suggestionSelectedListener) {
        this.suggestionSelectedListener = suggestionSelectedListener;
    }

    public @Nullable T getSelectedSuggestion() {
        return this.selectedSuggestion;
    }

    public int getMaxVisibleSuggestions() {
        return this.maxVisibleSuggestions;
    }

    public void setMaxVisibleSuggestions(int maxVisibleSuggestions) {
        this.maxVisibleSuggestions = Math.max(MIN_VISIBLE_SUGGESTIONS, maxVisibleSuggestions);
        clampScrollOffset();
    }

    public List<T> getSuggestions() {
        return List.copyOf(this.suggestions);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (!this.selectingSuggestion) {
            refreshSuggestions();
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) {
            refreshSuggestions();
        } else {
            closeSuggestions();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (isDropdownVisible()) {
            if (event.key() == GLFW.GLFW_KEY_DOWN) {
                moveHighlightedSuggestion(1);
                return true;
            }

            if (event.key() == GLFW.GLFW_KEY_UP) {
                moveHighlightedSuggestion(-1);
                return true;
            }

            if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER || event.key() == GLFW.GLFW_KEY_TAB) {
                if (isValidSuggestionIndex(this.highlightedSuggestionIndex)) {
                    selectSuggestion(this.highlightedSuggestionIndex);
                    return true;
                }
            }

            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                closeSuggestions();
                return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (isDropdownVisible()) {
            if (isMouseOverScrollbar(event.x(), event.y())) {
                this.scrollingSuggestions = true;
                updateScrollFromMouse(event.y());
                return true;
            }

            int clickedIndex = getSuggestionIndexAt(event.x(), event.y());
            if (clickedIndex >= 0) {
                selectSuggestion(clickedIndex);
                setFocused(true);
                return true;
            }
        }

        boolean clicked = super.mouseClicked(event, doubleClick);
        if (clicked && !this.suggestions.isEmpty()) {
            this.suggestionsOpen = true;
        }

        if (!clicked && !isMouseOverDropdown(event.x(), event.y())) {
            closeSuggestions();
        }

        return clicked;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.scrollingSuggestions = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double offsetX, double offsetY) {
        if (this.scrollingSuggestions && shouldScrollSuggestions()) {
            updateScrollFromMouse(event.y());
            return true;
        }

        return super.mouseDragged(event, offsetX, offsetY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isDropdownVisible() && isMouseOverDropdown(mouseX, mouseY) && shouldScrollSuggestions()) {
            this.scrollOffset = Mth.clamp(this.scrollOffset - (int) Math.signum(verticalAmount), 0, getMaxScrollOffset());
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || isDropdownVisible() && isMouseOverDropdown(mouseX, mouseY);
    }

    public boolean isMouseOverSuggestions(double mouseX, double mouseY) {
        return isDropdownVisible() && isMouseOverDropdown(mouseX, mouseY);
    }

    public void dismissSuggestions() {
        closeSuggestions();
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
        if (isDropdownVisible()) {
            renderDropdown(graphics, mouseX, mouseY);
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        super.updateWidgetNarration(output);
        if (isDropdownVisible() && isValidSuggestionIndex(this.highlightedSuggestionIndex)) {
            output.add(NarratedElementType.HINT, Component.literal("Suggestion: " + toSuggestionString(this.suggestions.get(this.highlightedSuggestionIndex))));
        }
    }

    private void handleValueChanged(String value) {
        if (!this.selectingSuggestion) {
            this.selectedSuggestion = null;
            refreshSuggestions();
        }

        this.responder.accept(value);
    }

    private void refreshSuggestions() {
        this.suggestions.clear();
        var providedSuggestions = this.provider.getSuggestions(getValue());
        if (providedSuggestions != null) {
            this.suggestions.addAll(providedSuggestions);
        }

        this.suggestionsOpen = isFocused() && !this.suggestions.isEmpty();
        this.highlightedSuggestionIndex = this.suggestions.isEmpty() ? -1 : 0;
        this.scrollOffset = 0;
        clampScrollOffset();
    }

    private void selectSuggestion(int index) {
        if (!isValidSuggestionIndex(index))
            return;

        T suggestion = this.suggestions.get(index);
        this.selectedSuggestion = suggestion;
        this.selectingSuggestion = true;
        setValue(toSuggestionString(suggestion));
        this.selectingSuggestion = false;
        closeSuggestions();
        this.suggestionSelectedListener.accept(suggestion);
    }

    private void moveHighlightedSuggestion(int amount) {
        if (this.suggestions.isEmpty()) {
            this.highlightedSuggestionIndex = -1;
            return;
        }

        if (this.highlightedSuggestionIndex < 0) {
            this.highlightedSuggestionIndex = 0;
        } else {
            this.highlightedSuggestionIndex = Mth.clamp(this.highlightedSuggestionIndex + amount, 0, this.suggestions.size() - 1);
        }

        ensureHighlightVisible();
    }

    private void ensureHighlightVisible() {
        if (!isValidSuggestionIndex(this.highlightedSuggestionIndex))
            return;

        if (this.highlightedSuggestionIndex < this.scrollOffset) {
            this.scrollOffset = this.highlightedSuggestionIndex;
        } else {
            int maxVisibleIndex = this.scrollOffset + getVisibleSuggestionCount() - 1;
            if (this.highlightedSuggestionIndex > maxVisibleIndex) {
                this.scrollOffset = this.highlightedSuggestionIndex - getVisibleSuggestionCount() + 1;
            }
        }

        clampScrollOffset();
    }

    private void closeSuggestions() {
        this.suggestionsOpen = false;
        this.scrollingSuggestions = false;
        this.scrollOffset = 0;
    }

    private void clampScrollOffset() {
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0, getMaxScrollOffset());
    }

    private boolean isDropdownVisible() {
        return this.suggestionsOpen && isVisible() && isActive() && isFocused() && !this.suggestions.isEmpty();
    }

    private boolean shouldScrollSuggestions() {
        return this.suggestions.size() > this.maxVisibleSuggestions;
    }

    private int getVisibleSuggestionCount() {
        return Math.min(this.suggestions.size(), this.maxVisibleSuggestions);
    }

    private int getMaxScrollOffset() {
        return Math.max(0, this.suggestions.size() - getVisibleSuggestionCount());
    }

    private int getDropdownX() {
        return getX();
    }

    private int getDropdownY() {
        return getBottom();
    }

    private int getDropdownWidth() {
        return getWidth();
    }

    private int getSuggestionRowHeight() {
        return this.font.lineHeight + DROPDOWN_PADDING;
    }

    private int getDropdownHeight() {
        return DROPDOWN_BORDER_WIDTH * 2 + getVisibleSuggestionCount() * getSuggestionRowHeight();
    }

    private int getDropdownContentX() {
        return getDropdownX() + DROPDOWN_BORDER_WIDTH;
    }

    private int getDropdownContentY() {
        return getDropdownY() + DROPDOWN_BORDER_WIDTH;
    }

    private int getDropdownContentWidth() {
        return getDropdownWidth() - DROPDOWN_BORDER_WIDTH * 2;
    }

    private int getDropdownContentHeight() {
        return getDropdownHeight() - DROPDOWN_BORDER_WIDTH * 2;
    }

    private int getScrollbarX() {
        return getDropdownX() + getDropdownWidth() - DROPDOWN_BORDER_WIDTH - DROPDOWN_SCROLLBAR_WIDTH;
    }

    private int getScrollbarHeight() {
        return getDropdownContentHeight();
    }

    private int getScrollbarHandleHeight() {
        if (!shouldScrollSuggestions())
            return getScrollbarHeight();

        int contentHeight = getScrollbarHeight();
        return Math.max(12, contentHeight * getVisibleSuggestionCount() / this.suggestions.size());
    }

    private int getScrollbarHandleY() {
        if (!shouldScrollSuggestions())
            return getDropdownContentY();

        int trackHeight = getScrollbarHeight() - getScrollbarHandleHeight();
        if (trackHeight <= 0)
            return getDropdownContentY();

        return getDropdownContentY() + Math.round(trackHeight * (this.scrollOffset / (float) getMaxScrollOffset()));
    }

    private boolean isMouseOverDropdown(double mouseX, double mouseY) {
        return mouseX >= getDropdownX() && mouseX < getDropdownX() + getDropdownWidth()
                && mouseY >= getDropdownY() && mouseY < getDropdownY() + getDropdownHeight();
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        return shouldScrollSuggestions()
                && mouseX >= getScrollbarX() && mouseX < getScrollbarX() + DROPDOWN_SCROLLBAR_WIDTH
                && mouseY >= getDropdownContentY() && mouseY < getDropdownContentY() + getScrollbarHeight();
    }

    private int getSuggestionIndexAt(double mouseX, double mouseY) {
        if (!isMouseOverDropdown(mouseX, mouseY))
            return -1;

        int contentWidth = getDropdownContentWidth() - (shouldScrollSuggestions() ? DROPDOWN_SCROLLBAR_WIDTH : 0);
        if (mouseX < getDropdownContentX() || mouseX >= getDropdownContentX() + contentWidth)
            return -1;

        int relativeY = Mth.floor(mouseY) - getDropdownContentY();
        int row = relativeY / getSuggestionRowHeight();
        int index = this.scrollOffset + row;
        return row >= 0 && row < getVisibleSuggestionCount() && isValidSuggestionIndex(index) ? index : -1;
    }

    private boolean isValidSuggestionIndex(int index) {
        return index >= 0 && index < this.suggestions.size();
    }

    private void updateScrollFromMouse(double mouseY) {
        int trackY = getDropdownContentY();
        int trackHeight = getScrollbarHeight() - getScrollbarHandleHeight();
        if (trackHeight <= 0) {
            this.scrollOffset = 0;
            return;
        }

        float relative = (float) (mouseY - trackY - getScrollbarHandleHeight() / 2.0D) / trackHeight;
        float scrollAmount = Mth.clamp(relative, 0.0F, 1.0F);
        this.scrollOffset = Math.round(scrollAmount * getMaxScrollOffset());
    }

    private void renderDropdown(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int dropdownX = getDropdownX();
        int dropdownY = getDropdownY();
        int dropdownWidth = getDropdownWidth();
        int dropdownHeight = getDropdownHeight();

        graphics.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + dropdownHeight, DROPDOWN_BORDER_COLOR);
        graphics.fill(dropdownX + DROPDOWN_BORDER_WIDTH, dropdownY + DROPDOWN_BORDER_WIDTH,
                dropdownX + dropdownWidth - DROPDOWN_BORDER_WIDTH, dropdownY + dropdownHeight - DROPDOWN_BORDER_WIDTH,
                DROPDOWN_BACKGROUND_COLOR);

        int contentX = getDropdownContentX();
        int contentY = getDropdownContentY();
        int contentWidth = getDropdownContentWidth() - (shouldScrollSuggestions() ? DROPDOWN_SCROLLBAR_WIDTH : 0);
        int contentHeight = getDropdownContentHeight();

        graphics.enableScissor(contentX, contentY, contentX + contentWidth, contentY + contentHeight);
        for (int visibleIndex = 0; visibleIndex < getVisibleSuggestionCount(); visibleIndex++) {
            int suggestionIndex = this.scrollOffset + visibleIndex;
            if (!isValidSuggestionIndex(suggestionIndex))
                break;

            int entryY = contentY + visibleIndex * getSuggestionRowHeight();
            boolean highlighted = suggestionIndex == this.highlightedSuggestionIndex;
            boolean hovered = suggestionIndex == getSuggestionIndexAt(mouseX, mouseY);
            if (highlighted || hovered) {
                graphics.fill(contentX, entryY, contentX + contentWidth, entryY + getSuggestionRowHeight(), DROPDOWN_HIGHLIGHT_COLOR);
            }

            String text = this.font.plainSubstrByWidth(toSuggestionString(this.suggestions.get(suggestionIndex)), contentWidth - DROPDOWN_PADDING * 2);
            graphics.text(this.font, text, contentX + DROPDOWN_PADDING, entryY + 2, DROPDOWN_TEXT_COLOR, false);
        }
        graphics.disableScissor();

        if (shouldScrollSuggestions()) {
            int scrollbarX = getScrollbarX();
            int scrollbarY = getDropdownContentY();
            int scrollbarHeight = getScrollbarHeight();
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + DROPDOWN_SCROLLBAR_WIDTH, scrollbarY + scrollbarHeight, DROPDOWN_SCROLLBAR_TRACK_COLOR);

            int handleY = getScrollbarHandleY();
            int handleHeight = getScrollbarHandleHeight();
            graphics.fill(scrollbarX, handleY, scrollbarX + DROPDOWN_SCROLLBAR_WIDTH, handleY + handleHeight, DROPDOWN_SCROLLBAR_HANDLE_COLOR);
        }
    }

    private String toSuggestionString(T suggestion) {
        return this.suggestionToString.apply(suggestion);
    }

    public static class Builder<T> {
        private static final int DEFAULT_MAX_VISIBLE_SUGGESTIONS = 5;

        private final Font font;
        private final AutoCompleteProvider<T> provider;

        private int x;
        private int y;
        private int width;
        private int height;
        private @Nullable EditBox oldBox;
        private Component narration = Component.empty();
        private int maxVisibleSuggestions = DEFAULT_MAX_VISIBLE_SUGGESTIONS;
        private @Nullable String value;
        private Consumer<String> responder = _ -> {
        };
        private Consumer<T> suggestionSelectedListener = _ -> {
        };
        private int maxLength = 32;
        private boolean bordered = true;
        private boolean editable = true;
        private boolean canLoseFocus = true;
        private boolean visible = true;
        private boolean textShadow = true;
        private boolean focused;

        public Builder(Font font, AutoCompleteProvider<T> provider) {
            this.font = font;
            this.provider = provider;
        }

        public Builder<T> x(int x) {
            this.x = x;
            return this;
        }

        public Builder<T> y(int y) {
            this.y = y;
            return this;
        }

        public Builder<T> position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder<T> width(int width) {
            this.width = width;
            return this;
        }

        public Builder<T> height(int height) {
            this.height = height;
            return this;
        }

        public Builder<T> size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder<T> bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder<T> oldBox(@Nullable EditBox oldBox) {
            this.oldBox = oldBox;
            return this;
        }

        public Builder<T> narration(Component narration) {
            this.narration = narration;
            return this;
        }

        public Builder<T> maxVisibleSuggestions(int maxVisibleSuggestions) {
            this.maxVisibleSuggestions = maxVisibleSuggestions;
            return this;
        }

        public Builder<T> value(String value) {
            this.value = value;
            return this;
        }

        public Builder<T> responder(Consumer<String> responder) {
            this.responder = responder;
            return this;
        }

        public Builder<T> onSuggestionSelected(Consumer<T> suggestionSelectedListener) {
            this.suggestionSelectedListener = suggestionSelectedListener;
            return this;
        }

        public Builder<T> maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder<T> bordered(boolean bordered) {
            this.bordered = bordered;
            return this;
        }

        public Builder<T> editable(boolean editable) {
            this.editable = editable;
            return this;
        }

        public Builder<T> canLoseFocus(boolean canLoseFocus) {
            this.canLoseFocus = canLoseFocus;
            return this;
        }

        public Builder<T> visible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public Builder<T> textShadow(boolean textShadow) {
            this.textShadow = textShadow;
            return this;
        }

        public Builder<T> focused(boolean focused) {
            this.focused = focused;
            return this;
        }

        public AutoCompleteEditBox<T> build() {
            AutoCompleteEditBox<T> widget = new AutoCompleteEditBox<>(
                    this.font, this.x, this.y, this.width, this.height, this.oldBox, this.narration, this.provider, this.maxVisibleSuggestions);
            widget.setMaxLength(this.maxLength);
            widget.setBordered(this.bordered);
            widget.setEditable(this.editable);
            widget.setCanLoseFocus(this.canLoseFocus);
            widget.setVisible(this.visible);
            widget.setTextShadow(this.textShadow);
            widget.setResponder(this.responder);
            widget.setSuggestionSelectedListener(this.suggestionSelectedListener);
            if (this.value != null) {
                widget.setValue(this.value);
            }

            if (this.focused) {
                widget.setFocused(true);
            }

            return widget;
        }
    }
}
