package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.fakeworld.FakeWorldScene;
import dev.turtywurty.industria.fakeworld.FakeWorldSceneBuilder;
import dev.turtywurty.industria.multiblock.PieceData;
import dev.turtywurty.industria.screen.widget.FakeWorldWidget;
import dev.turtywurty.industria.screenhandler.MultiblockDesignerScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// TODO: Rewrite the rendering of the blocks to actually render all the blocks in the predicate.
// TODO: Do not sync the scene every tick, only when there are changes.
public class MultiblockDesignerScreen extends HandledScreen<MultiblockDesignerScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/multiblock_designer.png");

    private FakeWorldWidget fakeWorldWidget;
    private FakeWorldScene scene;
    private Map<BlockPos, BlockState> cachedPieces = Map.of();

    public MultiblockDesignerScreen(MultiblockDesignerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.scene = FakeWorldSceneBuilder.create()
                .camera(new Vec3d(2.5, 66.0, 7.0), 200.0F, -18.0F)
                .build();
        this.fakeWorldWidget = new FakeWorldWidget.Builder()
                .position(this.x + 7, this.y + 16)
                .size(162, 162)
                .scene(this.scene)
                .build();
        addDrawableChild(this.fakeWorldWidget);
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
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void syncPreview() {
        if (this.scene == null) {
            return;
        }

        MultiblockDesignerBlockEntity blockEntity = this.handler.getBlockEntity();
        Map<BlockPos, PieceData> pieces = blockEntity.getPieces();
        Map<BlockPos, BlockState> resolved = new HashMap<>();
        for (Map.Entry<BlockPos, PieceData> entry : pieces.entrySet()) {
            resolved.put(entry.getKey(), resolveState(entry.getValue()));
        }

        for (BlockPos removed : this.cachedPieces.keySet()) {
            if (!resolved.containsKey(removed)) {
                this.scene.removeBlock(removed);
            }
        }

        for (Map.Entry<BlockPos, BlockState> entry : resolved.entrySet()) {
            BlockState previous = this.cachedPieces.get(entry.getKey());
            if (!Objects.equals(previous, entry.getValue())) {
                this.scene.addBlock(entry.getKey(), entry.getValue());
            }
        }

        this.cachedPieces = resolved;
        updateAnchor(resolved.keySet());
    }

    private BlockState resolveState(PieceData pieceData) {
        return pieceData.predicate.blocks()
                .flatMap(list -> list.stream().findFirst())
                .map(RegistryEntry::value)
                .map(Block::getDefaultState)
                .orElse(Blocks.STRUCTURE_VOID.getDefaultState());
    }

    private void updateAnchor(Collection<BlockPos> positions) {
        if (this.scene == null || this.fakeWorldWidget == null) {
            return;
        }

        if (positions.isEmpty()) {
            this.scene.clearAnchor();
            return;
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : positions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double centerZ = (minZ + maxZ) / 2.0;
        BlockPos anchorPos = BlockPos.ofFloored(centerX, centerY, centerZ);

        int targetX = this.fakeWorldWidget.getX() + this.fakeWorldWidget.getWidth() / 2;
        int targetY = this.fakeWorldWidget.getY() + this.fakeWorldWidget.getHeight() / 2;
        this.scene.setAnchor(anchorPos, targetX, targetY);

        double spanX = maxX - minX + 1;
        double spanY = maxY - minY + 1;
        double spanZ = maxZ - minZ + 1;
        double maxSpan = Math.max(spanX, Math.max(spanY, spanZ));
        double distance = Math.max(6.0, maxSpan + 4.0);

        Vec3d center = new Vec3d(centerX + 0.5, centerY + 0.5, centerZ + 0.5);
        Vec3d cameraPos = center.add(distance, distance, distance);
        Vec3d toCenter = center.subtract(cameraPos).normalize();
        float yaw = (float) (Math.toDegrees(Math.atan2(toCenter.z, toCenter.x)) + 90.0);
        float pitch = (float) -Math.toDegrees(Math.asin(toCenter.y));
        this.scene.setCamera(cameraPos, yaw, pitch);
    }
}
