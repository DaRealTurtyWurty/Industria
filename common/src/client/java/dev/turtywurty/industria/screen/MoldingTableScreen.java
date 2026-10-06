package dev.turtywurty.industria.screen;

import com.mojang.blaze3d.platform.NativeImage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.menu.MoldingTableMenu;
import dev.turtywurty.industria.molding.*;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
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
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/molding_table.png");

    private static final int CAST_X = 44;
    private static final int CAST_Y = 14;
    private static final int CAST_SIZE = 106;
    private static final double MAX_DISTANCE = 3.0;

    private boolean tracing;
    private int currentEdge;
    private ItemStack lastStack = ItemStack.EMPTY;
    private MoldPattern selectedPattern = MoldPatterns.INGOT;
    private int currentPolygonIndex = 0;
    private @Nullable DynamicTexture maskTexture;
    private @Nullable MoldGrid moldGrid;
    private boolean moldsVisible;
    private final List<MoldPoint> groovePoints = new ArrayList<>();

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
        this.moldGrid.visible = this.moldsVisible;
        this.moldGrid.active = this.moldsVisible;
        this.moldGrid.selectPattern(this.selectedPattern);

        addRenderableWidget(Button.builder(Component.empty(), _ -> {
                    this.moldsVisible = !this.moldsVisible;
                    if (this.moldGrid != null) {
                        this.moldGrid.visible = this.moldsVisible;
                        this.moldGrid.active = this.moldsVisible;
                    }
                })
                .pos(this.leftPos + 8, this.topPos + 14)
                .size(30, 20)
                .build());
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
        if (sprite == null || this.maskTexture == null)
            return;

        graphics.guiRenderState.addGuiElement(new MoldCastRenderState(
                sprite, this.maskTexture,
                this.leftPos + CAST_X, this.topPos + CAST_Y,
                CAST_SIZE,
                graphics.pose(),
                graphics.scissorStack.peek()
        ));

//        for (int i = 1; i < groovePoints.size(); i++) {
//            MoldPoint p1 = groovePoints.get(i - 1);
//            MoldPoint p2 = groovePoints.get(i);
//
//            float x1 = this.leftPos + CAST_X + p1.x() * CAST_SIZE;
//            float y1 = this.topPos + CAST_Y + p1.y() * CAST_SIZE;
//            float x2 = this.leftPos + CAST_X + p2.x() * CAST_SIZE;
//            float y2 = this.topPos + CAST_Y + p2.y() * CAST_SIZE;
//
//            drawLine(graphics, x1, y1, x2, y2, 0xFF806B48, true);
//        }

        for (int polygonIndex = 0; polygonIndex < this.selectedPattern.polygons().size(); polygonIndex++) {
            List<MoldPoint> polygon = this.selectedPattern.polygons().get(polygonIndex);
            for (int i = 0; i < polygon.size(); i++) {
                MoldPoint p1 = polygon.get(i);
                MoldPoint p2 = polygon.get((i + 1) % polygon.size());

                int x1 = this.leftPos + CAST_X + (int) (p1.x() * CAST_SIZE);
                int y1 = this.topPos + CAST_Y + (int) (p1.y() * CAST_SIZE);
                int x2 = this.leftPos + CAST_X + (int) (p2.x() * CAST_SIZE);
                int y2 = this.topPos + CAST_Y + (int) (p2.y() * CAST_SIZE);

                drawLine(graphics, x1, y1, x2, y2,
                        polygonIndex < currentPolygonIndex
                                || (polygonIndex == currentPolygonIndex && i < currentEdge)
                                ? 0xFF66FF88
                                : 0xFF55DDFF,
                        false);
            }
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!ItemStack.isSameItemSameComponents(this.menu.getSlot(0).getItem(), this.lastStack)) {
            this.tracing = false;
            this.currentEdge = 0;
            this.lastStack = this.menu.getSlot(0).getItem().copy();
            this.currentPolygonIndex = 0;
            this.groovePoints.clear();
            updateMaskTexture();
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (this.maskTexture != null) {
            this.maskTexture.close();
            this.maskTexture = null;
        }

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

        if (this.currentPolygonIndex >= selectedPattern.polygons().size())
            return super.mouseClicked(event, doubleClick);

        MoldPoint start = this.selectedPattern.polygons().get(this.currentPolygonIndex).getFirst();
        int startX = CAST_X + (int) (start.x() * CAST_SIZE);
        int startY = CAST_Y + (int) (start.y() * CAST_SIZE);
        if (Math.hypot(mouseX - startX, mouseY - startY) <= MAX_DISTANCE) {
            this.tracing = true;
            this.currentEdge = 0;
            this.groovePoints.clear();
            this.groovePoints.add(new MoldPoint((float) ((mouseX - CAST_X) / CAST_SIZE), (float) ((mouseY - CAST_Y) / CAST_SIZE)));
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() != 0 || !this.tracing)
            return super.mouseReleased(event);

        this.tracing = false;
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (event.button() != 0 || !this.tracing)
            return super.mouseDragged(event, dx, dy);

        List<MoldPoint> polygon = this.selectedPattern.polygons().get(this.currentPolygonIndex);
        MoldPoint p1 = polygon.get(this.currentEdge);
        MoldPoint p2 = polygon.get((this.currentEdge + 1) % polygon.size());

        int x1 = CAST_X + (int) (p1.x() * CAST_SIZE);
        int y1 = CAST_Y + (int) (p1.y() * CAST_SIZE);
        int x2 = CAST_X + (int) (p2.x() * CAST_SIZE);
        int y2 = CAST_Y + (int) (p2.y() * CAST_SIZE);

        double mouseX = event.x() - this.leftPos;
        double mouseY = event.y() - this.topPos;

        double distance = distanceToSegment(mouseX, mouseY, x1, y1, x2, y2);
        if (distance > MAX_DISTANCE) {
            this.tracing = false;
            return true;
        }

        this.groovePoints.add(new MoldPoint((float) ((mouseX - CAST_X) / CAST_SIZE), (float) ((mouseY - CAST_Y) / CAST_SIZE)));

        if (Math.hypot(mouseX - x2, mouseY - y2) <= MAX_DISTANCE) {
            this.currentEdge = (this.currentEdge + 1) % polygon.size();
            if (this.currentEdge == 0) {
                this.tracing = false;
                this.currentPolygonIndex++;
                updateMaskTexture();
            }
        }

        return true;
    }

    private void selectPattern(MoldPattern pattern) {
        if (Objects.equals(pattern, this.selectedPattern))
            return;

        this.selectedPattern = pattern;
        this.currentPolygonIndex = 0;
        this.currentEdge = 0;
        this.tracing = false;
        this.groovePoints.clear();
        updateMaskTexture();
    }

    private void updateMaskTexture() {
        NativeImage mask = MoldGeometry.createMask(this.selectedPattern, this.currentPolygonIndex, CAST_SIZE, CAST_SIZE);
        if (this.maskTexture == null) {
            this.maskTexture = new DynamicTexture(() -> "Molding table mask", mask);
        } else {
            this.maskTexture.setPixels(mask);
            this.maskTexture.upload();
        }
    }

    private static double distanceToSegment(double px, double py, double ax, double ay, double bx, double by) {
        double abx = bx - ax;
        double aby = by - ay;
        double apx = px - ax;
        double apy = py - ay;

        double abSquared = abx * abx + aby * aby;
        if (abSquared == 0)
            return Math.hypot(apx, apy);

        double apDotAb = apx * abx + apy * aby;

        double t = Math.clamp(apDotAb / abSquared, 0, 1);

        double closestX = ax + t * abx;
        double closestY = ay + t * aby;

        return Math.hypot(px - closestX, py - closestY);
    }

    private static void drawLine(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, int color, boolean groove) {
        graphics.guiRenderState.addGuiElement(new MoldLineRenderState(
                x1, y1, x2, y2,
                groove ? 1.5f : 5.0f,
                color,
                graphics.pose(),
                graphics.scissorStack.peek(),
                groove ? MoldRenderPipelines.MOLD_GROOVE : MoldRenderPipelines.MOLD_LINE));
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
