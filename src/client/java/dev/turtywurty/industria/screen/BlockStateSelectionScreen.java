package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScene;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldSceneBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlockStateSelectionScreen extends Screen {
    public static final Component BLOCK_TITLE = Component.translatable("screen." + Industria.MOD_ID + ".block_state_selection.block.title");
    public static final Component STATE_TITLE = Component.translatable("screen." + Industria.MOD_ID + ".block_state_selection.state.title");

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
    private static final int BUTTON_GAP = 6;
    private static final float SCENE_SCALE_MULTIPLIER = 2.5F;
    private static final Vec3 CAMERA_POS = new Vec3(-3.0, 2.5, 3.0);
    private static final float CAMERA_YAW = 225.0F;
    private static final float CAMERA_PITCH = 25.0F;

    private final Consumer<BlockState> onSelect;
    private final Screen parent;
    private final Runnable onClose;
    private final Map<Block, FakeWorldScene> blockSceneCache = new LinkedHashMap<>();
    private final Map<BlockState, FakeWorldScene> stateSceneCache = new LinkedHashMap<>();
    private boolean closed;

    private Step step = Step.BLOCK;

    private List<Block> allBlocks = List.of();
    private List<Block> blocks = List.of();
    private List<BlockState> allStates = List.of();
    private List<BlockState> states = List.of();

    private Block selectedBlock;
    private BlockState selectedState;

    private EditBox searchField;
    private Button confirmButton;
    private Button backButton;
    private String blockSearchQuery = "";
    private String stateSearchQuery = "";

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

    public BlockStateSelectionScreen(Consumer<BlockState> onSelect) {
        this(null, onSelect, () -> {
        });
    }

    public BlockStateSelectionScreen(Screen parent, Consumer<BlockState> onSelect) {
        this(parent, onSelect, () -> {
        });
    }

    public BlockStateSelectionScreen(Screen parent, Consumer<BlockState> onSelect, Runnable onClose) {
        super(BLOCK_TITLE);
        this.onSelect = onSelect;
        this.parent = parent;
        this.onClose = onClose;
    }

    @Override
    protected void init() {
        if (this.searchField != null) {
            setActiveSearchQuery(this.searchField.getValue());
        }

        if (this.allBlocks.isEmpty()) {
            this.allBlocks = BuiltInRegistries.BLOCK.stream()
                    .filter(block -> !block.defaultBlockState().isAir())
                    .filter(block -> !(block instanceof WallBannerBlock))
                    .sorted(Comparator.comparing(block -> block.getName().getString().toLowerCase(Locale.ROOT)))
                    .toList();
        }

        if (this.step == Step.STATE && this.selectedBlock != null) {
            this.allStates = getAllStatesForBlock(this.selectedBlock);
        }

        applyFilter(getActiveSearchQuery());

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
        this.searchField.setValue(getActiveSearchQuery());

        if (this.step == Step.STATE) {
            this.backButton = addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> switchToBlockStep())
                    .bounds(0, 0, 0, 0)
                    .build());
        }

        this.confirmButton = addRenderableWidget(Button.builder(getConfirmText(), button -> {
                    if (this.step == Step.BLOCK) {
                        if (this.selectedBlock != null) {
                            switchToStateStep(this.selectedBlock);
                        }
                    } else if (this.selectedState != null) {
                        this.onSelect.accept(this.selectedState);
                        if (this.minecraft != null && this.minecraft.screen == this) {
                            onClose();
                        }
                    }
                })
                .bounds(0, 0, 0, 0)
                .build());
        this.confirmButton.active = hasSelection();

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
        if (getActiveCount() == 0)
            return;

        int startIndex = getStartIndex();
        int endIndex = Math.min(getActiveCount(), startIndex + this.visibleRows * this.columns);
        for (int index = startIndex; index < endIndex; index++) {
            if (this.step == Step.BLOCK) {
                Block block = this.blocks.get(index);
                if (!shouldRenderAsItem(block)) {
                    FakeWorldScene scene = getBlockScene(block);
                    scene.tick();
                }
            } else {
                FakeWorldScene scene = getStateScene(this.states.get(index));
                scene.tick();
            }
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
                if (this.step == Step.BLOCK) {
                    this.selectedBlock = this.blocks.get(index);
                    this.selectedState = null;
                } else {
                    this.selectedState = this.states.get(index);
                }

                if (this.confirmButton != null) {
                    this.confirmButton.active = hasSelection();
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
        Component title = this.step == Step.BLOCK ? BLOCK_TITLE : STATE_TITLE;
        int titleWidth = this.font.width(title);
        context.drawString(this.font, title, (this.width - titleWidth) / 2, 6, 0xFFEEEEEE, false);
        Component countLabel = this.step == Step.BLOCK
                ? Component.translatable("screen." + Industria.MOD_ID + ".block_selection.count", this.blocks.size())
                : Component.translatable("screen." + Industria.MOD_ID + ".block_state_selection.count", this.states.size());
        int countWidth = this.font.width(countLabel);
        context.drawString(this.font, countLabel, (this.width - countWidth) / 2, 6 + this.font.lineHeight + 2, 0xFFB0B0B0, false);

        drawPanel(context);

        int slotStride = getSlotStride();
        int startIndex = getStartIndex();
        Component hoveredTooltip = null;

        for (int row = 0; row < this.visibleRows; row++) {
            for (int col = 0; col < this.columns; col++) {
                int index = startIndex + row * this.columns + col;
                if (index >= getActiveCount())
                    break;

                int slotX = this.gridContentX + col * slotStride;
                int slotY = this.gridContentY + row * slotStride;
                if (slotX + SLOT_SIZE > this.gridContentX + this.gridContentWidth ||
                        slotY + SLOT_SIZE > this.gridContentY + this.gridContentHeight)
                    continue;

                boolean hovered = mouseX >= slotX && mouseY >= slotY
                        && mouseX < slotX + SLOT_SIZE && mouseY < slotY + SLOT_SIZE;

                if (this.step == Step.BLOCK) {
                    Block block = this.blocks.get(index);
                    drawSlot(context, slotX, slotY, hovered, block == this.selectedBlock);

                    int renderSize = SLOT_SIZE - SLOT_INSET * 2;
                    if (shouldRenderAsItem(block)) {
                        renderItemPreview(context, block, slotX + SLOT_INSET, slotY + SLOT_INSET, renderSize);
                    } else {
                        FakeWorldScene scene = getBlockScene(block);
                        scene.setAnchor(BlockPos.ZERO, renderSize / 2, renderSize / 2);
                        scene.render(context, slotX + SLOT_INSET, slotY + SLOT_INSET, renderSize, renderSize, delta);
                    }

                    if (hovered) {
                        hoveredTooltip = block.getName();
                    }
                } else {
                    BlockState state = this.states.get(index);
                    drawSlot(context, slotX, slotY, hovered, state == this.selectedState);

                    int renderSize = SLOT_SIZE - SLOT_INSET * 2;
                    FakeWorldScene scene = getStateScene(state);
                    scene.setAnchor(BlockPos.ZERO, renderSize / 2, renderSize / 2);
                    scene.render(context, slotX + SLOT_INSET, slotY + SLOT_INSET, renderSize, renderSize, delta);

                    if (hovered) {
                        String properties = formatProperties(state).getString();
                        if (properties.isEmpty()) {
                            hoveredTooltip = state.getBlock().getName();
                        } else {
                            hoveredTooltip = Component.literal(state.getBlock().getName().getString() + " (" + properties + ")");
                        }
                    }
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
        if (index < 0 || index >= getActiveCount()) {
            return -1;
        }

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

    private int getActiveCount() {
        return this.step == Step.BLOCK ? this.blocks.size() : this.states.size();
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
        this.totalRows = Mth.ceil((float) getActiveCount() / this.columns);
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

        int confirmY = this.panelY + this.panelHeight - CONFIRM_PADDING - CONFIRM_HEIGHT;
        if (this.confirmButton != null) {
            if (this.backButton != null) {
                int halfWidth = Math.max(0, (this.gridWidth - SEARCH_PADDING * 2 - BUTTON_GAP) / 2);
                this.backButton.setX(this.gridX + SEARCH_PADDING);
                this.backButton.setY(confirmY);
                this.backButton.setWidth(halfWidth);
                this.backButton.setHeight(CONFIRM_HEIGHT);

                this.confirmButton.setX(this.gridX + SEARCH_PADDING + halfWidth + BUTTON_GAP);
                this.confirmButton.setY(confirmY);
                this.confirmButton.setWidth(halfWidth);
                this.confirmButton.setHeight(CONFIRM_HEIGHT);
            } else {
                this.confirmButton.setX(this.gridX + SEARCH_PADDING);
                this.confirmButton.setY(confirmY);
                this.confirmButton.setWidth(Math.max(0, this.gridWidth - SEARCH_PADDING * 2));
                this.confirmButton.setHeight(CONFIRM_HEIGHT);
            }
        }
    }

    private void applyFilter(String query) {
        setActiveSearchQuery(query);
        String trimmed = query.trim().toLowerCase(Locale.ROOT);
        if (this.step == Step.BLOCK) {
            if (trimmed.isEmpty()) {
                this.blocks = this.allBlocks;
            } else {
                this.blocks = this.allBlocks.stream()
                        .filter(block -> {
                            String id = BuiltInRegistries.BLOCK.getKey(block).toString();
                            if (id.contains(trimmed))
                                return true;

                            String name = block.getName().getString().toLowerCase(Locale.ROOT);
                            return name.contains(trimmed);
                        })
                        .toList();
            }

            if (this.selectedBlock != null && !this.blocks.contains(this.selectedBlock)) {
                this.selectedBlock = null;
            }
        } else {
            if (trimmed.isEmpty()) {
                this.states = this.allStates;
            } else {
                this.states = this.allStates.stream()
                        .filter(state -> formatProperties(state).getString().toLowerCase(Locale.ROOT).contains(trimmed))
                        .toList();
            }

            if (this.selectedState != null && !this.states.contains(this.selectedState)) {
                this.selectedState = null;
            }
        }

        if (this.confirmButton != null) {
            this.confirmButton.active = hasSelection();
        }

        this.scrollRow = 0;
        updateLayout();
    }

    private boolean shouldRenderAsItem(Block block) {
        return !getItemStack(block).isEmpty();
    }

    private ItemStack getItemStack(Block block) {
        if (block.asItem() == Items.AIR)
            return ItemStack.EMPTY;

        return block.asItem().getDefaultInstance();
    }

    private void renderItemPreview(GuiGraphics context, Block block, int x, int y, int size) {
        ItemStack stack = getItemStack(block);
        if (stack.isEmpty())
            return;

        float scale = size / 16.0F;
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        context.renderFakeItem(stack, 0, 0);
        context.pose().popMatrix();
    }

    private void drawTooltipWrapped(GuiGraphics context, Component text, int mouseX, int mouseY) {
        int maxWidth = Mth.clamp(this.width - 20, 80, 240);
        context.setTooltipForNextFrame(this.font.split(text, maxWidth), mouseX, mouseY);
    }

    private FakeWorldScene getBlockScene(Block block) {
        FakeWorldScene scene = this.blockSceneCache.remove(block);
        if (scene == null) {
            scene = FakeWorldSceneBuilder.create()
                    .camera(CAMERA_POS, CAMERA_YAW, CAMERA_PITCH)
                    .populate(ctx -> {
                        var state = block.defaultBlockState();
                        if (block instanceof LiquidBlock) {
                            ctx.addFluid(BlockPos.ZERO, state.getFluidState());
                        } else {
                            ctx.addBlock(BlockPos.ZERO, state);
                        }
                    })
                    .build();
            scene.setScaleMultiplier(SCENE_SCALE_MULTIPLIER);
        }

        this.blockSceneCache.put(block, scene);
        while (this.blockSceneCache.size() > MAX_SCENE_CACHE) {
            Block oldest = this.blockSceneCache.keySet().iterator().next();
            FakeWorldScene removed = this.blockSceneCache.remove(oldest);
            if (removed != null) {
                removed.close();
            }
        }

        return scene;
    }

    private FakeWorldScene getStateScene(BlockState state) {
        FakeWorldScene scene = this.stateSceneCache.remove(state);
        if (scene == null) {
            scene = FakeWorldSceneBuilder.create()
                    .camera(CAMERA_POS, CAMERA_YAW, CAMERA_PITCH)
                    .populate(ctx -> {
                        if (state.getBlock() instanceof LiquidBlock) {
                            ctx.addFluid(BlockPos.ZERO, state.getFluidState());
                        } else {
                            ctx.addBlock(BlockPos.ZERO, state);
                        }
                    })
                    .build();
            scene.setScaleMultiplier(SCENE_SCALE_MULTIPLIER);
        }

        this.stateSceneCache.put(state, scene);
        while (this.stateSceneCache.size() > MAX_SCENE_CACHE) {
            BlockState oldest = this.stateSceneCache.keySet().iterator().next();
            FakeWorldScene removed = this.stateSceneCache.remove(oldest);
            if (removed != null) {
                removed.close();
            }
        }

        return scene;
    }

    private void closeScenes() {
        for (FakeWorldScene scene : this.blockSceneCache.values()) {
            scene.close();
        }

        for (FakeWorldScene scene : this.stateSceneCache.values()) {
            scene.close();
        }

        this.blockSceneCache.clear();
        this.stateSceneCache.clear();
    }

    private List<BlockState> getAllStatesForBlock(Block block) {
        var manager = block.getStateDefinition();
        List<BlockState> states = manager.getPossibleStates();
        if (states.size() > 1 || manager.getProperties().isEmpty())
            return states;

        BlockState base = block.defaultBlockState();
        List<BlockState> generated = new ArrayList<>();
        generateStatesRecursive(generated, base, new ArrayList<>(manager.getProperties()), 0);
        return generated.isEmpty() ? List.of(base) : generated;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void generateStatesRecursive(List<BlockState> out, BlockState current, List<Property<?>> properties, int index) {
        if (index >= properties.size()) {
            out.add(current);
            return;
        }

        Property<?> property = properties.get(index);
        for (Object rawValue : property.getPossibleValues()) {
            Comparable value = (Comparable) rawValue;
            BlockState next = current.setValue((Property) property, value);
            generateStatesRecursive(out, next, properties, index + 1);
        }
    }

    private void switchToStateStep(Block block) {
        this.step = Step.STATE;
        this.selectedBlock = block;
        this.selectedState = null;
        this.stateSearchQuery = "";
        this.scrollRow = 0;
        this.allStates = getAllStatesForBlock(block);
        closeScenes();
        rebuildWidgets();
    }

    private void switchToBlockStep() {
        this.step = Step.BLOCK;
        this.selectedState = null;
        this.scrollRow = 0;
        closeScenes();
        rebuildWidgets();
    }

    private Component getConfirmText() {
        return this.step == Step.BLOCK ? CommonComponents.GUI_CONTINUE : CommonComponents.GUI_DONE;
    }

    private boolean hasSelection() {
        return this.step == Step.BLOCK ? this.selectedBlock != null : this.selectedState != null;
    }

    private String getActiveSearchQuery() {
        return this.step == Step.BLOCK ? this.blockSearchQuery : this.stateSearchQuery;
    }

    private void setActiveSearchQuery(String query) {
        if (this.step == Step.BLOCK) {
            this.blockSearchQuery = query;
        } else {
            this.stateSearchQuery = query;
        }
    }

    private static Component formatProperties(BlockState state) {
        Map<Property<?>, Comparable<?>> entries = state.getValues();
        if (entries.isEmpty())
            return Component.empty();

        String formatted = entries.entrySet().stream()
                .map(entry -> entry.getKey().getName() + "=" + formatProperty(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
        return Component.literal(formatted);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String formatProperty(Property property, Comparable<?> value) {
        return property.getName(value);
    }

    private void handleCloseCallbacks() {
        if (this.closed)
            return;

        this.closed = true;
        this.onClose.run();
        closeScenes();
    }

    private enum Step {
        BLOCK,
        STATE
    }
}
