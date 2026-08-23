package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.DistillationTowerBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.model.DistillationTowerModel;
import dev.turtywurty.industria.state.DistillationTowerRenderState;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DistillationTowerBlockEntityRenderer extends IndustriaBlockEntityRenderer<DistillationTowerBlockEntity, DistillationTowerRenderState> {
    private final DistillationTowerModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public DistillationTowerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new DistillationTowerModel(context.bakeLayer(DistillationTowerModel.LAYER_LOCATION));
    }

    @Override
    public DistillationTowerRenderState createRenderState() {
        return new DistillationTowerRenderState();
    }

    @Override
    protected void setupBlockEntityTransformations(PoseStack matrices, DistillationTowerRenderState state) {
        matrices.pushPose();
        matrices.translate(0.5F, 1.5F, 0.5F);
        matrices.mulPose(Axis.XP.rotationDegrees(180));
        matrices.translate(0.0F, 1.0F, 0.0F);

        if (!state.blockState.getProperties().contains(BlockStateProperties.HORIZONTAL_FACING))
            return;

        Direction facing = state.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        matrices.mulPose(Axis.YP.rotationDegrees(180 + switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));
    }

    @Override
    public void extractRenderState(DistillationTowerBlockEntity blockEntity, DistillationTowerRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();
        state.inputFluidTank = blockEntity.getInputFluidTank();
        state.primaryOutputFluidTank = blockEntity.getPrimaryOutputFluidTank();
        state.secondaryOutputFluidTank = blockEntity.getSecondaryOutputFluidTank();
    }

    @Override
    protected void onRender(DistillationTowerRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        queue.submitModel(this.model, state,
                matrices, this.model.renderType(DistillationTowerModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);

        Level level = Minecraft.getInstance().level;
        if (level == null)
            return;

        renderFluidColumn(state.inputFluidTank, queue, matrices, light, overlay, level, state.blockPos,
                -0.18F, -6.45F, -0.18F, 0.18F, -0.95F, 0.18F);
        renderFluidColumn(state.primaryOutputFluidTank, queue, matrices, light, overlay, level, state.blockPos,
                -1.45F, -5.70F, -0.55F, -0.80F, -5.05F, 0.55F);
        renderFluidColumn(state.secondaryOutputFluidTank, queue, matrices, light, overlay, level, state.blockPos,
                0.80F, -5.70F, -0.55F, 1.45F, -5.05F, 0.55F);
    }

    private void renderFluidColumn(@Nullable SyncingFluidStorage storage,
                                   SubmitNodeCollector queue, PoseStack matrices, int light, int overlay,
                                   Level level, net.minecraft.core.BlockPos pos,
                                   float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        if (storage == null || storage.getResource().isBlank() || storage.getAmount() <= 0)
            return;

        ResourceVariant<Fluid> fluidVariant = storage.getResource();
        float fluidProgress = (float) storage.getAmount() / (FluidAmounts.BUCKET * 5);
        float fluidHeight = minY + ((maxY - minY) * fluidProgress);

        this.fluidRenderer.renderTopFaceOnly(fluidVariant, queue, matrices, light, overlay, level, pos,
                minX, fluidHeight, minZ, maxX, maxZ);
    }
}
