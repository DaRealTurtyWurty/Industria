package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.WaterWheelBlockEntity;
import dev.turtywurty.industria.model.WaterWheelModel;
import dev.turtywurty.industria.state.WaterWheelRenderState;
import dev.turtywurty.industria.util.ModelSubmitUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WaterWheelBlockEntityRenderer
    extends IndustriaBlockEntityRenderer<WaterWheelBlockEntity, WaterWheelRenderState> {
    private final WaterWheelModel model;

    public WaterWheelBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        model = new WaterWheelModel(context.bakeLayer(WaterWheelModel.LAYER_LOCATION));
    }

    @Override
    public WaterWheelRenderState createRenderState() {
        return new WaterWheelRenderState();
    }

    @Override
    public void extractRenderState(
        WaterWheelBlockEntity blockEntity,
        WaterWheelRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.rotation = blockEntity.getRenderRotation(tickProgress);
    }

    @Override
    protected void setupBlockEntityTransformations(PoseStack matrices, WaterWheelRenderState state) {
        matrices.pushPose();
        matrices.translate(0.5F, 1.5F, 0.5F);
        if (state.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis() == Direction.Axis.X) {
            matrices.rotate(Axis.YP.rotationDegrees(90));
        }

        matrices.rotate(Axis.XP.rotationDegrees(180));
    }

    @Override
    protected void onRender(
        WaterWheelRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay
    ) {
        ModelSubmitUtils.submitModel(queue, model, new WaterWheelModel.WaterWheelModelRenderState(-state.rotation),
            matrices, model.renderType(WaterWheelModel.TEXTURE_LOCATION), light, overlay, 0, state.breakProgress);
    }

    @Override
    public AABB getRenderBoundingBox(WaterWheelBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1.0);
    }
}
