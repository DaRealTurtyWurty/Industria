package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.multiblock.PieceData;
import dev.turtywurty.industria.state.MultiblockDesignerRenderState;
import dev.turtywurty.industria.util.VariedBlockListRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

// TODO: Render as ghost blocks (will need custom render type)
public class MultiblockDesignerBlockEntityRenderer extends IndustriaBlockEntityRenderer<MultiblockDesignerBlockEntity, MultiblockDesignerRenderState> {
    public MultiblockDesignerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public MultiblockDesignerRenderState createRenderState() {
        return new MultiblockDesignerRenderState();
    }

    @Override
    public void updateRenderState(MultiblockDesignerBlockEntity blockEntity, MultiblockDesignerRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.pieces.clear();
        state.pieces.putAll(blockEntity.getPieces());
    }

    @Override
    protected void onRender(MultiblockDesignerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        if (state.pieces.isEmpty())
            return;

        matrices.push();
        matrices.translate(-0.5, 0.5, -0.5);

        for (PieceData piece : state.pieces.values()) {
            BlockPos position = state.pos.subtract(piece.position);

            matrices.push();
            matrices.translate(-position.getX(), position.getY(), position.getZ());
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            matrices.translate(0, -1, -1);

            VariedBlockListRenderer.renderInWorld(piece.variedBlockList, position, MinecraftClient.getInstance().world, matrices, queue, light, overlay);

            matrices.pop();
        }

        matrices.pop();
    }
}
