package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.multiblock.PieceData;
import dev.turtywurty.industria.multiblock.VariedBlockList;
import dev.turtywurty.industria.util.VariedBlockListRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

import java.util.Map;

// TODO: Render as ghost blocks (will need custom render type)
public class MultiblockDesignerBlockEntityRenderer extends IndustriaBlockEntityRenderer<MultiblockDesignerBlockEntity> {
    public MultiblockDesignerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void onRender(MultiblockDesignerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Map<BlockPos, PieceData> pieces = entity.getPieces();
        if (pieces.isEmpty()) return;

        matrices.push();
        matrices.translate(-0.5, 0.5, -0.5);

        BlockPos origin = entity.getPos();
        for (PieceData piece : pieces.values()) {
            BlockPos position = origin.subtract(piece.position);

            matrices.push();
            matrices.translate(-position.getX(), position.getY(), position.getZ());
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            matrices.translate(0, -1, -1);

            VariedBlockList blockList = piece.variedBlockList;
            VariedBlockListRenderer.renderInWorld(blockList, position, entity.getWorld(), matrices, vertexConsumers, light, overlay, tickDelta);

            matrices.pop();
        }

        matrices.pop();
    }
}
