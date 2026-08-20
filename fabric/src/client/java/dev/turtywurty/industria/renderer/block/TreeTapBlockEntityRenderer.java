package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.TreeTapBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.model.TreeTapModel;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TreeTapBlockEntityRenderer extends IndustriaBlockEntityRenderer<TreeTapBlockEntity, TreeTapBlockEntityRenderer.TreeTapRenderState> {
    private final TreeTapModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public TreeTapBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new TreeTapModel(context.bakeLayer(TreeTapModel.LAYER_LOCATION));
        //this.fluidRenderer.setShouldDebugAmount(true);
    }

    @Override
    protected void onRender(TreeTapRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        queue.submitModel(this.model, state, matrices, RenderTypes.entitySolid(TreeTapModel.TEXTURE_LOCATION), light, overlay, 0, null);

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            drawBowlFluid(state, matrices, queue, light, overlay, level);
            drawTapFluid(state, matrices, queue, light, overlay, level);
        }
    }

    private void drawBowlFluid(TreeTapRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay, ClientLevel level) {
        float fluidPercentage = state.fluidCapacity == 0 ? 0 : (float) state.fluidAmount / state.fluidCapacity;
        float height = 5f / 16f;
        float fluidHeight = (height * fluidPercentage) - 0.001f;

        float x0, z0, x1, z1;
        if (fluidPercentage <= 0.4f) {
            x0 = -0.25f;
            z0 = -0.5f;
            x1 = 0.25f;
            z1 = 0f;
        } else if (fluidPercentage < 0.8f) {
            x0 = -0.3125f;
            z0 = -0.5f;
            x1 = 0.3125f;
            z1 = 0.0625f;
        } else {
            x0 = -0.375f;
            z0 = -0.5f;
            x1 = 0.375f;
            z1 = 0.0625f;

            this.fluidRenderer.renderTopFaceOnly(state.fluidVariant, queue, matrices, light, overlay, level, state.blockPos, -0.25f, -1.4375f + fluidHeight, z1 - 0.002f, 0.25f, 0.125f);
        }

        this.fluidRenderer.renderTopFaceOnly(state.fluidVariant, queue, matrices, light, overlay, level, state.blockPos, x0, -1.4375f + fluidHeight, z0, x1, z1);
    }

    private void drawTapFluid(TreeTapRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay, ClientLevel level) {
        float fluidPercentage = state.fluidCapacity == 0 ? 0 : (float) state.fluidAmount / state.fluidCapacity;
        if(fluidPercentage < 0.85f)
            return;

        float height = 5f / 16f;
        float fluidHeight = (height * fluidPercentage) - 0.001f;

        matrices.pushPose();
        matrices.mulPose(Axis.XP.rotationDegrees(45f));
        this.fluidRenderer.renderTopFaceOnly(state.fluidVariant, queue, matrices, light, overlay, level, state.blockPos, -0.1249f, -0.985f + fluidHeight, 0f, 0.1249f, 0.464f);
        matrices.mulPose(Axis.XN.rotationDegrees(-90f));
        this.fluidRenderer.renderTopFaceOnly(state.fluidVariant, queue, matrices, light, overlay, level, state.blockPos, -0.1249f, 0.1865f + fluidHeight, 0.707f, 0.1249f, 0.7965f);
        matrices.popPose();
    }

    @Override
    public TreeTapRenderState createRenderState() {
        return new TreeTapRenderState();
    }

    @Override
    public void extractRenderState(TreeTapBlockEntity blockEntity, TreeTapRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        OutputFluidStorage fluidStorage = blockEntity.getFluidStorage();
        state.fluidVariant = fluidStorage.variant;
        state.fluidAmount = fluidStorage.amount;
        state.fluidCapacity = fluidStorage.getCapacity();
    }

    public static class TreeTapRenderState extends IndustriaBlockEntityRenderState {
        public FluidVariant fluidVariant = FluidVariant.blank();
        public long fluidAmount = 0;
        public long fluidCapacity = 0;

        public TreeTapRenderState() {
            super(0);
        }
    }
}
