package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.multiblock.PieceData;
import dev.turtywurty.industria.multiblock.VariedBlockList;
import dev.turtywurty.industria.network.DeletePaletteEntryPayload;
import dev.turtywurty.industria.network.SetMultiblockPieceCharPayload;
import dev.turtywurty.industria.network.UpdatePaletteEntryNamePayload;
import dev.turtywurty.industria.network.UpdatePaletteEntryVariedBlockListPayload;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScene;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldSceneBuilder;
import dev.turtywurty.industria.screen.widget.*;
import dev.turtywurty.industria.screenhandler.MultiblockDesignerScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.*;

// TODO: Do not sync the scene every tick, only when there are changes.
public class MultiblockDesignerScreen extends AbstractContainerScreen<MultiblockDesignerScreenHandler> {
    private static final Identifier GUI_BORDER_TEXTURE = Industria.id("textures/gui/gui_border.png");
    private static final Identifier DIRT_TEXTURE = Identifier.withDefaultNamespace("textures/block/dirt.png");
    private static final Identifier ADD_BLOCK_ICON_TEXTURE = Industria.id("textures/gui/icons/add_block.png");
    private static final Identifier ADD_BLOCK_STATE_ICON_TEXTURE = Industria.id("textures/gui/icons/add_blockstate.png");
    private static final Identifier ADD_BLOCK_TAG_ICON_TEXTURE = Industria.id("textures/gui/icons/add_block_tag.png");
    private static final Identifier CLOSE_ICON_TEXTURE = Industria.id("textures/gui/icons/close.png");
    private static final Identifier TRASH_ICON_TEXTURE = Industria.id("textures/gui/icons/trash.png");
    private static final Identifier CHECKMARK_TEXTURE = Identifier.withDefaultNamespace("textures/gui/sprites/icon/checkmark.png");

    public static final Component SEARCH_PLACEHOLDER = Component.translatable("screen." + Industria.MOD_ID + ".search.placeholder");
    public static final Component PALETTE_TITLE = Industria.containerTitle("multiblock_designer.palette");
    public static final Component EXPORT_TITLE = Industria.containerTitle("multiblock_designer.export");
    public static final Component EDIT_TITLE = Industria.containerTitle("multiblock_designer.edit");
    public static final Component PALETTE_NAME_NARRATION = Industria.containerTitle("multiblock_designer.edit.palette_name.narration");
    public static final Component DELETE_ENTRY = Industria.containerTitle("multiblock_designer.edit.delete_entry.button");
    public static final Component PALETTE_NAME_LABEL = Industria.containerTitle("multiblock_designer.edit.palette_name.label");
    public static final Component EXPORT_BUTTON_TEXT = Industria.containerTitle("multiblock_designer.export.button");
    public static final Component EXPORT_COPIED_MESSAGE = Industria.containerTitle("multiblock_designer.export.copied_message");
    public static final Component PORT_RULES_BUTTON_TEXT = Industria.containerTitle("multiblock_designer.port_rules.button");

    private static final int BORDER_SLICE = 16;

    private MultiblockDesignerWorldWidget fakeWorldWidget;
    private FakeWorldScene scene;
    private Map<BlockPos, VariedBlockList> cachedVariedBlockLists = Map.of();
    private Map<BlockPos, FakeWorldScene.Nameplate> variedBlockListNameplates = Map.of();
    private Set<BlockPos> cachedResolvedPositions = Set.of();
    private BlockPos selectedPiece;
    private int titleWidth;
    private PaletteEntryListWidget paletteEntryListWidget;
    private BlockEntryListWidget blockEntryListWidget;
    private EditBox paletteNameField;
    private Button deleteEntryButton;
    private Button removeBlockEntryButton;
    private PaletteEntryListWidget.Entry selectedPaletteEntry;
    private Character selectedPaletteChar;
    private BlockEntryListWidget.Entry selectedBlockEntry;
    private CharTextFieldWidget selectedPieceCharField;
    private IconButtonWidget selectedPieceConfirmButton;
    private Button editPortRulesButton;
    private boolean updatingNameField;
    private IconButtonWidget addBlockButton;
    private IconButtonWidget addBlockStateButton;
    private IconButtonWidget addBlockTagButton;
    private IconButtonWidget selectedPieceCloseButton;

    private boolean isFakeClose;

    public MultiblockDesignerScreen(MultiblockDesignerScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelX = -10000;
        this.titleLabelX = -10000;
        this.titleWidth = this.font.width(this.title);

        if (this.fakeWorldWidget != null) {
            this.fakeWorldWidget.onClose();
            this.fakeWorldWidget = null;
            this.scene = null;
        }

        this.scene = FakeWorldSceneBuilder.create()
                .camera(new Vec3(2.5, 66.0, 7.0), 200.0F, -18.0F)
                .build();
        this.fakeWorldWidget = addRenderableWidget(new MultiblockDesignerWorldWidget(this, this.leftPos + 7, this.topPos + 16));
        updateFakeWorldWidgetBounds();

        this.selectedPieceCloseButton = addRenderableWidget(new IconButtonWidget.Builder()
                .iconStates(CLOSE_ICON_TEXTURE, 0, 0, 16, 32, 16, 16, 16, 48)
                .drawBackground(false)
                .size(16, 16)
                .onPress(button -> clearSelection())
                .build());
        this.selectedPieceCloseButton.visible = false;
        this.selectedPieceCloseButton.active = false;

        this.selectedPieceCharField = addRenderableWidget(new CharTextFieldWidget(
                this.font,
                0,
                0,
                20,
                16,
                Component.empty()
        ));
        this.selectedPieceCharField.setVisible(false);
        this.selectedPieceCharField.setEditable(true);
        this.selectedPieceCharField.setResponder(this::onSelectedPieceCharChanged);

        this.selectedPieceConfirmButton = addRenderableWidget(new IconButtonWidget.Builder()
                .iconStates(CHECKMARK_TEXTURE, 0, 0, 0, 0, 9, 9, 9, 9)
                .size(16, 16)
                .onPress(button -> confirmSelectedPieceChar())
                .build());
        this.selectedPieceConfirmButton.visible = false;
        this.selectedPieceConfirmButton.active = false;

        this.editPortRulesButton = addRenderableWidget(Button.builder(PORT_RULES_BUTTON_TEXT, button -> {
                    Minecraft minecraftClient = MultiblockDesignerScreen.this.minecraft;
                    if (minecraftClient == null)
                        return;

                    PieceData pieceData = MultiblockDesignerScreen.this.menu.getBlockEntity()
                            .getPieces()
                            .get(MultiblockDesignerScreen.this.selectedPiece.offset(MultiblockDesignerScreen.this.menu.getBlockEntity().getBlockPos()));
                    if (pieceData == null)
                        return;

                    MultiblockDesignerScreen.this.isFakeClose = true;
                    minecraftClient.setScreen(new PortRulesScreen(
                            MultiblockDesignerScreen.this,
                            pieceData,
                            () -> MultiblockDesignerScreen.this.isFakeClose = false
                    ));
                })
                .bounds(0, 0, 0, 20)
                .build());
        this.editPortRulesButton.visible = false;
        this.editPortRulesButton.active = false;

        initPalettePanelWidgets();
        initExportPanelWidgets();
        initEditPanelWidgets();
        restorePaletteSelection();

        this.cachedVariedBlockLists = Map.of();
        this.variedBlockListNameplates = Map.of();
        this.cachedResolvedPositions = Set.of();
        syncPreview();
    }

    private void initPalettePanelWidgets() {
        if (this.paletteEntryListWidget != null) {
            this.paletteEntryListWidget.closeEntries();
            this.paletteEntryListWidget = null;
        }
        this.selectedPaletteEntry = null;

        this.paletteEntryListWidget = addRenderableWidget(new PaletteEntryListWidget(
                this.minecraft,
                16,
                24,
                (this.width - BORDER_SLICE) / 3 - 16,
                ((this.height - 24) / 3) * 2 - 24,
                24
        ));
        this.paletteEntryListWidget.setSelectionListener(this::onPaletteEntrySelected);
        this.menu.getBlockEntity().getPieces().forEach((blockPos, pieceData) ->
                this.paletteEntryListWidget.addOrUpdateEntry(
                        pieceData.paletteChar,
                        pieceData.name,
                        pieceData.variedBlockList
                ));
    }

    private void initExportPanelWidgets() {
        int panelWidth = (this.width - BORDER_SLICE) / 3 - 16;
        int panelStartX = 8;
        int panelStartY = ((this.height - 24) / 3) * 2 + BORDER_SLICE + 8;

        addRenderableWidget(Button.builder(EXPORT_BUTTON_TEXT, button -> {
                    String content = this.menu.getBlockEntity().exportMultiblock();
                    Minecraft client = this.minecraft;
                    if (client == null)
                        return;

                    client.keyboardHandler.setClipboard(content);
                    LocalPlayer player = client.player;
                    if (player == null)
                        return;

                    player.displayClientMessage(EXPORT_COPIED_MESSAGE, false);
                })
                .bounds(panelStartX + 8, panelStartY + 8, panelWidth - 16, 20)
                .build());
    }

    private void initEditPanelWidgets() {
        int panelWidth = (this.width - BORDER_SLICE) / 3 - 8;
        int panelStartX = ((this.width - BORDER_SLICE) / 3) * 2 + BORDER_SLICE;
        int panelHeight = this.height - BORDER_SLICE;
        int contentX = panelStartX + 8;
        int contentWidth = panelWidth - 16;

        this.paletteNameField = addRenderableWidget(new EditBox(
                this.font,
                contentX,
                36,
                contentWidth,
                20,
                PALETTE_NAME_NARRATION
        ));
        this.paletteNameField.setEditable(false);
        this.paletteNameField.setResponder(this::onPaletteNameChanged);

        Minecraft minecraftClient = MultiblockDesignerScreen.this.minecraft;
        if (minecraftClient == null)
            return;

        this.addBlockButton = addRenderableWidget(new IconButtonWidget.Builder()
                .icon(ADD_BLOCK_ICON_TEXTURE, 0, 0, 16, 16, 16, 16)
                .position(contentX, 58)
                .size(20, 20)
                .onPress(button -> {
                    MultiblockDesignerScreen.this.isFakeClose = true;
                    minecraftClient.setScreen(new BlockSelectionScreen(
                            MultiblockDesignerScreen.this,
                            this::addBlockToSelectedVariedList,
                            () -> MultiblockDesignerScreen.this.isFakeClose = false
                    ));
                })
                .build());

        this.addBlockStateButton = addRenderableWidget(new IconButtonWidget.Builder()
                .icon(ADD_BLOCK_STATE_ICON_TEXTURE, 0, 0, 16, 16, 16, 16)
                .position(contentX + 24, 58)
                .size(20, 20)
                .onPress(button -> {
                    MultiblockDesignerScreen.this.isFakeClose = true;
                    minecraftClient.setScreen(new BlockStateSelectionScreen(
                            MultiblockDesignerScreen.this,
                            this::addBlockStateToSelectedVariedList,
                            () -> MultiblockDesignerScreen.this.isFakeClose = false
                    ));
                })
                .build());

        this.addBlockTagButton = addRenderableWidget(new IconButtonWidget.Builder()
                .icon(ADD_BLOCK_TAG_ICON_TEXTURE, 0, 0, 16, 16, 16, 16)
                .position(contentX + 48, 58)
                .size(20, 20)
                .onPress(button -> {
                    MultiblockDesignerScreen.this.isFakeClose = true;
                    minecraftClient.setScreen(new BlockTagSelectionScreen(
                            MultiblockDesignerScreen.this,
                            this::addBlockTagToSelectedVariedList,
                            () -> MultiblockDesignerScreen.this.isFakeClose = false
                    ));
                })
                .build());
        this.addBlockButton.active = false;
        this.addBlockStateButton.active = false;
        this.addBlockTagButton.active = false;

        this.blockEntryListWidget = addRenderableWidget(new BlockEntryListWidget(
                minecraftClient,
                contentX,
                80,
                contentWidth,
                panelHeight - 110,
                24
        ));
        this.blockEntryListWidget.setSelectionListener(this::onBlockEntrySelected);

        this.deleteEntryButton = addRenderableWidget(Button.builder(DELETE_ENTRY, button -> onDeleteSelectedEntry())
                .bounds(contentX, panelHeight - 20, contentWidth, 20)
                .build());
        this.deleteEntryButton.active = false;

        this.removeBlockEntryButton = addRenderableWidget(new IconButtonWidget.Builder()
                .iconStates(TRASH_ICON_TEXTURE, 0, 0, 16, 32, 16, 16, 16, 48)
                .position(contentX + contentWidth - 20, 58)
                .size(20, 20)
                .onPress(button -> onRemoveSelectedBlockEntry())
                .build());
        this.removeBlockEntryButton.active = false;
    }

    @Override
    public void onClose() {
        super.onClose();
        if (this.isFakeClose)
            return;

        if (this.fakeWorldWidget != null) {
            this.fakeWorldWidget.onClose();
        }

        if (this.paletteEntryListWidget != null) {
            this.paletteEntryListWidget.closeEntries();
            this.paletteEntryListWidget = null;
        }

        if (this.blockEntryListWidget != null) {
            this.blockEntryListWidget.closeEntries();
            this.blockEntryListWidget = null;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        syncPreview();
        if (this.fakeWorldWidget != null) {
            this.fakeWorldWidget.tick();
        }

        if (this.paletteEntryListWidget != null) {
            this.paletteEntryListWidget.tickEntries();
        }

        if (this.blockEntryListWidget != null) {
            this.blockEntryListWidget.tickEntries();
        }

        syncSelectedPaletteChar();
        syncPaletteEntries();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.paletteEntryListWidget.isMouseOver(mouseX, mouseY))
            return this.paletteEntryListWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        if (this.blockEntryListWidget.isMouseOver(mouseX, mouseY))
            return this.blockEntryListWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
            if (this.fakeWorldWidget != null && this.scene != null && this.fakeWorldWidget.handleClick(click, doubled))
                return true;

            if (this.paletteEntryListWidget.isMouseOver(click.x(), click.y()))
                return this.paletteEntryListWidget.mouseClicked(click, doubled);
            if (this.blockEntryListWidget.isMouseOver(click.x(), click.y()))
                return this.blockEntryListWidget.mouseClicked(click, doubled);
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
            if (this.scene != null) {
                float sensitivity = 0.35F;
                this.scene.rotateCamera((float) (offsetX * sensitivity), (float) (offsetY * sensitivity));
                return true;
            }

            if (this.paletteEntryListWidget.isMouseOver(click.x(), click.y()))
                return this.paletteEntryListWidget.mouseDragged(click, offsetX, offsetY);
            if (this.blockEntryListWidget.isMouseOver(click.x(), click.y()))
                return this.blockEntryListWidget.mouseDragged(click, offsetX, offsetY);
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        boolean textFocused = (this.paletteNameField != null && this.paletteNameField.isFocused())
                || (this.selectedPieceCharField != null && this.selectedPieceCharField.isFocused());
        if (textFocused && this.minecraft != null && this.minecraft.options.keyInventory.matches(input))
            return true;

        return super.keyPressed(input);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        drawDirtBackground(context);
        drawBorderOverlay(context);
        renderBg(context, deltaTicks, mouseX, mouseY);
    }

    private void drawDirtBackground(GuiGraphics context) {
        for (int x = 0; x < this.width; x += 16) {
            for (int y = 0; y < this.height; y += 16) {
                int drawWidth = Math.min(16, this.width - x);
                int drawHeight = Math.min(16, this.height - y);
                ScreenUtils.drawTexture(context, DIRT_TEXTURE, x, y, 0.0F, 0.0F, drawWidth, drawHeight, 16, 16);
            }
        }

        context.fill(0, 0, this.width, this.height, 0xAA000000);
    }

    private void drawBorderOverlay(GuiGraphics context) {
        ScreenUtils.drawNineSlicedTexture(context, GUI_BORDER_TEXTURE, 0, 0, this.width, this.height, 0, 0, BORDER_SLICE);

        int furthestLeft = this.width / 2 - this.titleWidth / 2 - 5;
        int furthestRight = this.width / 2 + this.titleWidth / 2 + 5;
        ScreenUtils.drawTexture(context, GUI_BORDER_TEXTURE, furthestLeft, 3, 48.0F, 0.0F, 4, 12);
        for (int x = furthestLeft + 4; x < furthestRight - 4; x += 4) {
            int drawWidth = Math.min(4, furthestRight - 4 - x);
            ScreenUtils.drawTexture(context, GUI_BORDER_TEXTURE, x, 3, 52.0F, 0.0F, drawWidth, 12);
        }

        ScreenUtils.drawTexture(context, GUI_BORDER_TEXTURE, furthestRight - 4, 3, 56.0F, 0.0F, 4, 12);
    }

    @Override
    protected void renderBg(GuiGraphics context, float deltaTicks, int mouseX, int mouseY) {
        drawPalettePanel(context);
        drawExportPanel(context);
        if (this.selectedPiece != null) {
            drawSelectedPiecePanel(context);
        }
        drawEditPanel(context);
    }

    private void drawPalettePanel(GuiGraphics context) {
        int width = (this.width - BORDER_SLICE) / 3;
        int height = ((this.height - 24) / 3) * 2;
        ScreenUtils.drawNineSlicedTexture(context, GUI_BORDER_TEXTURE, 8, 8, width, height, 0, 48, BORDER_SLICE);
    }

    private void drawExportPanel(GuiGraphics context) {
        int width = (this.width - BORDER_SLICE) / 3;
        int height = (this.height - 24) / 3;
        int startY = (((this.height - 24) / 3) * 2) + BORDER_SLICE;
        ScreenUtils.drawNineSlicedTexture(context, GUI_BORDER_TEXTURE, 8, startY, width, height, 0, 48, BORDER_SLICE);
    }

    private void drawSelectedPiecePanel(GuiGraphics context) {
        PanelBounds bounds = getSelectedPiecePanelBounds();
        ScreenUtils.drawNineSlicedTexture(
                context,
                GUI_BORDER_TEXTURE,
                bounds.x(),
                bounds.y(),
                bounds.width(),
                bounds.height(),
                0,
                48,
                BORDER_SLICE
        );
    }

    private void drawEditPanel(GuiGraphics context) {
        int width = (this.width - BORDER_SLICE) / 3 - 8;
        int height = this.height - BORDER_SLICE;
        int startX = ((this.width - BORDER_SLICE) / 3) * 2 + BORDER_SLICE;
        ScreenUtils.drawNineSlicedTexture(context, GUI_BORDER_TEXTURE, startX, 8, width, height, 0, 48, BORDER_SLICE);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        updateFakeWorldWidgetBounds();
        updateSelectedPiecePanelWidgets();
        super.render(context, mouseX, mouseY, deltaTicks);
        if (this.fakeWorldWidget != null) {
            context.renderOutline(this.fakeWorldWidget.getX(), this.fakeWorldWidget.getY(),
                    this.fakeWorldWidget.getWidth(), this.fakeWorldWidget.getHeight(), 0xAAFF4040);
        }
        context.drawString(this.font, this.title, this.width / 2 - this.titleWidth / 2, 4, 0xFF404040, false);

        context.drawString(this.font, PALETTE_TITLE, 16, 14, 0xFF404040, false);

        context.drawString(this.font, EXPORT_TITLE, 16, ((this.height - 24) / 3) * 2 + BORDER_SLICE + 6, 0xFF404040, false);

        context.drawString(this.font, EDIT_TITLE, ((this.width - BORDER_SLICE) / 3) * 2 + BORDER_SLICE + 8, 14, 0xFF404040, false);
        context.drawString(this.font, PALETTE_NAME_LABEL, ((this.width - BORDER_SLICE) / 3) * 2 + BORDER_SLICE + 8, 26, 0xFF808080, false);

        if (this.selectedPiece != null) {
            renderSelectedPiecePanel(context);
        }
    }

    public FakeWorldScene getScene() {
        return scene;
    }

    public Map<BlockPos, VariedBlockList> getCachedVariedBlockLists() {
        return cachedVariedBlockLists;
    }

    private void syncPreview() {
        if (this.scene == null)
            return;

        MultiblockDesignerBlockEntity blockEntity = this.menu.getBlockEntity();
        BlockPos origin = blockEntity.getBlockPos();
        Map<BlockPos, PieceData> pieces = blockEntity.getPieces();
        Set<BlockPos> resolved = new HashSet<>();
        Map<BlockPos, VariedBlockList> variedBlockLists = new HashMap<>();
        Map<BlockPos, FakeWorldScene.Nameplate> nameplates = new HashMap<>();
        for (Map.Entry<BlockPos, PieceData> entry : pieces.entrySet()) {
            BlockPos relative = entry.getKey().subtract(origin);
            resolved.add(relative);
            PieceData data = entry.getValue();
            variedBlockLists.put(relative, data.variedBlockList);

            char paletteChar = data.paletteChar;
            String paletteText = paletteChar == ' ' ? "_" : String.valueOf(paletteChar);
            FakeWorldScene.Nameplate existing = this.variedBlockListNameplates.get(relative);
            boolean paletteChanged = existing == null || !existing.text().getString().equals(paletteText);
            if (paletteChanged && existing != null) {
                this.scene.removeNameplate(existing);
            }

            FakeWorldScene.Nameplate nameplate = paletteChanged
                    ? this.scene.addNameplate(Vec3.atCenterOf(relative), Component.literal(paletteText), 0.75F)
                    : existing;
            if (nameplate != null) {
                nameplates.put(relative, nameplate);
            }
        }

        for (BlockPos removed : this.cachedVariedBlockLists.keySet()) {
            if (!variedBlockLists.containsKey(removed)) {
                this.scene.removeVariedBlockList(removed);
                FakeWorldScene.Nameplate removedNameplate = this.variedBlockListNameplates.get(removed);
                if (removedNameplate != null) {
                    this.scene.removeNameplate(removedNameplate);
                }
            }
        }

        for (Map.Entry<BlockPos, VariedBlockList> entry : variedBlockLists.entrySet()) {
            VariedBlockList previous = this.cachedVariedBlockLists.get(entry.getKey());
            if (!Objects.equals(previous, entry.getValue())) {
                this.scene.addVariedBlockList(entry.getKey(), entry.getValue());
            }
        }

        if (this.selectedPiece != null && !variedBlockLists.containsKey(this.selectedPiece)) {
            clearSelection();
        }

        this.cachedVariedBlockLists = variedBlockLists;
        this.variedBlockListNameplates = nameplates;

        boolean resolvedChanged = !resolved.equals(this.cachedResolvedPositions);
        this.cachedResolvedPositions = Set.copyOf(resolved);
        if (resolvedChanged) {
            int targetX = this.fakeWorldWidget.getWidth() / 2;
            int targetY = this.fakeWorldWidget.getHeight() / 2;
            this.scene.updateAnchor(resolved, targetX, targetY);
        }
    }

    public void selectPiece(BlockPos relativePos) {
        this.selectedPiece = relativePos;
        if (this.scene != null) {
            this.scene.highlightBlock(relativePos, 0xFF2ECC71, 0.03F);
        }
        updateFakeWorldWidgetBounds();
        updateSelectedPieceCharField();
        updateSelectedPiecePanelWidgets();
    }

    private void clearSelection() {
        this.selectedPiece = null;
        if (this.scene != null) {
            this.scene.clearHighlights();
        }
        updateFakeWorldWidgetBounds();
        updateSelectedPieceCharField();
        updateSelectedPiecePanelWidgets();
    }

    private void syncSelectedPaletteChar() {
        if (this.selectedPiece == null)
            return;

        BlockPos worldPos = this.selectedPiece.offset(this.menu.getBlockEntity().getBlockPos());
        PieceData data = this.menu.getBlockEntity().getPieces().get(worldPos);
        if (data == null) {
            clearSelection();
        }
    }

    private void syncPaletteEntries() {
        if (this.paletteEntryListWidget == null)
            return;

        MultiblockDesignerBlockEntity blockEntity = this.menu.getBlockEntity();
        Set<Character> seen = new HashSet<>();
        for (PieceData pieceData : blockEntity.getPieces().values()) {
            seen.add(pieceData.paletteChar);
            this.paletteEntryListWidget.addOrUpdateEntry(
                    pieceData.paletteChar,
                    pieceData.name,
                    pieceData.variedBlockList
            );
        }

        this.paletteEntryListWidget.removeEntriesNotIn(seen);
        if (this.selectedPaletteEntry != null && !seen.contains(this.selectedPaletteEntry.getPaletteChar())) {
            onPaletteEntrySelected(null);
        }
    }

    private void onPaletteEntrySelected(PaletteEntryListWidget.Entry entry) {
        if (this.paletteNameField == null || this.deleteEntryButton == null || this.blockEntryListWidget == null)
            return;

        this.selectedPaletteEntry = entry;
        this.selectedPaletteChar = entry == null ? null : entry.getPaletteChar();
        this.selectedBlockEntry = null;
        if (this.removeBlockEntryButton != null) {
            this.removeBlockEntryButton.active = false;
        }

        boolean hasSelection = entry != null;
        this.paletteNameField.setEditable(hasSelection);
        this.updatingNameField = true;
        this.paletteNameField.setValue(hasSelection ? entry.getName() : "");
        this.updatingNameField = false;
        if (!hasSelection) {
            this.paletteNameField.setFocused(false);
        }

        this.deleteEntryButton.active = hasSelection;
        if (this.addBlockButton != null) {
            this.addBlockButton.active = hasSelection;
        }

        if (this.addBlockStateButton != null) {
            this.addBlockStateButton.active = hasSelection;
        }

        if (this.addBlockTagButton != null) {
            this.addBlockTagButton.active = hasSelection;
        }

        this.blockEntryListWidget.clearEntries();
        if (hasSelection) {
            VariedBlockList variedBlockList = this.selectedPaletteEntry.getVariedBlockList();
            for (BlockState state : variedBlockList.stateList()) {
                this.blockEntryListWidget.addBlockState(state);
            }

            for (Block block : variedBlockList.blockList()) {
                this.blockEntryListWidget.addBlock(block);
            }

            for (TagKey<Block> tagKey : variedBlockList.tagList()) {
                this.blockEntryListWidget.addBlockTag(tagKey);
            }
        }
    }

    private void onPaletteNameChanged(String name) {
        if (this.updatingNameField)
            return;

        if (this.selectedPaletteEntry == null)
            return;

        this.selectedPaletteEntry.update(name, this.selectedPaletteEntry.getVariedBlockList());
        this.menu.getBlockEntity().setPaletteName(this.selectedPaletteEntry.getPaletteChar(), name);
        ClientPlayNetworking.send(new UpdatePaletteEntryNamePayload(this.selectedPaletteEntry.getPaletteChar(), name));
    }

    private void onDeleteSelectedEntry() {
        if (this.selectedPaletteEntry == null || this.paletteEntryListWidget == null)
            return;

        char paletteChar = this.selectedPaletteEntry.getPaletteChar();
        this.paletteEntryListWidget.removeEntry(paletteChar);
        this.menu.getBlockEntity().removePiecesWithChar(paletteChar);
        ClientPlayNetworking.send(new DeletePaletteEntryPayload(paletteChar));
        onPaletteEntrySelected(null);
    }

    private void addBlockToSelectedVariedList(Block block) {
        if (this.selectedPaletteEntry == null)
            return;

        if (this.blockEntryListWidget != null) {
            this.blockEntryListWidget.addBlock(block);
        }

        VariedBlockList newList = buildVariedBlockListWith(builder -> builder.addBlock(block));
        applyVariedBlockList(newList);
    }

    private void addBlockStateToSelectedVariedList(BlockState state) {
        if (this.selectedPaletteEntry == null)
            return;

        if (this.blockEntryListWidget != null) {
            this.blockEntryListWidget.addBlockState(state);
        }

        VariedBlockList newList = buildVariedBlockListWith(builder -> builder.addState(state));
        applyVariedBlockList(newList);
    }

    private void addBlockTagToSelectedVariedList(TagKey<Block> tagKey) {
        if (this.selectedPaletteEntry == null)
            return;

        if (this.blockEntryListWidget != null) {
            this.blockEntryListWidget.addBlockTag(tagKey);
        }

        VariedBlockList newList = buildVariedBlockListWith(builder -> builder.addTag(tagKey));
        applyVariedBlockList(newList);
    }

    private VariedBlockList buildVariedBlockListWith(java.util.function.Consumer<VariedBlockList.Builder> adder) {
        VariedBlockList current = this.selectedPaletteEntry.getVariedBlockList();
        VariedBlockList.Builder builder = VariedBlockList.Builder.create();
        current.blockList().forEach(builder::addBlock);
        current.stateList().forEach(builder::addState);
        current.tagList().forEach(builder::addTag);
        adder.accept(builder);
        return builder.build();
    }

    private void applyVariedBlockList(VariedBlockList newList) {
        if (this.selectedPaletteEntry == null)
            return;

        char paletteChar = this.selectedPaletteEntry.getPaletteChar();
        this.selectedPaletteEntry.update(this.selectedPaletteEntry.getName(), newList);
        this.menu.getBlockEntity().setPaletteVariedBlockList(paletteChar, newList);
        if (ClientPlayNetworking.canSend(UpdatePaletteEntryVariedBlockListPayload.ID)) {
            ClientPlayNetworking.send(new UpdatePaletteEntryVariedBlockListPayload(paletteChar, newList));
        } else {
            Industria.LOGGER.warn("Skipped sending palette update payload for char '{}' because channel is unavailable", paletteChar);
        }
    }

    private void onBlockEntrySelected(BlockEntryListWidget.Entry entry) {
        this.selectedBlockEntry = entry;
        if (this.removeBlockEntryButton != null) {
            this.removeBlockEntryButton.active = entry != null;
        }
    }

    private void onRemoveSelectedBlockEntry() {
        if (this.selectedPaletteEntry == null || this.selectedBlockEntry == null)
            return;

        VariedBlockList current = this.selectedPaletteEntry.getVariedBlockList();
        VariedBlockList.Builder builder = VariedBlockList.Builder.create();
        boolean removed = false;

        switch (this.selectedBlockEntry) {
            case BlockEntryListWidget.BlockEntry blockEntry -> {
                for (Block block : current.blockList()) {
                    if (!removed && block.equals(blockEntry.getBlock())) {
                        removed = true;
                        continue;
                    }
                    builder.addBlock(block);
                }
                current.stateList().forEach(builder::addState);
                current.tagList().forEach(builder::addTag);
            }
            case BlockEntryListWidget.BlockStateEntry stateEntry -> {
                for (BlockState state : current.stateList()) {
                    if (!removed && state.equals(stateEntry.getBlockState())) {
                        removed = true;
                        continue;
                    }
                    builder.addState(state);
                }
                current.blockList().forEach(builder::addBlock);
                current.tagList().forEach(builder::addTag);
            }
            case BlockEntryListWidget.BlockTagEntry tagEntry -> {
                for (TagKey<Block> tag : current.tagList()) {
                    if (!removed && tag.equals(tagEntry.getTagKey())) {
                        removed = true;
                        continue;
                    }
                    builder.addTag(tag);
                }
                current.blockList().forEach(builder::addBlock);
                current.stateList().forEach(builder::addState);
            }
            case null, default -> {
                return;
            }
        }

        if (!removed)
            return;

        VariedBlockList newList = builder.build();
        this.blockEntryListWidget.removeEntry(this.selectedBlockEntry);
        onBlockEntrySelected(null);
        applyVariedBlockList(newList);
    }

    private void restorePaletteSelection() {
        if (this.paletteEntryListWidget == null)
            return;

        if (this.selectedPaletteChar == null) {
            onPaletteEntrySelected(null);
            return;
        }

        PaletteEntryListWidget.Entry entry = this.paletteEntryListWidget.getEntry(this.selectedPaletteChar);
        if (entry != null) {
            this.paletteEntryListWidget.setSelected(entry);
        } else {
            onPaletteEntrySelected(null);
        }
    }

    private void renderSelectedPiecePanel(GuiGraphics context) {
        PanelBounds bounds = getSelectedPiecePanelBounds();
        int textX = bounds.x() + 8;
        int textY = bounds.y() + 6;
        int titleColor = 0xFF404040;
        int labelColor = 0xFF808080;
        int lineHeight = this.font.lineHeight + 2;

        context.drawString(this.font, Component.literal("Selected Piece"), textX, textY, titleColor, false);
        textY += lineHeight;

        BlockPos worldPos = this.selectedPiece.offset(this.menu.getBlockEntity().getBlockPos());
        PieceData data = this.menu.getBlockEntity().getPieces().get(worldPos);
        String name = data != null ? data.name : "Unknown";

        context.drawString(this.font, Component.literal("Name: " + name), textX, textY, labelColor, false);
        textY += lineHeight;

        context.drawString(this.font, Component.literal("Palette:"), textX, textY, labelColor, false);
    }

    private PanelBounds getSelectedPiecePanelBounds() {
        int columnWidth = (this.width - BORDER_SLICE) / 3;
        int height = (this.height - 24) / 3;
        int startX = 12 + columnWidth;
        int startY = 18;
        return new PanelBounds(startX, startY, columnWidth, height);
    }

    private record PanelBounds(int x, int y, int width, int height) {
    }

    private void updateFakeWorldWidgetBounds() {
        if (this.fakeWorldWidget == null)
            return;

        int prevX = this.fakeWorldWidget.getX();
        int prevY = this.fakeWorldWidget.getY();
        int prevWidth = this.fakeWorldWidget.getWidth();
        int prevHeight = this.fakeWorldWidget.getHeight();

        int columnWidth = (this.width - BORDER_SLICE) / 3;
        int middleStartX = 8 + columnWidth;
        int top = 8;
        int bottom = this.height - 8;
        if (this.selectedPiece != null) {
            PanelBounds bounds = getSelectedPiecePanelBounds();
            top = bounds.y() + bounds.height() + 4;
        }

        int widgetX = middleStartX + 4;
        int widgetWidth = Math.max(16, columnWidth - 8);
        int widgetHeight = Math.max(0, bottom - top);

        this.fakeWorldWidget.setX(widgetX);
        this.fakeWorldWidget.setY(top);
        this.fakeWorldWidget.setSize(widgetWidth, widgetHeight);

        boolean boundsChanged = prevX != widgetX || prevY != top || prevWidth != widgetWidth || prevHeight != widgetHeight;
        if (boundsChanged && this.scene != null) {
            int targetX = widgetWidth / 2;
            int targetY = widgetHeight / 2;
            this.scene.updateAnchor(this.cachedResolvedPositions, targetX, targetY);
        }
    }

    private void updateSelectedPiecePanelWidgets() {
        if (this.selectedPieceCloseButton == null || this.selectedPieceCharField == null || this.selectedPieceConfirmButton == null
                || this.editPortRulesButton == null)
            return;

        boolean show = this.selectedPiece != null;
        this.selectedPieceCloseButton.visible = show;
        this.selectedPieceCloseButton.active = show;
        this.selectedPieceCharField.setVisible(show);
        this.selectedPieceCharField.setEditable(show);
        this.selectedPieceConfirmButton.visible = show;
        this.editPortRulesButton.visible = show;

        if (!show) {
            this.selectedPieceConfirmButton.active = false;
            this.editPortRulesButton.active = false;
            return;
        }

        PanelBounds bounds = getSelectedPiecePanelBounds();
        int padding = 4;
        int buttonX = bounds.x() + bounds.width() - this.selectedPieceCloseButton.getWidth() - padding;
        int buttonY = bounds.y() + padding;
        this.selectedPieceCloseButton.setX(buttonX);
        this.selectedPieceCloseButton.setY(buttonY);

        int lineHeight = this.font.lineHeight + 2;
        int paletteLineY = bounds.y() + 6 + lineHeight * 2;
        int labelWidth = this.font.width("Palette:");
        int fieldX = bounds.x() + 8 + labelWidth + 4;
        int fieldY = paletteLineY - (this.selectedPieceCharField.getHeight() - this.font.lineHeight) / 2 - 1;
        this.selectedPieceCharField.setX(fieldX);
        this.selectedPieceCharField.setY(fieldY);

        int confirmX = fieldX + this.selectedPieceCharField.getWidth() + 6;
        int confirmY = fieldY + (this.selectedPieceCharField.getHeight() - this.selectedPieceConfirmButton.getHeight()) / 2;
        this.selectedPieceConfirmButton.setX(confirmX);
        this.selectedPieceConfirmButton.setY(confirmY);
        this.selectedPieceConfirmButton.active = shouldEnableConfirmButton();

        int buttonWidth = bounds.width() - 16;
        buttonX = bounds.x() + 8;
        buttonY = bounds.y() + bounds.height() - 28;
        this.editPortRulesButton.setX(buttonX);
        this.editPortRulesButton.setY(buttonY);
        this.editPortRulesButton.setWidth(buttonWidth);
        this.editPortRulesButton.active = true;
    }

    private void onSelectedPieceCharChanged(String newValue) {
        if (this.selectedPieceConfirmButton != null) {
            this.selectedPieceConfirmButton.active = shouldEnableConfirmButton();
        }
    }

    private boolean shouldEnableConfirmButton() {
        if (this.selectedPiece == null || this.selectedPieceCharField == null)
            return false;

        String text = this.selectedPieceCharField.getValue();
        if (text == null || text.isEmpty())
            return false;

        char current = getCurrentPaletteCharForSelection();
        char newChar = text.charAt(0);
        return current != newChar;
    }

    private char getCurrentPaletteCharForSelection() {
        if (this.selectedPiece == null)
            return '\0';

        BlockPos worldPos = this.selectedPiece.offset(this.menu.getBlockEntity().getBlockPos());
        PieceData data = this.menu.getBlockEntity().getPieces().get(worldPos);
        return data != null ? data.paletteChar : '\0';
    }

    private void updateSelectedPieceCharField() {
        if (this.selectedPieceCharField == null)
            return;

        if (this.selectedPiece == null) {
            this.selectedPieceCharField.setValue("");
            this.selectedPieceCharField.setVisible(false);
            this.selectedPieceCharField.setEditable(false);
            this.selectedPieceCharField.setFocused(false);
            onSelectedPieceCharChanged("");
            return;
        }

        char current = getCurrentPaletteCharForSelection();
        this.selectedPieceCharField.setVisible(true);
        this.selectedPieceCharField.setEditable(true);
        this.selectedPieceCharField.setValue(current == '\0' ? "" : String.valueOf(current));
        this.selectedPieceCharField.setFocused(true);
        onSelectedPieceCharChanged(this.selectedPieceCharField.getValue());
    }

    private void confirmSelectedPieceChar() {
        if (this.selectedPiece == null || this.selectedPieceCharField == null)
            return;

        String text = this.selectedPieceCharField.getValue();
        if (text == null || text.isEmpty())
            return;

        char newChar = text.charAt(0);
        BlockPos worldPos = this.selectedPiece.offset(this.menu.getBlockEntity().getBlockPos());
        this.menu.getBlockEntity().setPaletteChar(worldPos, newChar);
        if (ClientPlayNetworking.canSend(SetMultiblockPieceCharPayload.ID)) {
            ClientPlayNetworking.send(new SetMultiblockPieceCharPayload(worldPos, newChar));
        } else {
            Industria.LOGGER.warn("Skipped sending set piece char payload because channel is unavailable");
        }

        this.selectedPaletteChar = newChar;
        clearSelection();
    }
}
