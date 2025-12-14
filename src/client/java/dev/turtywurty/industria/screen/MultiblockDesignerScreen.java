package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.multiblock.PieceData;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScene;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldSceneBuilder;
import dev.turtywurty.industria.screen.widget.FakeWorldWidget;
import dev.turtywurty.industria.screenhandler.MultiblockDesignerScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

// TODO: Do not sync the scene every tick, only when there are changes.
public class MultiblockDesignerScreen extends HandledScreen<MultiblockDesignerScreenHandler> {
    private FakeWorldWidget fakeWorldWidget;
    private FakeWorldScene scene;
    private Map<BlockPos, BlockPredicate> cachedPredicates = Map.of();
    private Map<BlockPos, FakeWorldScene.Nameplate> predicateNameplates = Map.of();
    private Set<BlockPos> cachedResolvedPositions = Set.of();

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
        this.fakeWorldWidget = addDrawableChild(new FakeWorldWidget.Builder()
                .position(this.x + 7, this.y + 16)
                .size(162, 162)
                .scene(this.scene)
                .enableInteraction(true)
                .build());
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

            String paletteText = "beans";
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
}
