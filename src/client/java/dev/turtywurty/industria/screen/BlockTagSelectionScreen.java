package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.VariedBlockList;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScene;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldSceneBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

public class BlockTagSelectionScreen extends Screen {
    public static final Component TITLE = Component.translatable("screen." + Industria.MOD_ID + ".block_tag_selection.title");

    private static final int TOP_MARGIN = 24;
    private static final int SIDE_MARGIN = 16;
    private static final int BOTTOM_MARGIN = 16;
    private static final int SLOT_SIZE = 28;
    private static final int SLOT_GAP = 4;
    private static final int SLOT_INSET = 2;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_PADDING = 4;
    private static final int MIN_THUMB_HEIGHT = 12;
    private static final int MAX_SCENE_CACHE = 200;
    private static final int SEARCH_HEIGHT = 20;
    private static final int SEARCH_PADDING = 6;
    private static final int CONFIRM_HEIGHT = 20;
    private static final int CONFIRM_PADDING = 6;
    private static final float SCENE_SCALE_MULTIPLIER = 2.5F;
    private static final Vec3 CAMERA_POS = new Vec3(-3.0, 2.5, 3.0);
    private static final float CAMERA_YAW = 225.0F;
    private static final float CAMERA_PITCH = 25.0F;

    private final Consumer<TagKey<Block>> onSelect;
    private final Screen parent;
    private final Runnable onClose;
    private final Map<TagKey<Block>, FakeWorldScene> sceneCache = new LinkedHashMap<>();
    private boolean closed;

    private List<TagKey<Block>> allTags = List.of();
    private List<TagKey<Block>> tags = List.of();
    private TagKey<Block> selectedTag;
    private EditBox searchField;
    private Button confirmButton;
    private String searchQuery = "";
    private int scrollRow;
    private boolean scrolling;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int gridX;
    private int gridY;
    private int gridWidth;
    private int gridHeight;
    private int gridContentX;
    private int gridContentY;
    private int gridContentWidth;
    private int gridContentHeight;
    private int scrollbarX;
    private int columns;
    private int visibleRows;
    private int totalRows;
    private int maxScrollRows;

    public BlockTagSelectionScreen(Consumer<TagKey<Block>> onSelect) {
        this(null, onSelect, () -> {
        });
    }

    public BlockTagSelectionScreen(Screen parent, Consumer<TagKey<Block>> onSelect) {
        this(parent, onSelect, () -> {
        });
    }

    public BlockTagSelectionScreen(Screen parent, Consumer<TagKey<Block>> onSelect, Runnable onClose) {
        super(TITLE);
        this.onSelect = onSelect;
        this.parent = parent;
        this.onClose = onClose;
    }

    @Override
    protected void init() {
        if (this.searchField != null) {
            this.searchQuery = this.searchField.getValue();
        }

        if (this.allTags.isEmpty()) {
            this.allTags = BuiltInRegistries.BLOCK.getTags()
                    .map(HolderSet.Named::key)
                    .sorted(Comparator.comparing(tag -> tag.location().toString()))
                    .toList();
        }

        applyFilter(this.searchQuery);

        this.searchField = addRenderableWidget(new EditBox(
                this.font,
                this.gridX + SEARCH_PADDING,
                this.panelY + SEARCH_PADDING,
                Math.max(0, this.gridWidth - SEARCH_PADDING * 2),
                SEARCH_HEIGHT,
                Component.empty()
        ));
        this.searchField.setMaxLength(128);
        this.searchField.setHint(MultiblockDesignerScreen.SEARCH_PLACEHOLDER);
        this.searchField.setResponder(this::applyFilter);
        this.searchField.setValue(this.searchQuery);

        this.confirmButton = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                    if (this.selectedTag != null) {
                        this.onSelect.accept(this.selectedTag);
                        if (this.minecraft != null && this.minecraft.screen == this) {
                            onClose();
                        }
                    }
                })
                .bounds(0, 0, 0, 0)
                .build());
        this.confirmButton.active = this.selectedTag != null;

        updateLayout();
    }

    @Override
    public void removed() {
        handleCloseCallbacks();
        super.removed();
    }

    @Override
    public void onClose() {
        handleCloseCallbacks();
        if (this.minecraft != null && this.parent != null) {
            this.minecraft.setScreen(this.parent);
            return;
        }

        super.onClose();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tags.isEmpty())
            return;

        int startIndex = getStartIndex();
        int endIndex = Math.min(this.tags.size(), startIndex + this.visibleRows * this.columns);
        for (int index = startIndex; index < endIndex; index++) {
            FakeWorldScene scene = getScene(this.tags.get(index));
            scene.tick();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isMouseOverPanel(mouseX, mouseY) || !hasScrollbar())
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        int delta = (int) Math.signum(verticalAmount);
        this.scrollRow = Mth.clamp(this.scrollRow - delta, 0, this.maxScrollRows);
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
            if (isMouseOverScrollbar(click.x(), click.y())) {
                this.scrolling = true;
                updateScrollFromMouse(click.y());
                return true;
            }

            int index = getEntryIndexAt(click.x(), click.y());
            if (index != -1) {
                this.selectedTag = this.tags.get(index);
                if (this.confirmButton != null) {
                    this.confirmButton.active = this.selectedTag != null;
                }

                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1 && this.scrolling) {
            this.scrolling = false;
            return true;
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (this.scrolling && click.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
            double mouseY = click.y() + offsetY;
            updateScrollFromMouse(mouseY);
            return true;
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int titleWidth = this.font.width(this.title);
        context.drawString(this.font, this.title, (this.width - titleWidth) / 2, 6, 0xFFEEEEEE, false);
        Component countLabel = Component.translatable("screen." + Industria.MOD_ID + ".block_tag_selection.count", this.tags.size());
        int countWidth = this.font.width(countLabel);
        context.drawString(this.font, countLabel, (this.width - countWidth) / 2, 6 + this.font.lineHeight + 2, 0xFFB0B0B0, false);

        drawPanel(context);

        int slotStride = getSlotStride();
        int startIndex = getStartIndex();
        Component hoveredTooltip = null;

        for (int row = 0; row < this.visibleRows; row++) {
            for (int col = 0; col < this.columns; col++) {
                int index = startIndex + row * this.columns + col;
                if (index >= this.tags.size())
                    break;

                int slotX = this.gridContentX + col * slotStride;
                int slotY = this.gridContentY + row * slotStride;
                if (slotX + SLOT_SIZE > this.gridContentX + this.gridContentWidth ||
                        slotY + SLOT_SIZE > this.gridContentY + this.gridContentHeight)
                    continue;

                TagKey<Block> tag = this.tags.get(index);
                boolean hovered = mouseX >= slotX && mouseY >= slotY
                        && mouseX < slotX + SLOT_SIZE && mouseY < slotY + SLOT_SIZE;
                drawSlot(context, slotX, slotY, hovered, tag.equals(this.selectedTag));

                int renderSize = SLOT_SIZE - SLOT_INSET * 2;
                FakeWorldScene scene = getScene(tag);
                scene.setAnchor(BlockPos.ZERO, renderSize / 2, renderSize / 2);
                scene.render(context, slotX + SLOT_INSET, slotY + SLOT_INSET, renderSize, renderSize, delta);

                if (hovered) {
                    hoveredTooltip = buildTooltipForTag(tag);
                }
            }
        }

        drawScrollbar(context);
        super.render(context, mouseX, mouseY, delta);

        if (hoveredTooltip != null) {
            drawTooltipWrapped(context, hoveredTooltip, mouseX, mouseY);
        }
    }

    private void drawPanel(GuiGraphics context) {
        context.fill(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight, 0xAA101010);
        context.renderOutline(this.panelX, this.panelY, this.panelWidth, this.panelHeight, 0xFF404040);
    }

    private void drawSlot(GuiGraphics context, int x, int y, boolean hovered, boolean selected) {
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF2B2B2B);
        context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF606060);
        if (hovered) {
            context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0x40FFFFFF);
        }

        if (selected) {
            context.renderOutline(x, y, SLOT_SIZE, SLOT_SIZE, 0xFF2FA9FF);
        }
    }

    private void drawScrollbar(GuiGraphics context) {
        if (!hasScrollbar())
            return;

        int trackTop = this.panelY;
        int trackHeight = this.panelHeight;
        context.fill(this.scrollbarX, trackTop, this.scrollbarX + SCROLLBAR_WIDTH, trackTop + trackHeight, 0xFF202020);

        int thumbHeight = getThumbHeight(trackHeight);
        int maxOffset = trackHeight - thumbHeight;
        int thumbY = trackTop + (this.maxScrollRows == 0 ? 0 : Math.round((float) this.scrollRow / this.maxScrollRows * maxOffset));
        context.fill(this.scrollbarX + 1, thumbY, this.scrollbarX + SCROLLBAR_WIDTH - 1, thumbY + thumbHeight, 0xFFB0B0B0);
    }

    private int getThumbHeight(int trackHeight) {
        if (this.totalRows <= 0)
            return trackHeight;

        int thumbHeight = Math.round((float) trackHeight * this.visibleRows / this.totalRows);
        return Mth.clamp(thumbHeight, MIN_THUMB_HEIGHT, trackHeight);
    }

    private void updateScrollFromMouse(double mouseY) {
        int trackTop = this.panelY;
        int trackHeight = this.panelHeight;
        int thumbHeight = getThumbHeight(trackHeight);
        int maxOffset = trackHeight - thumbHeight;
        if (maxOffset <= 0) {
            this.scrollRow = 0;
            return;
        }

        float ratio = (float) (mouseY - trackTop - thumbHeight / 2.0F) / (float) maxOffset;
        ratio = Mth.clamp(ratio, 0.0F, 1.0F);
        this.scrollRow = Mth.clamp(Math.round(ratio * this.maxScrollRows), 0, this.maxScrollRows);
    }

    private int getEntryIndexAt(double mouseX, double mouseY) {
        if (!isMouseOverGrid(mouseX, mouseY))
            return -1;

        int slotStride = getSlotStride();
        int col = (int) ((mouseX - this.gridContentX) / slotStride);
        int row = (int) ((mouseY - this.gridContentY) / slotStride);
        if (col < 0 || col >= this.columns || row < 0 || row >= this.visibleRows)
            return -1;

        int localX = (int) ((mouseX - this.gridContentX) % slotStride);
        int localY = (int) ((mouseY - this.gridContentY) % slotStride);
        if (localX >= SLOT_SIZE || localY >= SLOT_SIZE)
            return -1;

        int index = (row + this.scrollRow) * this.columns + col;
        if (index < 0 || index >= this.tags.size())
            return -1;

        return index;
    }

    private boolean isMouseOverPanel(double mouseX, double mouseY) {
        return mouseX >= this.panelX && mouseY >= this.panelY
                && mouseX < this.panelX + this.panelWidth && mouseY < this.panelY + this.panelHeight;
    }

    private boolean isMouseOverGrid(double mouseX, double mouseY) {
        return mouseX >= this.gridContentX && mouseY >= this.gridContentY
                && mouseX < this.gridContentX + this.gridContentWidth
                && mouseY < this.gridContentY + this.gridContentHeight;
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        if (!hasScrollbar())
            return false;

        return mouseX >= this.scrollbarX && mouseY >= this.panelY
                && mouseX < this.scrollbarX + SCROLLBAR_WIDTH
                && mouseY < this.panelY + this.panelHeight;
    }

    private int getSlotStride() {
        return SLOT_SIZE + SLOT_GAP;
    }

    private int getStartIndex() {
        return this.scrollRow * this.columns;
    }

    private boolean hasScrollbar() {
        return this.maxScrollRows > 0;
    }

    private void updateLayout() {
        this.panelX = SIDE_MARGIN;
        this.panelY = TOP_MARGIN;
        this.panelWidth = Math.max(0, this.width - SIDE_MARGIN * 2);
        this.panelHeight = Math.max(0, this.height - TOP_MARGIN - BOTTOM_MARGIN);

        int headerHeight = SEARCH_PADDING + SEARCH_HEIGHT + SEARCH_PADDING;
        int footerHeight = CONFIRM_PADDING + CONFIRM_HEIGHT + CONFIRM_PADDING;

        this.gridX = this.panelX;
        this.gridY = this.panelY + headerHeight;
        this.gridWidth = Math.max(0, this.panelWidth - SCROLLBAR_WIDTH - SCROLLBAR_PADDING);
        this.gridHeight = Math.max(0, this.panelHeight - headerHeight - footerHeight);
        this.scrollbarX = this.gridX + this.gridWidth + SCROLLBAR_PADDING;

        int slotStride = getSlotStride();
        this.columns = Math.max(1, (this.gridWidth + SLOT_GAP) / slotStride);
        this.visibleRows = Math.max(1, (this.gridHeight + SLOT_GAP) / slotStride);
        this.totalRows = Mth.ceil((float) this.tags.size() / this.columns);
        this.maxScrollRows = Math.max(0, this.totalRows - this.visibleRows);
        this.scrollRow = Mth.clamp(this.scrollRow, 0, this.maxScrollRows);

        this.gridContentWidth = this.columns * slotStride - SLOT_GAP;
        this.gridContentHeight = this.visibleRows * slotStride - SLOT_GAP;
        this.gridContentX = this.gridX + Math.max(0, (this.gridWidth - this.gridContentWidth) / 2);
        this.gridContentY = this.gridY + Math.max(0, (this.gridHeight - this.gridContentHeight) / 2);

        if (this.searchField != null) {
            this.searchField.setX(this.gridX + SEARCH_PADDING);
            this.searchField.setY(this.panelY + SEARCH_PADDING);
            this.searchField.setWidth(Math.max(0, this.gridWidth - SEARCH_PADDING * 2));
            this.searchField.setHeight(SEARCH_HEIGHT);
        }

        if (this.confirmButton != null) {
            this.confirmButton.setX(this.gridX + SEARCH_PADDING);
            this.confirmButton.setY(this.panelY + this.panelHeight - CONFIRM_PADDING - CONFIRM_HEIGHT);
            this.confirmButton.setWidth(Math.max(0, this.gridWidth - SEARCH_PADDING * 2));
            this.confirmButton.setHeight(CONFIRM_HEIGHT);
        }
    }

    private void applyFilter(String query) {
        this.searchQuery = query;
        String trimmed = query.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) {
            this.tags = this.allTags;
        } else {
            this.tags = this.allTags.stream()
                    .filter(tag -> tag.location().toString().toLowerCase(Locale.ROOT).contains(trimmed))
                    .toList();
        }

        if (this.selectedTag != null && !this.tags.contains(this.selectedTag)) {
            this.selectedTag = null;
        }
        if (this.confirmButton != null) {
            this.confirmButton.active = this.selectedTag != null;
        }

        this.scrollRow = 0;
        updateLayout();
    }

    private FakeWorldScene getScene(TagKey<Block> tagKey) {
        FakeWorldScene scene = this.sceneCache.remove(tagKey);
        if (scene == null) {
            VariedBlockList variedBlockList = VariedBlockList.Builder.create()
                    .addTag(tagKey)
                    .build();
            scene = FakeWorldSceneBuilder.create()
                    .camera(CAMERA_POS, CAMERA_YAW, CAMERA_PITCH)
                    .populate(ctx -> ctx.addVariedBlockList(BlockPos.ZERO, variedBlockList))
                    .build();
            scene.setScaleMultiplier(SCENE_SCALE_MULTIPLIER);
        }

        this.sceneCache.put(tagKey, scene);
        while (this.sceneCache.size() > MAX_SCENE_CACHE) {
            TagKey<Block> oldest = this.sceneCache.keySet().iterator().next();
            FakeWorldScene removed = this.sceneCache.remove(oldest);
            if (removed != null) {
                removed.close();
            }
        }

        return scene;
    }

    private void closeScenes() {
        for (FakeWorldScene scene : this.sceneCache.values()) {
            scene.close();
        }

        this.sceneCache.clear();
    }

    private void handleCloseCallbacks() {
        if (this.closed)
            return;

        this.closed = true;
        this.onClose.run();
        closeScenes();
    }

    private void drawTooltipWrapped(GuiGraphics context, Component text, int mouseX, int mouseY) {
        int maxWidth = Mth.clamp(this.width - 20, 120, 260);
        context.setTooltipForNextFrame(this.font.split(text, maxWidth), mouseX, mouseY);
    }

    private Component buildTooltipForTag(TagKey<Block> tagKey) {
        List<String> names = new ArrayList<>();
        HolderSet.Named<Block> list = BuiltInRegistries.BLOCK.get(tagKey).orElse(null);
        if (list != null) {
            for (Holder<Block> entry : list) {
                Identifier id = entry.unwrapKey().map(ResourceKey::identifier).orElse(BuiltInRegistries.BLOCK.getKey(entry.value()));
                names.add(id.toString());
                if (names.size() >= 50) {
                    names.add("...");
                    break;
                }
            }
        }

        String joined = String.join("\n", names);
        if (joined.isEmpty()) {
            joined = "Empty tag";
        }

        return Component.literal(joined);
    }
}
