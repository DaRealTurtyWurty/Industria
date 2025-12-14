package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.multiblock.PieceData;
import dev.turtywurty.industria.network.SetMultiblockPieceCharPayload;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScene;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldSceneBuilder;
import dev.turtywurty.industria.screen.widget.CharTextFieldWidget;
import dev.turtywurty.industria.screen.widget.FakeWorldWidget;
import dev.turtywurty.industria.screenhandler.MultiblockDesignerScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Supplier;

// TODO: Do not sync the scene every tick, only when there are changes.
public class MultiblockDesignerScreen extends HandledScreen<MultiblockDesignerScreenHandler> {
    private MultiblockDesignerWorldWidget fakeWorldWidget;
    private FakeWorldScene scene;
    private Map<BlockPos, BlockPredicate> cachedPredicates = Map.of();
    private Map<BlockPos, FakeWorldScene.Nameplate> predicateNameplates = Map.of();
    private Set<BlockPos> cachedResolvedPositions = Set.of();
    private BlockPos selectedPiece;
    private CharTextFieldWidget paletteCharField;
    private ButtonWidget confirmPaletteButton;

    public MultiblockDesignerScreen(MultiblockDesignerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.playerInventoryTitleX = -10000;
        this.titleX = -10000;

        if (this.fakeWorldWidget != null) {
            this.fakeWorldWidget.onClose();
            this.fakeWorldWidget = null;
            this.scene = null;
        }

        this.scene = FakeWorldSceneBuilder.create()
                .camera(new Vec3d(2.5, 66.0, 7.0), 200.0F, -18.0F)
                .build();
        this.fakeWorldWidget = addDrawableChild(new MultiblockDesignerWorldWidget(this.x + 7, this.y + 16));

        initPaletteInputs();
        this.cachedPredicates = Map.of();
        this.predicateNameplates = Map.of();
        this.cachedResolvedPositions = Set.of();
        syncPreview();
    }

    @Override
    public void close() {
        super.close();
        if (this.fakeWorldWidget != null) {
            this.fakeWorldWidget.onClose();
        }
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        syncPreview();
        if (this.fakeWorldWidget != null) {
            this.fakeWorldWidget.tick();
        }

        syncSelectedPaletteChar();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.fakeWorldWidget != null && this.scene != null) {
            if (this.fakeWorldWidget.handleClick(mouseX, mouseY))
                return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scene != null && button == 0) {
            float sensitivity = 0.35F;
            this.scene.rotateCamera((float) (deltaX * sensitivity), (float) (deltaY * sensitivity));
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        if (this.paletteCharField != null && this.paletteCharField.isVisible()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("Palette Key"), this.paletteCharField.getX(), this.paletteCharField.getY() - 10, 0xFFFFFF);
        }
    }

    private void syncPreview() {
        if (this.scene == null)
            return;

        MultiblockDesignerBlockEntity blockEntity = this.handler.getBlockEntity();
        BlockPos origin = blockEntity.getPos();
        Map<BlockPos, PieceData> pieces = blockEntity.getPieces();
        Set<BlockPos> resolved = new HashSet<>();
        Map<BlockPos, BlockPredicate> predicates = new HashMap<>();
        Map<BlockPos, FakeWorldScene.Nameplate> nameplates = new HashMap<>();
        for (Map.Entry<BlockPos, PieceData> entry : pieces.entrySet()) {
            BlockPos relative = entry.getKey().subtract(origin);
            resolved.add(relative);
            PieceData data = entry.getValue();
            predicates.put(relative, data.predicate);

            char paletteChar = data.paletteChar;
            String paletteText = paletteChar == ' ' ? "_" : String.valueOf(paletteChar);
            FakeWorldScene.Nameplate existing = this.predicateNameplates.get(relative);
            boolean paletteChanged = existing == null || !existing.text().getString().equals(paletteText);
            if (paletteChanged && existing != null) {
                this.scene.removeNameplate(existing);
            }

            FakeWorldScene.Nameplate nameplate = paletteChanged
                    ? this.scene.addNameplate(Vec3d.ofCenter(relative), Text.literal(paletteText), 0.75F)
                    : existing;
            if (nameplate != null) {
                nameplates.put(relative, nameplate);
            }
        }

        for (BlockPos removed : this.cachedPredicates.keySet()) {
            if (!predicates.containsKey(removed)) {
                this.scene.removePredicate(removed);
                FakeWorldScene.Nameplate removedNameplate = this.predicateNameplates.get(removed);
                if (removedNameplate != null) {
                    this.scene.removeNameplate(removedNameplate);
                }
            }
        }

        for (Map.Entry<BlockPos, BlockPredicate> entry : predicates.entrySet()) {
            BlockPredicate previous = this.cachedPredicates.get(entry.getKey());
            if (!Objects.equals(previous, entry.getValue())) {
                this.scene.addPredicate(entry.getKey(), entry.getValue());
            }
        }

        if (this.selectedPiece != null && !predicates.containsKey(this.selectedPiece)) {
            clearSelection();
        }

        this.cachedPredicates = predicates;
        this.predicateNameplates = nameplates;

        boolean resolvedChanged = !resolved.equals(this.cachedResolvedPositions);
        this.cachedResolvedPositions = Set.copyOf(resolved);
        if (resolvedChanged) {
            int targetX = this.fakeWorldWidget.getWidth() / 2;
            int targetY = this.fakeWorldWidget.getHeight() / 2;
            this.scene.updateAnchor(resolved, targetX, targetY);
        }
    }

    private void initPaletteInputs() {
        int fieldX = this.fakeWorldWidget.getX() + this.fakeWorldWidget.getWidth() + 10;
        int fieldY = this.fakeWorldWidget.getY();
        this.paletteCharField = addDrawableChild(new CharTextFieldWidget(this.textRenderer, fieldX, fieldY, 30, 20, Text.literal("Palette Key")));
        this.paletteCharField.setVisible(false);

        int buttonX = fieldX + this.paletteCharField.getWidth() + 4;
        this.confirmPaletteButton = addDrawableChild(new ConfirmPaletteButton(buttonX, fieldY));
        this.confirmPaletteButton.visible = false;
        this.confirmPaletteButton.active = false;

        if (this.selectedPiece != null) {
            populatePaletteInput();
        }
    }

    private void selectPiece(BlockPos relativePos) {
        this.selectedPiece = relativePos;
        if (this.scene != null) {
            this.scene.highlightBlock(relativePos, 0xFF2ECC71, 0.03F);
        }

        populatePaletteInput();
        if (this.paletteCharField != null) {
            this.paletteCharField.setFocused(true);
            this.setFocused(this.paletteCharField);
            this.paletteCharField.setCursorToEnd(false);
        }
    }

    private void populatePaletteInput() {
        if (this.selectedPiece == null || this.paletteCharField == null || this.confirmPaletteButton == null || this.fakeWorldWidget == null)
            return;

        this.paletteCharField.setVisible(true);
        this.confirmPaletteButton.visible = true;
        this.confirmPaletteButton.active = true;

        BlockPos worldPos = this.selectedPiece.add(this.handler.getBlockEntity().getPos());
        PieceData data = this.handler.getBlockEntity().getPieces().get(worldPos);
        char paletteChar = data != null ? data.paletteChar : ' ';
        String text = paletteChar == ' ' ? "" : String.valueOf(paletteChar);
        if (!Objects.equals(this.paletteCharField.getText(), text)) {
            this.paletteCharField.setText(text);
        }
    }

    private void clearSelection() {
        this.selectedPiece = null;
        if (this.scene != null) {
            this.scene.clearHighlights();
        }
        if (this.paletteCharField != null) {
            this.paletteCharField.setText("");
            this.paletteCharField.setVisible(false);
        }

        if (this.confirmPaletteButton != null) {
            this.confirmPaletteButton.visible = false;
            this.confirmPaletteButton.active = false;
        }
    }

    private void confirmPaletteChar() {
        if (this.selectedPiece == null)
            return;

        char paletteChar = getPaletteCharInput();
        MultiblockDesignerBlockEntity blockEntity = this.handler.getBlockEntity();
        BlockPos worldPos = this.selectedPiece.add(blockEntity.getPos());
        blockEntity.setPaletteChar(worldPos, paletteChar);
        ClientPlayNetworking.send(new SetMultiblockPieceCharPayload(worldPos, paletteChar));
        clearSelection();
        syncPreview();
    }

    private char getPaletteCharInput() {
        if (this.paletteCharField == null)
            return ' ';

        String text = this.paletteCharField.getText();
        if (text == null || text.isEmpty())
            return ' ';

        return text.charAt(0);
    }

    private void syncSelectedPaletteChar() {
        if (this.selectedPiece == null || this.paletteCharField == null || this.paletteCharField.isFocused())
            return;

        BlockPos worldPos = this.selectedPiece.add(this.handler.getBlockEntity().getPos());
        PieceData data = this.handler.getBlockEntity().getPieces().get(worldPos);
        if (data == null) {
            clearSelection();
            return;
        }

        String paletteText = data.paletteChar == ' ' ? "" : String.valueOf(data.paletteChar);
        if (!Objects.equals(this.paletteCharField.getText(), paletteText)) {
            this.paletteCharField.setText(paletteText);
        }
    }

    public class MultiblockDesignerWorldWidget extends FakeWorldWidget {
        public MultiblockDesignerWorldWidget(int x, int y) {
            super(MultiblockDesignerScreen.this.scene, x, y, 162, 162, true, 0.35F);
        }

        public boolean handleClick(double mouseX, double mouseY) {
            if (isInsideWidget(mouseX, mouseY)) {
                BlockPos closest = findClosestPiece(mouseX, mouseY);
                if (closest != null) {
                    MultiblockDesignerScreen.this.selectPiece(closest);
                    return true;
                }
            }

            return false;
        }

        private boolean isInsideWidget(double mouseX, double mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.width
                    && mouseY >= this.y && mouseY < this.y + this.height;
        }

        private BlockPos findClosestPiece(double mouseX, double mouseY) {
            double closestDistanceSq = 100.0;
            BlockPos closest = null;
            for (BlockPos pos : MultiblockDesignerScreen.this.cachedPredicates.keySet()) {
                Optional<Vec2f> projected = this.scene.projectToWidget(Vec3d.ofCenter(pos));
                if (projected.isEmpty())
                    continue;

                Vec2f screenPos = projected.get();
                double dx = screenPos.x - mouseX;
                double dy = screenPos.y - mouseY;
                double distanceSq = dx * dx + dy * dy;
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closest = pos;
                }
            }

            return closest;
        }
    }

    private class ConfirmPaletteButton extends ButtonWidget {
        private static final Identifier CHECKMARK_TEXTURE = Identifier.ofVanilla("icon/checkmark");

        public ConfirmPaletteButton(int x, int y) {
            super(x, y, 18, 18, Text.empty(), button -> MultiblockDesignerScreen.this.confirmPaletteChar(), Supplier::get);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            int x = getX();
            int y = getY();
            int w = getWidth();
            int h = getHeight();

            int texW = 9;
            int texH = 8;
            int drawX = x + (w - texW) / 2;
            int drawY = y + (h - texH) / 2;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, CHECKMARK_TEXTURE, drawX, drawY, texW, texH);
        }
    }
}
