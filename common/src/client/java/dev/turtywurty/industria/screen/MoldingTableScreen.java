package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.menu.MoldingTableMenu;
import dev.turtywurty.industria.molding.*;
import dev.turtywurty.industria.network.CompleteMoldPayload;
import dev.turtywurty.industria.network.UpdateMoldVisualPayload;
import dev.turtywurty.industria.util.ScreenUtils;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MoldingTableScreen extends AbstractContainerScreen<MoldingTableMenu> {
    public static final Component MOLD_TEMPLATES_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".molding_table.templates");
    public static final Component RESET_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".molding_table.reset");
    public static final Component FAILED_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".molding_table.failed");

    private static final Identifier TEXTURE = Industria.id("textures/gui/container/molding_table.png");

    private static final int CAST_X = 44;
    private static final int CAST_Y = 14;
    private static final int CAST_SIZE = 106;
    private static final float GROOVE_RADIUS = 4.0f / CAST_SIZE;
    private static final double MAX_DISTANCE = 3.0;

    private boolean tracing;
    private boolean failedCast;
    private @Nullable MoldTrace moldTrace;
    private int currentEdge;
    private ItemStack lastStack = ItemStack.EMPTY;
    private MoldPattern selectedPattern = MoldPatterns.INGOT;
    private int currentPolygonIndex = 0;
    private @Nullable MoldGrid moldGrid;
    private @Nullable Button resetButton;
    private boolean moldsVisible;
    private final MoldCastSurface castSurface = new MoldCastSurface();
    private @Nullable MoldPoint lastGroovePoint;
    private final List<MoldTableVisualState.Stroke> visualStrokes = new ArrayList<>();
    private boolean visualDirty;
    private int visualSyncTicks;

    public MoldingTableScreen(MoldingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 213);
        this.titleLabelY = 5;
        this.inventoryLabelY = 122;
    }

    @Override
    protected void init() {
        super.init();
        if (this.moldGrid != null) {
            this.moldGrid.close();
        }

        this.moldGrid = addRenderableWidget(new MoldGrid.Builder()
                .setPosition(this.leftPos - 72, this.topPos + 14)
                .setSize(68, 108)
                .setPatterns(MoldPatterns.ALL)
                .setOnSelected(this::selectPattern)
                .build());
        this.moldGrid.selectPattern(this.selectedPattern);

        addRenderableWidget(new ImageButton(
                this.leftPos + 13, this.topPos + 14, 20, 20,
                RecipeBookComponent.RECIPE_BUTTON_SPRITES,
                _ -> {
                    this.moldsVisible = !this.moldsVisible;
                    updateControls();
                }, MOLD_TEMPLATES_LABEL));

        this.resetButton = addRenderableWidget(Button.builder(RESET_LABEL, _ -> resetCast())
                .pos(this.leftPos + 4, this.topPos + 38)
                .size(36, 16)
                .build());
        updateControls();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        ScreenUtils.drawTexture(graphics, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (this.moldsVisible) {
            ScreenUtils.drawTexture(graphics, TEXTURE, this.leftPos - 80, this.topPos, 176, 0, 80, 133);
        }

        if (this.lastStack.isEmpty())
            return;

        TextureAtlasSprite sprite = getSprite(this.lastStack);
        if (sprite == null)
            return;

        graphics.guiRenderState.addPicturesInPictureState(new MoldCastPreviewState(
                this.leftPos + CAST_X, this.topPos + CAST_Y,
                CAST_SIZE,
                sprite,
                this.castSurface.snapshot(),
                graphics.scissorStack.peek()
        ));

        if (this.failedCast) {
            graphics.text(this.font, FAILED_LABEL, this.leftPos + 8, this.topPos + 83, 0xFFAA2222, false);
        }

        for (int polygonIndex = 0; polygonIndex < this.selectedPattern.polygons().size(); polygonIndex++) {
            if (polygonIndex < this.currentPolygonIndex)
                continue;
            List<MoldPoint> polygon = this.selectedPattern.polygons().get(polygonIndex);
            for (int i = 0; i < polygon.size(); i++) {
                MoldPoint p1 = polygon.get(i);
                MoldPoint p2 = polygon.get((i + 1) % polygon.size());

                int color = 0xAA55DDFF;
                if (this.failedCast) {
                    color = 0xAAFF7777;
                } else if (polygonIndex == this.currentPolygonIndex && i == this.currentEdge) {
                    color = 0x5555DDFF;
                }
                float position = 0;
                if (!this.failedCast && polygonIndex == this.currentPolygonIndex && this.moldTrace != null) {
                    for (MoldTrace.ProgressRange range : this.moldTrace.progress(i)) {
                        drawGuideSection(graphics, p1, p2, position, range.start(), color);
                        drawGuideSection(graphics, p1, p2, range.start(), range.end(), 0xFF33EE77);
                        position = range.end();
                    }
                }
                drawGuideSection(graphics, p1, p2, position, 1, color);
            }

            for (int i = 0; i < polygon.size(); i++) {
                int color = 0xFF55DDFF;
                if (this.failedCast) {
                    color = 0xFFFF7777;
                } else if (polygonIndex == this.currentPolygonIndex && this.moldTrace != null) {
                    if (i == (this.currentEdge + 1) % polygon.size()) {
                        color = 0xFFFFD166;
                    } else if (this.moldTrace.visitedCorner(i)) {
                        color = 0xFF33EE77;
                    }
                }
                MoldPoint point = polygon.get(i);
                int x = Math.round(this.leftPos + CAST_X + point.x() * CAST_SIZE);
                int y = Math.round(this.topPos + CAST_Y + point.y() * CAST_SIZE);
                graphics.fill(x - 3, y - 3, x + 4, y + 4, 0xFF263238);
                graphics.fill(x - 2, y - 2, x + 3, y + 3, color);
            }
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!ItemStack.isSameItemSameComponents(this.menu.getSlot(0).getItem(), this.lastStack)) {
            this.lastStack = this.menu.getSlot(0).getItem().copy();
            if (!restoreVisual()) {
                clearCast();
            }
        }
        if (++this.visualSyncTicks >= 4) {
            this.visualSyncTicks = 0;
            syncVisual();
        }
    }

    @Override
    public void onClose() {
        syncVisual();
        super.onClose();
    }

    @Override
    public void removed() {
        syncVisual();
        super.removed();
        this.tracing = false;
        this.lastGroovePoint = null;

        if (this.moldGrid != null) {
            this.moldGrid.close();
            this.moldGrid = null;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0 || !this.menu.getSlot(0).hasItem() || !this.menu.getCarried().isEmpty())
            return super.mouseClicked(event, doubleClick);

        double mouseX = event.x() - this.leftPos;
        double mouseY = event.y() - this.topPos;

        if (!isOnCast(mouseX, mouseY))
            return super.mouseClicked(event, doubleClick);

        if (isCastFinished())
            return true;

        MoldPoint point = toCastPoint(mouseX, mouseY);
        if (this.currentPolygonIndex < this.selectedPattern.polygons().size()) {
            if (this.moldTrace == null) {
                this.moldTrace = new MoldTrace(this.selectedPattern.polygons().get(this.currentPolygonIndex),
                        (float) MAX_DISTANCE / CAST_SIZE);
            }
            this.moldTrace.begin(point);
            this.failedCast |= this.moldTrace.failed();
            this.currentEdge = this.moldTrace.edge();
        } else {
            this.failedCast = true;
        }

        this.tracing = true;
        this.lastGroovePoint = point;
        carve(point, point);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() != 0 || !this.tracing)
            return super.mouseReleased(event);

        this.tracing = false;
        this.lastGroovePoint = null;
        syncVisual();
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (event.button() != 0 || !this.tracing)
            return super.mouseDragged(event, dx, dy);

        double mouseX = event.x() - this.leftPos;
        double mouseY = event.y() - this.topPos;

        if (!isOnCast(mouseX, mouseY)) {
            this.failedCast = true;
            this.visualDirty = true;
            this.lastGroovePoint = null;
            return true;
        }

        MoldPoint point = toCastPoint(mouseX, mouseY);
        carve(this.lastGroovePoint == null ? point : this.lastGroovePoint, point);
        this.lastGroovePoint = point;

        if (this.moldTrace != null) {
            boolean complete = this.moldTrace.move(point);
            this.failedCast |= this.moldTrace.failed();
            this.currentEdge = this.moldTrace.edge();
            if (complete && !this.failedCast) {
                this.tracing = false;
                this.lastGroovePoint = null;
                this.castSurface.removePolygon(this.selectedPattern, this.currentPolygonIndex);
                this.visualStrokes.clear();
                this.visualDirty = true;
                this.currentPolygonIndex++;
                this.currentEdge = 0;
                this.moldTrace = null;
                updateControls();

                if (isCastFinished()) {
                    syncVisual();
                    NetworkService.get().sendToServer(
                            new CompleteMoldPayload(this.menu.containerId, this.selectedPattern.id()));
                }
            }
        }

        return true;
    }

    private void selectPattern(MoldPattern pattern) {
        if (isCastFinished() || Objects.equals(pattern, this.selectedPattern))
            return;

        this.selectedPattern = pattern;
        resetCast();
    }

    private void resetCast() {
        if (!isCastFinished()) {
            clearCast();
        }
    }

    private void clearCast() {
        this.currentPolygonIndex = 0;
        this.currentEdge = 0;
        this.tracing = false;
        this.failedCast = false;
        this.moldTrace = null;
        this.lastGroovePoint = null;
        this.castSurface.clear();
        this.visualStrokes.clear();
        this.visualDirty = true;
        syncVisual();
        updateControls();
    }

    private void carve(MoldPoint from, MoldPoint to) {
        this.castSurface.carve(from, to, GROOVE_RADIUS);
        if (this.visualStrokes.size() < MoldTableVisualState.MAX_STROKES) {
            this.visualStrokes.add(new MoldTableVisualState.Stroke(from, to, GROOVE_RADIUS));
        }
        this.visualDirty = true;
    }

    private void syncVisual() {
        if (!this.visualDirty || this.menu.getSlot(0).getItem().isEmpty()) {
            return;
        }
        var visual = new MoldTableVisualState(
                BuiltInRegistries.ITEM.getKey(this.menu.getSlot(0).getItem().getItem()),
                this.selectedPattern.id(), this.currentPolygonIndex, this.failedCast, this.visualStrokes);
        this.menu.getBlockEntity().setVisualState(visual);
        NetworkService.get().sendToServer(new UpdateMoldVisualPayload(this.menu.containerId, visual));
        this.visualDirty = false;
    }

    private boolean restoreVisual() {
        MoldTableVisualState visual = this.menu.getBlockEntity().getVisualState();
        if (visual == null || !visual.matches(this.lastStack) || !visual.isValid()) {
            return false;
        }
        this.selectedPattern = MoldPatterns.find(visual.moldId()).orElseThrow();
        this.currentPolygonIndex = visual.completedPolygons();
        this.failedCast = visual.failed();
        this.tracing = false;
        this.lastGroovePoint = null;
        this.moldTrace = null;
        this.currentEdge = 0;
        this.castSurface.clear();
        this.visualStrokes.clear();
        for (int i = 0; i < this.currentPolygonIndex; i++) {
            this.castSurface.removePolygon(this.selectedPattern, i);
        }
        for (MoldTableVisualState.Stroke stroke : visual.strokes()) {
            this.castSurface.carve(stroke.from(), stroke.to(), stroke.radius());
            if (this.currentPolygonIndex < this.selectedPattern.polygons().size()) {
                if (this.moldTrace == null) {
                    this.moldTrace = new MoldTrace(this.selectedPattern.polygons().get(this.currentPolygonIndex),
                            (float) MAX_DISTANCE / CAST_SIZE);
                }
                if (stroke.from().equals(stroke.to())) {
                    this.moldTrace.begin(stroke.from());
                } else {
                    this.moldTrace.move(stroke.to());
                }
                this.failedCast |= this.moldTrace.failed();
                this.currentEdge = this.moldTrace.edge();
            }
        }
        this.visualStrokes.addAll(visual.strokes());
        this.visualDirty = false;
        if (this.moldGrid != null) {
            this.moldGrid.selectPattern(this.selectedPattern);
        }
        updateControls();
        return true;
    }

    private boolean isCastFinished() {
        return !this.failedCast && this.currentPolygonIndex >= this.selectedPattern.polygons().size();
    }

    private void updateControls() {
        boolean finished = isCastFinished();
        if (this.resetButton != null) {
            this.resetButton.active = !finished;
        }

        if (this.moldGrid != null) {
            this.moldGrid.visible = this.moldsVisible;
            this.moldGrid.active = this.moldsVisible && !finished;
        }
    }

    private static boolean isOnCast(double mouseX, double mouseY) {
        return mouseX >= CAST_X - MAX_DISTANCE && mouseX <= CAST_X + CAST_SIZE + MAX_DISTANCE
                && mouseY >= CAST_Y - MAX_DISTANCE && mouseY <= CAST_Y + CAST_SIZE + MAX_DISTANCE;
    }

    private static MoldPoint toCastPoint(double mouseX, double mouseY) {
        return new MoldPoint((float) ((mouseX - CAST_X) / CAST_SIZE), (float) ((mouseY - CAST_Y) / CAST_SIZE));
    }

    private void drawGuideSection(GuiGraphicsExtractor graphics, MoldPoint from, MoldPoint to,
                                  float start, float end, int color) {
        if (end <= start)
            return;

        float dx = to.x() - from.x();
        float dy = to.y() - from.y();
        drawLine(graphics,
                this.leftPos + CAST_X + (from.x() + dx * start) * CAST_SIZE,
                this.topPos + CAST_Y + (from.y() + dy * start) * CAST_SIZE,
                this.leftPos + CAST_X + (from.x() + dx * end) * CAST_SIZE,
                this.topPos + CAST_Y + (from.y() + dy * end) * CAST_SIZE,
                color);
    }

    private static void drawLine(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, int color) {
        graphics.guiRenderState.addGuiElement(new MoldLineRenderState(
                x1, y1, x2, y2,
                5.0f,
                color,
                graphics.pose(),
                graphics.scissorStack.peek(),
                MoldRenderPipelines.MOLD_LINE));
    }

    private static @Nullable TextureAtlasSprite getSprite(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        var state = new ItemStackRenderState();

        minecraft.getItemModelResolver().updateForTopItem(
                state,
                stack,
                ItemDisplayContext.GUI,
                minecraft.level,
                minecraft.player,
                0
        );

        Material.Baked material = state.pickParticleMaterial(RandomSource.create(0));
        return material != null ? material.sprite() : null;
    }
}
