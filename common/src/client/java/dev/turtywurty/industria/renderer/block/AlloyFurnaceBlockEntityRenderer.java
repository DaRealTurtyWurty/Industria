package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.AlloyFurnaceBlockEntity;
import dev.turtywurty.industria.model.AlloyFurnaceModel;
import dev.turtywurty.industria.state.ContainerDoorRenderState;
import dev.turtywurty.industria.util.ModelSubmitUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AlloyFurnaceBlockEntityRenderer
    extends IndustriaBlockEntityRenderer<AlloyFurnaceBlockEntity, ContainerDoorRenderState> {
    private final AlloyFurnaceModel model;

    public AlloyFurnaceBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new AlloyFurnaceModel(context.bakeLayer(AlloyFurnaceModel.LAYER_LOCATION));
    }

    @Override
    public ContainerDoorRenderState createRenderState() {
        return new ContainerDoorRenderState();
    }

    @Override
    public void extractRenderState(
        AlloyFurnaceBlockEntity blockEntity,
        ContainerDoorRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.doorOpenness = blockEntity.getOpenNess(tickProgress);
    }

    @Override
    protected void onRender(ContainerDoorRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        ModelSubmitUtils.submitModel(queue, this.model, state, matrices,
            this.model.renderType(AlloyFurnaceModel.TEXTURE_LOCATION), light, overlay, 0, state.breakProgress);
    }
}
