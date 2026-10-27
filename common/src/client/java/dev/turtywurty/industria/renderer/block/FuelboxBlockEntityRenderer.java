package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.FuelboxBlockEntity;
import dev.turtywurty.industria.model.FuelboxModel;
import dev.turtywurty.industria.state.ContainerDoorRenderState;
import dev.turtywurty.industria.util.ModelSubmitUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FuelboxBlockEntityRenderer
    extends
        IndustriaBlockEntityRenderer<FuelboxBlockEntity, ContainerDoorRenderState> {
    private final FuelboxModel model;

    public FuelboxBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new FuelboxModel(context.bakeLayer(FuelboxModel.LAYER_LOCATION));
    }

    @Override
    public ContainerDoorRenderState createRenderState() {
        return new ContainerDoorRenderState();
    }

    @Override
    public void extractRenderState(
        FuelboxBlockEntity blockEntity,
        ContainerDoorRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.doorOpenness = blockEntity.getOpenNess(tickProgress);

        Level level = blockEntity.getLevel();
        if (level != null) {
            var frontPos = state.blockPos.relative(state.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING));
            state.lightCoords = LightCoordsUtil.max(state.lightCoords, LightCoordsUtil.getLightCoords(level, frontPos));
        }
    }

    @Override
    protected void onRender(
        ContainerDoorRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay
    ) {
        ModelSubmitUtils.submitModel(queue, this.model, state,
            matrices, this.model.renderType(FuelboxModel.TEXTURE_LOCATION),
            light, overlay, 0, state.breakProgress);
    }
}
