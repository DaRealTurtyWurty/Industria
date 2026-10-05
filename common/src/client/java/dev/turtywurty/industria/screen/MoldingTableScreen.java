package dev.turtywurty.industria.screen;

import com.mojang.blaze3d.platform.NativeImage;
import dev.turtywurty.industria.menu.MoldingTableMenu;
import dev.turtywurty.industria.molding.*;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

import java.util.List;
import java.util.Objects;

public class MoldingTableScreen extends AbstractContainerScreen<MoldingTableMenu> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/dispenser.png");

    private static final int CAST_X = 112;
    private static final int CAST_Y = 16;
    private static final int CAST_SIZE = 56;

    private boolean tracing;
    private int currentEdge;
    private ItemStack lastStack = ItemStack.EMPTY;
    private MoldPattern selectedPattern = MoldPatterns.INGOT;
    private int currentPolygonIndex = 0;
    private @Nullable DynamicTexture maskTexture;

    public MoldingTableScreen(MoldingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        MoldList moldList = addRenderableWidget(new MoldList(this.minecraft,
                this.leftPos + 8, this.topPos + 16,
                64, 56,
                this::selectPattern));
        for (MoldList.MoldEntry child : moldList.children()) {
            if (Objects.equals(child.getPattern(), this.selectedPattern)) {
                moldList.setSelected(child);
                break;
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        ScreenUtils.drawTexture(graphics, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

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
                                : 0xFF55DDFF);
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
        if (Math.hypot(mouseX - startX, mouseY - startY) <= 3.0) {
            this.tracing = true;
            this.currentEdge = 0;
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
        if (distance > 3.0) {
            this.tracing = false;
            return true;
        }

        if (Math.hypot(mouseX - x2, mouseY - y2) <= 3.0) {
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

    private static void drawLine(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color) {
        graphics.guiRenderState.addGuiElement(new MoldLineRenderState(x1, y1, x2, y2, 5.0f, color, graphics.pose(), graphics.scissorStack.peek()));
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
