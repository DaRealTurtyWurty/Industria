package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.UpgradeStationBlockEntity;
import dev.turtywurty.industria.model.UpgradeStationModel;
import dev.turtywurty.industria.state.UpgradeStationRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class UpgradeStationBlockEntityRenderer extends IndustriaBlockEntityRenderer<UpgradeStationBlockEntity, UpgradeStationRenderState> {
    private final UpgradeStationModel model;

    public UpgradeStationBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new UpgradeStationModel(context.getLayerModelPart(UpgradeStationModel.LAYER_LOCATION));
    }

    @Override
    public UpgradeStationRenderState createRenderState() {
        return new UpgradeStationRenderState();
    }

    @Override
    public void updateRenderState(UpgradeStationBlockEntity blockEntity, UpgradeStationRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
    }

    @Override
    public void onRender(UpgradeStationRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 + switch (state.blockState.get(Properties.HORIZONTAL_FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));

        RenderLayer renderLayer = this.model.getLayer(UpgradeStationModel.TEXTURE_LOCATION);
        queue.submitModel(this.model, state, matrices, renderLayer, light, overlay, 0, state.crumblingOverlay);

        matrices.pop();
    }
}
