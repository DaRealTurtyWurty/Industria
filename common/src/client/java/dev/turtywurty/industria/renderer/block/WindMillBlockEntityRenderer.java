package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.WindMillBlockEntity;
import dev.turtywurty.industria.model.WindMillModel;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.util.ModelSubmitUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WindMillBlockEntityRenderer
    extends IndustriaBlockEntityRenderer<WindMillBlockEntity, WindMillBlockEntityRenderer.WindMillRenderState> {
    private final WindMillModel model;

    public WindMillBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        model = new WindMillModel(context.bakeLayer(WindMillModel.LAYER_LOCATION));
    }

    @Override
    public WindMillRenderState createRenderState() {
        return new WindMillRenderState();
    }

    @Override
    public void extractRenderState(
        WindMillBlockEntity blockEntity,
        WindMillRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.rotation = blockEntity.getRenderRotation(tickProgress);
        state.installedSails = blockEntity.getInstalledSailMask();
    }

    @Override
    protected void setupBlockEntityTransformations(PoseStack matrices, WindMillRenderState state) {
        matrices.pushPose();
        matrices.translate(0.5F, 0.5F, 0.5F);
        if (state.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis() == Direction.Axis.X) {
            matrices.rotate(Axis.YP.rotationDegrees(90));
        }

        matrices.rotate(Axis.XP.rotationDegrees(180));
    }

    @Override
    protected void onRender(
        WindMillRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay
    ) {
        ModelSubmitUtils.submitModel(queue, model, new WindMillModel.RotorState(-state.rotation, state.installedSails),
            matrices, model.renderType(WindMillModel.TEXTURE_LOCATION), light, overlay, 0, state.breakProgress);
    }

    @Override
    public AABB getRenderBoundingBox(WindMillBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(3.0);
    }

    public static class WindMillRenderState extends IndustriaBlockEntityRenderState {
        public float rotation;
        public int installedSails;

        public WindMillRenderState() {
            super(0);
        }
    }
}
