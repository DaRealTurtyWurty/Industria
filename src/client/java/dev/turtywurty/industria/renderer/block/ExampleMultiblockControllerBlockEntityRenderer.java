package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.MultiblockControllerBlockEntity;
import dev.turtywurty.industria.state.ExampleMultiblockControllerRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ExampleMultiblockControllerBlockEntityRenderer extends IndustriaBlockEntityRenderer<MultiblockControllerBlockEntity, ExampleMultiblockControllerRenderState> {
    public ExampleMultiblockControllerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ExampleMultiblockControllerRenderState createRenderState() {
        return new ExampleMultiblockControllerRenderState();
    }

    @Override
    public void extractRenderState(MultiblockControllerBlockEntity blockEntity, ExampleMultiblockControllerRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.positions.clear();
        state.positions.addAll(blockEntity.getPositions());
    }

    @Override
    protected void onRender(ExampleMultiblockControllerRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(0, 1, 0);

        for (BlockPos position : state.positions) {
            Gizmos.cuboid(position, GizmoStyle.strokeAndFill(0x7700FF00, 5.0f, 0x5500EE00));
        }

        matrices.popPose();
    }
}
