package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.multiblock.PieceData;
import dev.turtywurty.industria.state.MultiblockDesignerRenderState;
import dev.turtywurty.industria.util.VariedBlockListRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// TODO: Render as ghost blocks (will need custom render type)
public class MultiblockDesignerBlockEntityRenderer extends IndustriaBlockEntityRenderer<MultiblockDesignerBlockEntity, MultiblockDesignerRenderState> {
    public MultiblockDesignerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public MultiblockDesignerRenderState createRenderState() {
        return new MultiblockDesignerRenderState();
    }

    @Override
    public void extractRenderState(MultiblockDesignerBlockEntity blockEntity, MultiblockDesignerRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.pieces.clear();
        state.pieces.putAll(blockEntity.getPieces());
    }

    @Override
    protected void onRender(MultiblockDesignerRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        if (state.pieces.isEmpty())
            return;

        matrices.pushPose();
        matrices.translate(-0.5, 0.5, -0.5);

        for (PieceData piece : state.pieces.values()) {
            BlockPos position = state.blockPos.subtract(piece.position);

            matrices.pushPose();
            matrices.translate(-position.getX(), position.getY(), position.getZ());
            matrices.mulPose(Axis.XP.rotationDegrees(180));
            matrices.translate(0, -1, -1);

            VariedBlockListRenderer.renderInWorld(piece.variedBlockList, position, Minecraft.getInstance().level, matrices, queue, light, overlay);

            matrices.popPose();
        }

        matrices.popPose();
    }
}
